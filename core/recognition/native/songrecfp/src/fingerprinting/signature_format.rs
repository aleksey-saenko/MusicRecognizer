use base64::Engine;
use byteorder::{LittleEndian, WriteBytesExt};
use crc32fast::Hasher;
use std::error::Error;
use std::io::{Cursor, Seek, SeekFrom, Write};

const DATA_URI_PREFIX: &str = "data:audio/vnd.shazam.sig;base64,";

pub struct FrequencyPeak {
    pub fft_pass_number: u32,
    pub peak_magnitude: u16,
    pub corrected_peak_frequency_bin: u16,
}

#[derive(Hash, Eq, PartialEq, Ord, PartialOrd, Debug, Clone, Copy)]
pub enum FrequencyBand {
    _250_520 = 0,
    _520_1450 = 1,
    _1450_3500 = 2,
    _3500_5500 = 3,
}

// struct RawSignatureHeader {
//     magic1: u32,                                  // Fixed 0xcafe2580 - 80 25 fe ca
//     crc32: u32, // CRC-32 for all of the following (so excluding these first 8 bytes)
//     size_minus_header: u32, // Total size of the message, minus the size of the current header (which is 48 bytes)
//     magic2: u32,            // Fixed 0x94119c00 - 00 9c 11 94
//     _void1: [u32; 3],       // Void
//     shifted_sample_rate_id: u32, // A member of SampleRate (usually 3 for 16000 Hz), left-shifted by 27 (usually giving 0x18000000 - 00 00 00 18)
//     _void2: [u32; 2],            // Void, or maybe used only in "rolling window" mode?
//     number_samples_plus_divided_sample_rate: u32, // int(number_of_samples + sample_rate * 0.24) - As the sample rate is known thanks to the field above, it can be inferred and substracted so that we obtain the number of samples, and from the number of samples and sample rate we can obtain the length of the recording
//     _fixed_value: u32, // Calculated as ((15 << 19) + 0x40000) - 0x7c0000 or 00 00 7c 00 - seems pretty constant, may be different in the "SigType.STREAMING" mode
// }

pub struct DecodedSignature {
    pub sample_rate_hz: u32,
    pub number_samples: u32,
    pub frequency_band_to_sound_peaks: [Vec<FrequencyPeak>; 4],
}

impl DecodedSignature {

    pub fn encode_to_binary(&self) -> Result<Vec<u8>, Box<dyn Error>> {
        let mut cursor = Cursor::new(vec![]);

        // Please see the RawSignatureHeader structure definition above for
        // information about the following fields.

        cursor.write_u32::<LittleEndian>(0xcafe2580)?; // magic1
        cursor.write_u32::<LittleEndian>(0)?; // crc32 - Will write later
        cursor.write_u32::<LittleEndian>(0)?; // size_minus_header - Will write later
        cursor.write_u32::<LittleEndian>(0x94119c00)?; // magic2
        cursor.write_u32::<LittleEndian>(0)?; // void1
        cursor.write_u32::<LittleEndian>(0)?;
        cursor.write_u32::<LittleEndian>(0)?;
        cursor.write_u32::<LittleEndian>(
            match self.sample_rate_hz {
                8000 => 1,
                11025 => 2,
                16000 => 3,
                32000 => 4,
                44100 => 5,
                48000 => 6,
                _ => { panic!("Invalid sample rate passed when encoding Shazam packet"); }
            } << 27,
        )?; // shifted_sample_rate_id
        cursor.write_u32::<LittleEndian>(0)?; // void2
        cursor.write_u32::<LittleEndian>(0)?;
        cursor.write_u32::<LittleEndian>(
            self.number_samples + (self.sample_rate_hz as f32 * 0.24) as u32,
        )?; // number_samples_plus_divided_sample_rate
        cursor.write_u32::<LittleEndian>((15 << 19) + 0x40000)?; // fixed_value

        cursor.write_u32::<LittleEndian>(0x40000000)?;
        cursor.write_u32::<LittleEndian>(0)?; // size_minus_header - Will write later

        for (frequency_band, frequency_peaks) in
            self.frequency_band_to_sound_peaks.iter().enumerate()
        {
            if frequency_peaks.is_empty() {
                continue;
            }
            let mut peaks_cursor = Cursor::new(vec![]);

            let mut fft_pass_number = 0;

            for frequency_peak in frequency_peaks {
                assert!(frequency_peak.fft_pass_number >= fft_pass_number);

                if frequency_peak.fft_pass_number - fft_pass_number >= 255 {
                    peaks_cursor.write_u8(0xff)?;
                    peaks_cursor.write_u32::<LittleEndian>(frequency_peak.fft_pass_number)?;

                    fft_pass_number = frequency_peak.fft_pass_number;
                }

                peaks_cursor.write_u8((frequency_peak.fft_pass_number - fft_pass_number) as u8)?;

                peaks_cursor.write_u16::<LittleEndian>(frequency_peak.peak_magnitude)?;
                peaks_cursor
                    .write_u16::<LittleEndian>(frequency_peak.corrected_peak_frequency_bin)?;

                fft_pass_number = frequency_peak.fft_pass_number;
            }

            let peaks_buffer = peaks_cursor.into_inner();

            cursor.write_u32::<LittleEndian>(0x60030040 + frequency_band as u32)?;
            cursor.write_u32::<LittleEndian>(peaks_buffer.len() as u32)?;
            cursor.write_all(&peaks_buffer)?;
            for _padding_index in 0..((4 - peaks_buffer.len() as u32 % 4) % 4) {
                cursor.write_u8(0)?;
            }
        }

        let buffer_size = cursor.position() as u32;

        cursor.seek(SeekFrom::Start(8))?;
        cursor.write_u32::<LittleEndian>(buffer_size - 48)?;

        cursor.seek(SeekFrom::Start(48 + 4))?;
        cursor.write_u32::<LittleEndian>(buffer_size - 48)?;

        cursor.seek(SeekFrom::Start(4))?;
        let mut hasher = Hasher::new();
        hasher.update(&cursor.get_ref()[8..]);
        cursor.write_u32::<LittleEndian>(hasher.finalize())?; // crc32

        Ok(cursor.into_inner())
    }

    pub fn encode_to_uri(&self) -> Result<String, Box<dyn Error>> {
        Ok(format!(
            "{}{}",
            DATA_URI_PREFIX,
            base64::prelude::BASE64_STANDARD.encode(self.encode_to_binary()?)
        ))
    }
}
