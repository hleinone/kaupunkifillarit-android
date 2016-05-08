package fi.kaupunkifillarit.inject;

import android.app.Application;

import com.crashlytics.android.answers.Answers;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.okhttp.OkHttpClient;

import dagger.Module;
import dagger.Provides;
import fi.kaupunkifillarit.R;

@Module
public class ServicesModule {
    private final Application application;

    public ServicesModule(Application application) {
        this.application = application;
    }

    @Provides
    @ApplicationScope
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient();
    }

    @Provides
    @ApplicationScope
    Tracker provideTracker() {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(application);
        return analytics.newTracker(R.xml.tracker);
    }

    @Provides
    @ApplicationScope
    Answers provideAnswers() {
        return Answers.getInstance();
    }
}
