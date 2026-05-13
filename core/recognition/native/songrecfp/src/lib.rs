#![cfg(target_os = "android")]

mod fingerprinting {
    pub mod algorithm;
    mod hanning;
    pub mod signature_format;
}

use core::ffi::c_void;
use fingerprinting::algorithm::SignatureGenerator;
use jni::errors::Error;
use jni::objects::{JClass, JShortArray, JString};
use jni::sys::{jint, JNI_VERSION_1_6};
use jni::vm::JavaVM;
use jni::{Env, NativeMethod};

const SONGREC_SIGNATURE_METHODS: &[NativeMethod] = &[
    jni::native_method! {
        static fn fromPcm16Mono16kHz(samples: [jshort]) -> JString,
        fn = from_pcm16_mono_16khz_impl,
    },
];

fn from_pcm16_mono_16khz_impl<'local>(
    env: &mut Env<'local>,
    _class: JClass<'local>,
    samples: JShortArray<'local>,
) -> Result<JString<'local>, Error> {
    let size = samples.len(env)?;
    let mut buffer = vec![0i16; size];
    samples.get_region(env, 0, &mut buffer)?;

    let uri = SignatureGenerator::make_signature_from_buffer(&buffer)
        .encode_to_uri()
        .map_err(|e| Error::ParseFailed(format!("songrecfp: Failed to encode signature: {e}")))?;

    JString::from_str(env, uri)
}

fn register_songrec_signature_natives(env: &mut Env<'_>) -> Result<(), Error> {
    let class = env.find_class(jni::jni_str!(
        "com/mrsep/musicrecognizer/core/recognition/shazam/SongRecSignature"
    ))?;
    unsafe { env.register_native_methods(class, SONGREC_SIGNATURE_METHODS) }
}

#[unsafe(no_mangle)]
pub extern "system" fn JNI_OnLoad(vm: *mut jni::sys::JavaVM, _: *mut c_void) -> jint {
    let vm = unsafe { JavaVM::from_raw(vm) };

    let result = vm.attach_current_thread(|env| -> Result<(), Error> {
        register_songrec_signature_natives(env)?;
        Ok(())
    });

    if result.is_err() {
        return 0;
    }

    JNI_VERSION_1_6
}
