# Videos@Home

The Videos@Home project is designed to stream, download, and cast high quality
videos to your notebooks, mobile devices, and televisions. Emphasis is placed
on simplicity, specifically targetting modern video formats with good hardware
decoding.

Features include:

* Streaming 1080p full Bluray quality video to your Android devices
* Downloading videos and subtitles to your device
* Casting to Google cast devices (including vtt subtitles)
* Automatic encoding of videos for downloading and casting

This software is intended to be used over your local (high bandwidth) network
only.

## Prerequisites

The following software is required to build this Android application:

* Java 6
* Android Studio 0.8.14
* Android SDK 21
* Google Cast Companion Library 1.11

The Cast Companion Library should be cloned from Github into a sibling
directory to this project so that it can be referenced.

(https://github.com/googlecast/CastCompanionLibrary-android)

## Usage

When the app is lauched for the first time, it will automatically discover the
running video server. Subsequent launches of the app will refresh the video
list, although the refresh button can be used to trigger the discovery process
and reload the available videos.

Tapping on a video will start playback either on the device by invoking your
preferred video player or on a Google cast device (Chromecast, Nexus Player) if
one is connected.

This app does not playback videos, but shares the video to applications that
support http streaming. (Some players will work well, others will not.)

Long press a video to select one or more for downloading. The Android Downloads
application will place the video and associated subtitle files into your
standard "Movies" directory. They can be easily accessed in your preferred
video player app or the Downloads app.

## License

Copyright © 2014 Jeff Hudren. All rights reserved.

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
