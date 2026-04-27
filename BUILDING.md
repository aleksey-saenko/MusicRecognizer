# Building Audile

These instructions are intended for Debian-based systems. Please adapt them for your operating system if needed.

## Prerequisites

First, build the FFTW 3.3.10 static library.

### Install dependencies

```bash
sudo apt update
sudo apt install -y sdkmanager build-essential curl tar gzip git
```

### Install the required NDK version

If NDK **29.0.14206865** is not installed:

```bash
mkdir -p "$HOME/Android/Sdk"
export ANDROID_HOME="$HOME/Android/Sdk"
sdkmanager "ndk;29.0.14206865"
```

### Clone the app repository and build FFTW
```bash
git clone https://github.com/aleksey-saenko/MusicRecognizer.git
cd ./MusicRecognizer
pushd ./core/recognition/src/main/cpp/vibrafp/third_party
chmod +x ./build-fftw-android.sh
./build-fftw-android.sh --ndk "$ANDROID_HOME/ndk/29.0.14206865"
popd
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
