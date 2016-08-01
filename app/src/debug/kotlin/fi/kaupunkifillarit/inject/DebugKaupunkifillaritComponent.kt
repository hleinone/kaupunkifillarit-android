package fi.kaupunkifillarit.inject

import dagger.Component
import fi.kaupunkifillarit.DebugKaupunkifillaritApplication
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(DebugServicesModule::class, SystemServicesModule::class))
interface DebugKaupunkifillaritComponent : KaupunkifillaritComponent {
    /**
     * An initializer that creates the graph from an application.
     */
    object Initializer {
        fun init(application: DebugKaupunkifillaritApplication): DebugKaupunkifillaritComponent {
            return DaggerDebugKaupunkifillaritComponent.builder()
                    .systemServicesModule(SystemServicesModule(application))
                    .debugServicesModule(DebugServicesModule(application))
                    .build()
        }
    }
}
