package fi.kaupunkifillarit.inject

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.android.gms.common.GoogleApiAvailability
import dagger.Module
import dagger.Provides
import fi.kaupunkifillarit.KaupunkifillaritApplication
import javax.inject.Singleton

@Module
class SystemServicesModule(private val application: KaupunkifillaritApplication) {
    @Provides
    @Singleton
    @ForApplication
    fun provideApplicationContext(): Context {
        return application
    }

    @Provides
    @Singleton
    fun providePreferenceManager(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }

    @Provides
    @Singleton
    fun provideGoogleApiAvailability(): GoogleApiAvailability {
        return GoogleApiAvailability.getInstance()
    }
}
