package fi.kaupunkifillarit.inject;

import fi.kaupunkifillarit.KaupunkifillaritApplication;
import fi.kaupunkifillarit.MapActivity;

public interface KaupunkifillaritGraph {
    void inject(KaupunkifillaritApplication app);

    void inject(MapActivity activity);
}
