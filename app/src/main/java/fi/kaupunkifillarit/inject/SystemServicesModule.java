package fi.kaupunkifillarit.inject;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.GoogleApiAvailability;

import dagger.Module;
import dagger.Provides;
import fi.kaupunkifillarit.jackson.datatype.fj.FunctionalJavaModule;

@Module
public final class SystemServicesModule {
    private final Application application;

    public SystemServicesModule(Application application) {
        this.application = application;
    }

    @Provides
    @ApplicationScope
    Application provideApplication() {
        return application;
    }

    @Provides
    @ApplicationScope
    SharedPreferences providePreferenceManager() {
        return PreferenceManager.getDefaultSharedPreferences(application);
    }

    @Provides
    @ApplicationScope
    ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new FunctionalJavaModule());
        return objectMapper;
    }

    @Provides
    @ApplicationScope
    GoogleApiAvailability provideGoogleApiAvailability() {
        return GoogleApiAvailability.getInstance();
    }
}
