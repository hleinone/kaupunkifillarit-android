package fi.kaupunkifillarit.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Racks(
        @SerialName("bikeRentalStations")
        val racks: Set<Rack>)
