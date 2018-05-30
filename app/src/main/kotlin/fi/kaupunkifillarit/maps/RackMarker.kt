package fi.kaupunkifillarit.maps

class RackMarker internal constructor(private val marker: Maps.MarkerWrapper<*>) {
    fun remove() {
        marker.remove()
    }
}
