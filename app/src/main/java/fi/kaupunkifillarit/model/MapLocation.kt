package fi.kaupunkifillarit.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MapLocation(
        @SerialName("latitude")
        val latitude: Double,
        @SerialName("longitude")
        val longitude: Double,
        @SerialName("zoom")
        val zoom: Float,
        @SerialName("bearing")
        val bearing: Float,
        @SerialName("tilt")
        val tilt: Float) {

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