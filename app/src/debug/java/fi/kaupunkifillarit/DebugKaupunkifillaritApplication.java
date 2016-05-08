package fi.kaupunkifillarit;

//import com.facebook.stetho.Stetho;

import fi.kaupunkifillarit.inject.DebugKaupunkifillaritComponent;
import timber.log.Timber;

public class DebugKaupunkifillaritApplication extends KaupunkifillaritApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
/*        Stetho.initialize(
                Stetho.newInitializerBuilder(this)
                        .enableDumpapp(
                                Stetho.defaultDumperPluginsProvider(this))
                        .enableWebKitInspector(
                                Stetho.defaultInspectorModulesProvider(this))
                        .build());*/
    }

    @Override
    protected void buildComponentAndInject() {
        component = DebugKaupunkifillaritComponent.Initializer.init(this);
        component.inject(this);
    }
}
