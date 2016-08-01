package fi.kaupunkifillarit.inject

import dagger.Component
import fi.kaupunkifillarit.KaupunkifillaritApplication
import fi.kaupunkifillarit.MapActivity
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(SystemServicesModule::class, ServicesModule::class))
interface KaupunkifillaritComponent {
    fun inject(application: KaupunkifillaritApplication)

    fun inject(activity: MapActivity)

    /**
     * An initializer that creates the graph from an application.
     */
    object Initializer {
        fun init(application: KaupunkifillaritApplication): KaupunkifillaritComponent {
            return DaggerKaupunkifillaritComponent.builder()
                    .systemServicesModule(SystemServicesModule(application))
                    .servicesModule(ServicesModule(application))
                    .build()
        }
    }
}

