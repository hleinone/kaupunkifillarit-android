package fi.kaupunkifillarit.analytics;

import com.google.android.gms.analytics.HitBuilders;

import java.util.Map;

public abstract class MapsEvents {
    private static final String CATEGORY = "Maps";

    public static Map<String, String> myLocationClick() {
        return new HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_CLICK)
                .setLabel("My Location")
                .setValue(1)
                .build();
    }
}
