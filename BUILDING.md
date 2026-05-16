# Building Audile

These instructions are intended for Debian-based systems. Adapt them for your operating system if needed.

## Prerequisites

### Install Rust and cargo-ndk

Install Rust with `rustup`, which is the [recommended installation method](https://rust-lang.org/tools/install/):

```bash
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
source ~/.cargo/env
cargo install cargo-ndk@4.1.2 --locked
```

### Install the required Android NDK version

The app uses NDK version **29.0.14206865**.

If you use Android Studio, you can install the NDK from **Tools → SDK Manager → SDK Tools**.

Or install it with `sdkmanager`:

```bash
sudo apt update && sudo apt install -y sdkmanager
mkdir -p "$HOME/Android/Sdk"
export ANDROID_HOME="$HOME/Android/Sdk"
sdkmanager --install "ndk;29.0.14206865"
```

## Build the app with Android Studio

You can now build the app in Android Studio like any other project.

## Build without Android Studio

Install Java 21 or later:

```bash
sudo apt install -y openjdk-21-jdk-headless
```

Accept the Android SDK licenses:

```bash
yes | sdkmanager --licenses
```

Build the debug variant:

```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

To build the release variant, make sure your signing config is set up, then run:

```bash
./gradlew assembleRelease
```
