package fi.kaupunkifillarit.analytics;

import com.google.android.gms.analytics.HitBuilders;

import java.util.Map;

public abstract class InfoDrawerEvents {
    private static final String CATEGORY = "Info Drawer";

    public static Map<String, String> open() {
        return new HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_OPEN)
                .setValue(1)
                .build();
    }

    public static Map<String, String> close() {
        return new HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_CLOSE)
                .setValue(1)
                .build();
    }

    public static Map<String, String> shareClick() {
        return new HitBuilders.EventBuilder()
                .setCategory(Events.CATEGORY_SHARE)
                .setAction(Events.ACTION_CLICK)
                .setValue(1)
                .build();
    }
}
