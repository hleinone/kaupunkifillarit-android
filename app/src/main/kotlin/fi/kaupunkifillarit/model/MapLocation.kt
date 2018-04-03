package fi.kaupunkifillarit.model

import android.location.Location
import com.bluelinelabs.logansquare.annotation.JsonField
import com.bluelinelabs.logansquare.annotation.JsonObject

@JsonObject
class MapLocation {
    @JsonField
    var latitude: Double = 0.0
    @JsonField
    var longitude: Double = 0.0
    @JsonField
    var zoom: Float = 0f
    @JsonField
    var bearing: Float = 0f
    @JsonField
    var tilt: Float = 0f

    constructor()

    constructor(latitude: Double,
                longitude: Double,
                zoom: Float,
                bearing: Float,
                tilt: Float) {
        this.latitude = latitude
        this.longitude = longitude
        this.zoom = zoom
        this.bearing = bearing
        this.tilt = tilt
    }

    val isWithinDesiredMapBounds: Boolean
        get() = latitude > 60.151568 &&
                latitude < 60.194072 &&
                longitude > 24.903618 &&
                longitude < 24.984335

    override fun toString(): String {
        return "MapLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", zoom=" + zoom +
                ", bearing=" + bearing +
                ", tilt=" + tilt +
                '}'
    }
}

fun Location.isWithinDesiredMapBounds(): Boolean {
    return latitude > 60.139118 &&
            latitude < 60.229646 &&
            longitude > 24.689006 &&
            longitude < 25.017768
}