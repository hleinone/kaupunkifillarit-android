package fi.kaupunkifillarit.analytics

import com.google.android.gms.analytics.HitBuilders

object MapsEvents {
    private val CATEGORY = "Maps"

    fun myLocationClick(): Map<String, String> {
        return HitBuilders.EventBuilder().setCategory(CATEGORY).setAction(Events.ACTION_CLICK).setLabel("My Location").setValue(1).build()
    }
}
