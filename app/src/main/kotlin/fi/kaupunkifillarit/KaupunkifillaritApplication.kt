package fi.kaupunkifillarit

import android.app.Application
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.google.android.gms.common.GoogleApiAvailability
import com.patloew.rxlocation.RxLocation
import io.fabric.sdk.android.Fabric

open class KaupunkifillaritApplication : Application() {
    val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    val googleApiAvailability: GoogleApiAvailability by lazy {
        GoogleApiAvailability.getInstance()
    }

    val rxLocation: RxLocation by lazy {
        RxLocation(this)
    }

    override fun onCreate() {
        super.onCreate()

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics(), Answers())
        }
    }
}
