package fi.kaupunkifillarit.analytics

import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.common.GoogleApiAvailability

object ErrorEvents {
    private val CATEGORY = "Error"

    fun playServicesError(error: String): Map<String, String> {
        return HitBuilders.EventBuilder().setCategory(CATEGORY).setAction("Play Services").setLabel(error).set("Version", GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE.toString()).setValue(1).build()
    }
}
