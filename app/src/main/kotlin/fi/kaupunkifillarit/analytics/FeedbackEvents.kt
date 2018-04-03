package fi.kaupunkifillarit.analytics

import android.content.DialogInterface
import com.google.android.gms.analytics.HitBuilders

object FeedbackEvents {
    private const val CATEGORY = "Feedback"
    private val BUTTONS_TO_LABELS = mapOf(
            Pair(DialogInterface.BUTTON_POSITIVE, "Rate"),
            Pair(DialogInterface.BUTTON_NEUTRAL, "Later"),
            Pair(DialogInterface.BUTTON_NEGATIVE, "No thanks"))

    fun showDialog(): Map<String, String> {
        return HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_SHOW)
                .setValue(1)
                .build()
    }

    fun buttonClick(which: Int): Map<String, String> {
        return HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_CLICK)
                .setLabel(BUTTONS_TO_LABELS[which])
                .setValue(1)
                .build()
    }
}
