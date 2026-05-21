Rust implementation of Shazam's audio fingerprinting algorithm based on SongRec (https://github.com/marin-m/SongRec)

Based on commit: `5afbf7361fcd72a3edaaeed24dc0ea150fd79385` (tag 0.7.3)

Modifications:

- Pruned source files and dependencies to keep only the core algorithm that converts an audio buffer into a Shazam fingerprint
- Added `lib.rs` as a bridge between Rust and Android code
- Replaced large buffer initializations with Vecs to prevent segmentation faults on Android background threads