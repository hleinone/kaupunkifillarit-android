package fi.kaupunkifillarit.analytics;

import com.google.android.gms.analytics.HitBuilders;

import java.util.Map;

public abstract class LocationPermissionsEvents {
    private static final String CATEGORY = "Location Permissions";

    public static Map<String, String> granted() {
        return new HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_PERMISSION_GRANTED)
                .setValue(1)
                .build();
    }

    public static Map<String, String> denied() {
        return new HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_PERMISSION_DENIED)
                .setValue(1)
                .build();
    }
}
