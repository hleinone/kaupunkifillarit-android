package fi.kaupunkifillarit;

import android.app.Application;
import android.content.Context;
//import android.support.multidex.MultiDex;
//import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import fi.kaupunkifillarit.inject.KaupunkifillaritComponent;
import io.fabric.sdk.android.Fabric;

public class KaupunkifillaritApplication extends Application {//MultiDexApplication {
    protected KaupunkifillaritComponent component;

/*    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }*/

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics(), new Answers());
            // TODO Timber.plant(new CrashlyticsTree());
        }
        buildComponentAndInject();
    }

    protected void buildComponentAndInject() {
        component = KaupunkifillaritComponent.Initializer.init(this);
        component.inject(this);
    }

    public KaupunkifillaritComponent component() {
        return component;
    }

    public static KaupunkifillaritApplication get(Context context) {
        return (KaupunkifillaritApplication) context.getApplicationContext();
    }
}
