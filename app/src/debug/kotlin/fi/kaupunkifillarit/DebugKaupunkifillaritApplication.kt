package fi.kaupunkifillarit

import fi.kaupunkifillarit.inject.DebugKaupunkifillaritComponent
import timber.log.Timber

class DebugKaupunkifillaritApplication : KaupunkifillaritApplication() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    override fun inject() {
        component = DebugKaupunkifillaritComponent.Initializer.init(this)
        component.inject(this)
    }
}
