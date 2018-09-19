package fi.kaupunkifillarit

import timber.log.Timber

class DebugKaupunkifillaritApplication : KaupunkifillaritApplication() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
