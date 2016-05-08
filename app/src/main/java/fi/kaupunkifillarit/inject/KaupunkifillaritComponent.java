package fi.kaupunkifillarit.inject;

import dagger.Component;
import fi.kaupunkifillarit.KaupunkifillaritApplication;

@ApplicationScope
@Component(modules = {SystemServicesModule.class, ServicesModule.class})
public interface KaupunkifillaritComponent extends KaupunkifillaritGraph {
    /**
     * An initializer that creates the graph from an application.
     */
    final class Initializer {
        public static KaupunkifillaritComponent init(KaupunkifillaritApplication app) {
            return DaggerKaupunkifillaritComponent.builder()
                    .systemServicesModule(new SystemServicesModule(app))
                    .servicesModule(new ServicesModule(app))
                    .build();
        }

        private Initializer() {
        } // No instances.
    }
}

