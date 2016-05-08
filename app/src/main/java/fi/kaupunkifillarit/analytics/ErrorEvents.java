package fi.kaupunkifillarit.analytics;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Map;

public abstract class ErrorEvents {
    private static final String CATEGORY = "Error";

    public static Map<String, String> playServicesError(String error) {
        return new HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction("Play Services")
                .setLabel(error)
                .set("Version", String.valueOf(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE))
                .setValue(1)
                .build();
    }
}
