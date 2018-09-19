[Kaupunkifillarit.fi](https://kaupunkifillarit.fi) - Helsinki City Bikes Android app
====================================================================================

City bikes rolled to the streets of Helsinki in the summer of 2016. They gained popularity quickly.

This popularity was a nuisance so we made a service to check the availability status on all bike stations. The contributors are: Sampsa Kuronen ([backend + web](https://github.com/sampsakuronen/kaupunkifillarit-web)), Antero Päärni (gfx), Lauri Piispanen ([iOS](https://github.com/lauripiispanen/kaupunkifillarit-ios)) and Hannu Leinonen ([Android](https://github.com/hleinone/kaupunkifillarit-android)).

The data is open data provided by [HSL](http://dev.hsl.fi/).

Getting Started
---------------

1. Install Android Studio
1. Obtain google-services.json and put it to app/src/debug/google-services.json
1. Obtain google_maps_api.xml and put it to app/src/debug/res/values/google_maps_api.xml 
1. Open project and run it

Releasing
---------

1. Obtain google-services.json and put it to app/src/release/google-services.json
1. Obtain google_maps_api.xml and put it to app/src/release/res/values/google_maps_api.xml
1. Obtain kaupunkifillarit.jks and put it in your filesystem
1. Create app/gradle.properties (see app/gradle.properties.example)
1. Run `./gradlew clean assembleRelease` on command line
1. Upload the created APK on Google Play Developer Console

TODO
----

* Translate to Swedish
* Translate to English

Licence
-------

The MIT License (MIT)

Copyright (c) 2016 Hannu Leinonen

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
