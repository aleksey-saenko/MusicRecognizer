Rust implementation of Shazam's audio fingerprinting algorithm based on SongRec (https://github.com/marin-m/SongRec)

Based on commit: `be369cc0cb6e8fda18f3c7a462f392bb8dd5ac39`

Modifications:

- Pruned source files and dependencies to keep only the core algorithm that converts an audio buffer into a Shazam fingerprint
- Added `lib.rs` as a bridge between Rust and Android code
- Replaced large buffer initializations with Vecs to prevent segmentation faults on Android background threads