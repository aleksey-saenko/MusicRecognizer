## Privacy policy

As a client application, Audile respects your privacy and doesn't track or profile its users. The app doesn't use ads, tracking and analytics services.

By default, while the app is open, it continuously records audio into a small circular in-memory buffer to reduce the time to the first match. This option can be disabled in settings, and in that case the app records audio only during a short recognition session at your request. All recorded audio is used solely for music recognition. If there is no internet connection or another error occurs, audio samples may be stored in app-private storage for deferred recognition attempts. This behavior can also be changed by selecting a different fallback policy in settings.

The app does use third-party services that may collect your personal information. When using these remote services, you agree to the terms of use of these services:

* [AudD](https://audd.io/) ([Terms of Service](https://audd.io/terms/), [Privacy Policy](https://audd.io/privacy/))

* [ACRCloud](https://www.acrcloud.com/) ([Terms of Service](https://www.acrcloud.com/terms/), [Privacy Policy](https://www.acrcloud.com/privacy/))

* [Shazam](https://www.shazam.com/company/) ([Terms of Service](https://www.shazam.com/terms), [Privacy Policy](https://www.apple.com/privacy))

* [Odesli](https://odesli.co/) ([Terms of Service](https://odesli.co/terms), [Privacy Policy](https://odesli.co/privacy))

For recognition purposes, Audile uses one of the remote services AudD, ACRCloud, or Shazam, as selected by the user. When AudD or ACRCloud is used, short audio recordings (3–10 seconds) are sent directly to the selected remote service for identification. When Shazam is used, the app converts the recorded audio on the device into compact audio fingerprints (signatures) and sends only those fingerprints to Shazam for matching; the original audio samples never leave the device and cannot be reconstructed from the fingerprints. No other sensitive information is shared. As Audile is intended to be an open-source application, it does not use any closed-source audio-fingerprinting libraries.

After a successful recognition, the app may make additional requests to external services, when necessary, to obtain extra information about the track. To retrieve additional platform-specific track links, the app uses the Odesli service or makes direct search requests to required music services. To retrieve song lyrics, the app uses the [LRCLIB](https://lrclib.net/) service.

Audile uses [ACRA](https://github.com/ACRA/acra) to generate crash reports in case of application failures. These reports contain detailed device and crash information, which can be useful for fixing issues. Reports are not sent automatically; instead, the app provides an option to email the report to developers, granting users full control over sharing their information.