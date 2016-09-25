Kaupunkifillarit.fi Android Application
=======================================

Getting Started
---------------

1. Install Android Studio
1. Obtain google-services.json and put it to app/src/debug/google-services.json
1. Obtain google_maps_api.xml and put it to app/src/debug/res/values/google_maps_api.xml 
1. Obtain tracker.xml and put it to app/src/main/res/xml/tracker.xml
1. Open project and run it

Releasing
---------

1. Obtain google-services.json and put it to app/src/release/google-services.json
1. Obtain google_maps_api.xml and put it to app/src/release/res/values/google_maps_api.xml
1. Obtain tracker.xml and put it to app/src/main/res/xml/tracker.xml
1. Obtain kaupunkifillarit.jks and put it in your filesystem
1. Create app/gradle.properties (see app/gradle.properties.example)
1. Run `./gradlew clean assembleRelease` on command line
1. Upload the created APK on Google Play Developer Console

TODO
----

* Report initial location to API/GA
* Translate to Swedish
* Translate to English
