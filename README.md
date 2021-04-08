[Kaupunkifillarit.fi](https://kaupunkifillarit.fi) - Helsinki City Bikes Android app
====================================================================================

City bikes rolled to the streets of Helsinki in the summer of 2016. They gained popularity quickly. This popularity was a nuisance so we made a service to check the availability status on all bike stations.

The data is open data provided by [HSL](https://www.hsl.fi/hsl/avoin-data), [Föli](https://www.foli.fi/fi/avoin-data) and [Oulun liikenne](https://wp.oulunliikenne.fi/avoin-data/).

Getting Started
---------------

1. Install Android Studio
1. Obtain google-services.json and put it to app/google-services.json
1. Obtain google_maps_api.xml and put it to app/src/debug/res/values/google_maps_api.xml 
1. Open project and run it

Releasing
---------

1. Obtain google_maps_api.xml and put it to app/src/release/res/values/google_maps_api.xml
1. Obtain kaupunkifillarit.jks and put it in your filesystem
1. Create app/gradle.properties (see app/gradle.properties.example)
1. Run `./gradlew clean assembleRelease` on command line
1. Upload the created APK on Google Play Developer Console

TODO
----

* Translate to Swedish
* Translate to English
* Use AAB when uploading to Play Store
* Dark theme

License
-------

The MIT License (MIT)

Copyright (c) 2016-2021 Hannu Leinonen

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
