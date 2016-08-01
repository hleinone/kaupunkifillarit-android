package fi.kaupunkifillarit.maps

import fi.kaupunkifillarit.model.Rack

class RackMarker internal constructor(private val marker: Maps.MarkerWrapper<*>) {

    fun update(rack: Rack) {
        marker.setPosition(rack.latitude, rack.longitude)
    }
}
