package fi.kaupunkifillarit

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.android.gms.common.GoogleApiAvailability

open class KaupunkifillaritApplication : Application() {
    val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    val googleApiAvailability: GoogleApiAvailability by lazy {
        GoogleApiAvailability.getInstance()
    }
}

val Context.app: KaupunkifillaritApplication
    get() = applicationContext as KaupunkifillaritApplication
