# Audile

Audile is an open-source music recognition application, that can help you quickly and accurately recognize a music track playing nearby you.
This application uses [AudD](https://audd.io/) and [ACRCloud](https://www.acrcloud.com/) services to perform song identification and [Odesli](https://odesli.co/) service to retrieve additional platform-specific track links.

[<img src="./img/get-it-on-f-droid.png" alt="Get it on F-Droid" height="80">](https://f-droid.org/en/packages/com.mrsep.musicrecognizer/)
[<img src="./img/get-it-on-github.png" alt="Get it on GitHub" height="80">](https://github.com/aleksey-saenko/MusicRecognizer/releases/)

F-Droid releases cannot be upgraded to other releases as they are compiled and signed by [F-Droid](https://f-droid.org/docs/).

The app runs on Android 8.0 or later.

## Features

* **Recognition** - Audile allows you to perform song recognition in one click. The app will save the recording if there is no internet, and the recognition process will be executed when you come back online. You can customize the default behavior for unsuccessful recognition (no internet, no matches, another failure) via the preferences.
* **Background Usage** - Use the application seamlessly from the notification drawer, home screen widget, or quick settings tile. Manage the recognition process and obtain track information without launching the app.
* **About track** - In case of successful recognition, information about the track, such as the name, artist, album and year, artwork, links to this track on popular music services, lyrics is provided.
* **Library** - All recognized tracks are stored in the application's library. You can filter the track list, create a favorites collection, search for tracks, or delete tracks.
* **Preferences** - The application has a number of options for customizing its behavior and appearance, which can be found on the settings screen.

## Screenshots
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/00.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/00.png "Recognition screen")
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/01.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/01.png "Track screen")
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/02.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/02.png "Library screen")
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/03.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/03.png "Notification service")
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/04.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/04.png "Lyrics screen")
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/05.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/05.png "Library search")
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/06.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/06.png "Queue screen")
[<img src="./fastlane/metadata/android/en-US/images/phoneScreenshots/07.png" width=200>](./fastlane/metadata/android/en-US/images/phoneScreenshots/07.png "Preferences screen")

## API Key

<details open><summary>AudD</summary><p>

The app uses AudD as the default recognition provider. Please note that AudD is a paid service, and you will need an API token to use this application. If you don't have one, you can [sign up](https://dashboard.audd.io/) for a 14-day trial token.

You can also use the app without a token, but this will significantly restrict the number of daily recognitions that can be performed. Please keep in mind that this behavior is not guaranteed by the service and can be restricted at any time.

</p></details>

<details><summary>ACRCloud</summary><p>

ACRCloud is a paid service that offers a free trial period and ongoing limited free usage for development. To use this service, you must create an account and register a project. If you encounter any difficulties, please refer to this [guide](https://docs.acrcloud.com/tutorials/recognize-music/).

Please pay attention to these steps:
* Select the geographical region closest to you to minimize internet latency.
* Set the audio source as `Recorded audio`.
* Set the audio engine of your choice. `Audio fingerprinting` is the most accurate for general use.
* Check all the boxes for 3rd party ID integration.

</p></details>

## Translation

[<img src="https://hosted.weblate.org/widget/audile/multi-auto.svg" alt="Translation progress">](https://hosted.weblate.org/engage/audile/)

If you'd like to help translate the app into your language, you can do so through [Weblate](https://hosted.weblate.org/engage/audile/).

Feel free to correct any errors or suggest better translation options for the current translations.

## License

```
Copyright (C) 2023-2025 Aleksey Saenko

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```
