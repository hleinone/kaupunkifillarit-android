package fi.kaupunkifillarit.analytics

import com.google.android.gms.analytics.HitBuilders

object InfoDrawerEvents {
    private const val CATEGORY = "Info Drawer"

    fun open(): Map<String, String> {
        return HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_OPEN)
                .setValue(1)
                .build()
    }

    fun close(): Map<String, String> {
        return HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_CLOSE)
                .setValue(1)
                .build()
    }

    fun shareClick(): Map<String, String> {
        return HitBuilders.EventBuilder()
                .setCategory(Events.CATEGORY_SHARE)
                .setAction(Events.ACTION_CLICK)
                .setValue(1)
                .build()
    }
}
