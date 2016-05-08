package fi.kaupunkifillarit.analytics;

import android.content.DialogInterface;

import com.google.android.gms.analytics.HitBuilders;

import java.util.HashMap;
import java.util.Map;

public abstract class FeedbackEvents {
    private static final String CATEGORY = "Feedback";
    private static final Map<Integer, String> BUTTONS_TO_LABELS;

    static {
        BUTTONS_TO_LABELS = new HashMap<>(3);
        BUTTONS_TO_LABELS.put(DialogInterface.BUTTON_POSITIVE, "Rate");
        BUTTONS_TO_LABELS.put(DialogInterface.BUTTON_NEUTRAL, "Later");
        BUTTONS_TO_LABELS.put(DialogInterface.BUTTON_NEGATIVE, "No thanks");
    }

    public static Map<String, String> showDialog() {
        return new HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_SHOW)
                .setValue(1)
                .build();
    }

    public static Map<String, String> buttonClick(int which) {
        return new HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_CLICK)
                .setLabel(BUTTONS_TO_LABELS.get(which))
                .setValue(1)
                .build();
    }
}
