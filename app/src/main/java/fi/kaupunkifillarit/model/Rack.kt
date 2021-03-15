package fi.kaupunkifillarit.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Rack(
        @SerialName("id")
        val id: String,
        @SerialName("name")
        val name: String,
        @SerialName("lat")
        val latitude: Double,
        @SerialName("lon")
        val longitude: Double,
        @SerialName("bikesAvailable")
        val bikes: Int) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val tram = other as Rack?

        return id == tram!!.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
