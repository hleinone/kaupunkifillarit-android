package fi.kaupunkifillarit.inject;

import android.app.Application;

import com.crashlytics.android.answers.Answers;
//import com.facebook.stetho.okhttp.StethoInterceptor;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import fi.kaupunkifillarit.R;

@Module
public class DebugServicesModule {
    private final Application application;

    public DebugServicesModule(Application application) {
        this.application = application;
    }

    @Provides
    @ApplicationScope
    OkHttpClient provideOkHttpClient() {
        OkHttpClient client = new OkHttpClient();
//        client.networkInterceptors().add(new StethoInterceptor());
        return client;
    }

    @Provides
    @ApplicationScope
    Tracker provideTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(application);
        analytics.setDryRun(true);
        analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
        return analytics.newTracker(R.xml.tracker);
    }

    @Provides
    @ApplicationScope
    Answers provideAnswers() {
        return new Answers();
    }
}
