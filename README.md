# TuneSurfer

TuneSurfer is the music recognizing application, that can help you quickly and accurately recognize a music track playing nearby you.
This application uses the AudD® service as a Music Recognition API.

I'm developing this application as the project where I learn how to build complex applications using the best coding practices.
At the same time, I try to make the application reliable for everyday use.

## Features

* **Recognition** - The application allows you to perform song recognition in one click. The app will save the recording if there is no internet, and the recognition process will be executed when you come back online. You can customize the default behavior for unsuccessful recognition (no internet, no matches, another failure) via the preferences.
* **Notification service** - You can use the application from the notification drawer. Manage the recognition process and receive information about the track.
* **About track** - In case of successful recognition, information about the track, such as the name, artist, album and year, artwork, links to this track on popular music services, lyrics is provided.
* **Library** - All recognized tracks are stored in the application's library. You can filter the track list, create a favorites collection, search for tracks, or delete tracks.
* **Preferences** - The application has a number of options for customizing its behavior and appearance, which can be found on the settings screen.

## Tech

TuneSurfer is written in Kotlin and runs on Android 8.0 or higher.  
App architecture inspired by clean arch practices. The application is modularized with a feature-based approach (definitely overkill for such small app).  
UI completely written in Jetpack Compose with MVVM pattern in Material3 design.  
The app uses the AudioRecord and MediaCodec API for audio recording and encoding, which allows to create recording strategies and audio processing of any complexity.  
Stack: Kotlin, Coroutines, Jetpack Compose, Hilt, WorkManager, Room, OkHttp, Moshi, DataStore, Coil, Junit.  

## Screenshots

[<img src="./readme_img/0_recognition_screen.png" width=200>](./readme_img/0_recognition_screen.png)
[<img src="./readme_img/1_track_screen.png" width=200>](./readme_img/1_track_screen.png)
[<img src="./readme_img/2_library_screen.png" width=200>](./readme_img/2_library_screen.png)
[<img src="./readme_img/3_library_filter.png" width=200>](./readme_img/3_library_filter.png)
[<img src="./readme_img/4_library_search.png" width=200>](./readme_img/4_library_search.png)
[<img src="./readme_img/5_notification_service.png" width=200>](./readme_img/5_notification_service.png)
[<img src="./readme_img/6_queue_screen.png" width=200>](./readme_img/6_queue_screen.png)
[<img src="./readme_img/7_preferences_screen.png" width=200>](./readme_img/7_preferences_screen.png)

## API Key

This application uses the AudD® service as a Music Recognition API. You need a special API token provided by AudD® to use the application. If you don't have one, you can sign up for a free API token.
You can add the key on the onboarding or preferences screen, or just set it in `local.properties`.

## License

Copyright (C) 2023 [Aleksey Saenko]

The license is [GNU GPLv3](LICENSE.md).
