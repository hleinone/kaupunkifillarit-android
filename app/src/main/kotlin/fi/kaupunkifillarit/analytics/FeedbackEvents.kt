package fi.kaupunkifillarit.analytics

import android.content.DialogInterface

import com.google.android.gms.analytics.HitBuilders

import java.util.HashMap

object FeedbackEvents {
    private val CATEGORY = "Feedback"
    private val BUTTONS_TO_LABELS: MutableMap<Int, String>

    init {
        BUTTONS_TO_LABELS = HashMap<Int, String>(3)
        BUTTONS_TO_LABELS.put(DialogInterface.BUTTON_POSITIVE, "Rate")
        BUTTONS_TO_LABELS.put(DialogInterface.BUTTON_NEUTRAL, "Later")
        BUTTONS_TO_LABELS.put(DialogInterface.BUTTON_NEGATIVE, "No thanks")
    }

    fun showDialog(): Map<String, String> {
        return HitBuilders.EventBuilder().setCategory(CATEGORY).setAction(Events.ACTION_SHOW).setValue(1).build()
    }

    fun buttonClick(which: Int): Map<String, String> {
        return HitBuilders.EventBuilder().setCategory(CATEGORY).setAction(Events.ACTION_CLICK).setLabel(BUTTONS_TO_LABELS[which]).setValue(1).build()
    }
}
