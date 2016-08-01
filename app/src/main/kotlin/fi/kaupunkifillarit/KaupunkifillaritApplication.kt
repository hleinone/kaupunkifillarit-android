package fi.kaupunkifillarit

import android.app.Application
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import fi.kaupunkifillarit.inject.KaupunkifillaritComponent
import io.fabric.sdk.android.Fabric

open class KaupunkifillaritApplication : Application() {
    lateinit var component: KaupunkifillaritComponent

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics(), Answers())
            // TODO Timber.plant(new CrashlyticsTree());
        }

        inject()
    }

    protected open fun inject() {
        component = KaupunkifillaritComponent.Initializer.init(this)
        component.inject(this)
    }
}
