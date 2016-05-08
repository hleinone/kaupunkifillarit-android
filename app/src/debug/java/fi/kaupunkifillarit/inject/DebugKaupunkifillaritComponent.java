package fi.kaupunkifillarit.inject;

import dagger.Component;
import fi.kaupunkifillarit.DebugKaupunkifillaritApplication;

@ApplicationScope
@Component(modules = {DebugServicesModule.class, SystemServicesModule.class})
public interface DebugKaupunkifillaritComponent extends KaupunkifillaritComponent {
    /**
     * An initializer that creates the graph from an application.
     */
    final class Initializer {
        public static DebugKaupunkifillaritComponent init(DebugKaupunkifillaritApplication app) {
            return DaggerDebugKaupunkifillaritComponent.builder()
                    .systemServicesModule(new SystemServicesModule(app))
                    .debugServicesModule(new DebugServicesModule(app))
                    .build();
        }

        private Initializer() {
        } // No instances.
    }
}
