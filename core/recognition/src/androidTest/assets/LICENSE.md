===============================================================================

Filename = ss-s16le-1c-16khz.wav

Original = "Sneaky Snitch" by Kevin MacLeod
https://incompetech.com/music/royalty-free/mp3-royaltyfree/Sneaky%20Snitch.mp3
Licensed under Creative Commons: By Attribution 4.0 License
http://creativecommons.org/licenses/by/4.0/

Modifications:

ffmpeg -i "Sneaky Snitch.mp3" -ss 00:01:00 -t 10 \
-acodec pcm_s16le -ar 16000 -ac 1 \
-map_metadata -1 \
-fflags +bitexact -flags:a +bitexact \
"ss-s16le-1c-16khz.wav" && \
sha256sum "ss-s16le-1c-16khz.wav"

===============================================================================