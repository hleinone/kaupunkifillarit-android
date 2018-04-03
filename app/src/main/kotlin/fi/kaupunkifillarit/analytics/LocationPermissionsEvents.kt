package fi.kaupunkifillarit.analytics

import com.google.android.gms.analytics.HitBuilders

object LocationPermissionsEvents {
    private const val CATEGORY = "Location Permissions"

    fun granted(): Map<String, String> {
        return HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_PERMISSION_GRANTED)
                .setValue(1)
                .build()
    }

    fun denied(): Map<String, String> {
        return HitBuilders.EventBuilder()
                .setCategory(CATEGORY)
                .setAction(Events.ACTION_PERMISSION_DENIED)
                .setValue(1)
                .build()
    }
}
