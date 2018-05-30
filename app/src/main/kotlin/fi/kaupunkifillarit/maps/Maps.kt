package fi.kaupunkifillarit.maps

import android.graphics.Bitmap

import fi.kaupunkifillarit.model.MapLocation

abstract class Maps {
    interface MapWrapper<out MW : Maps.MarkerWrapper<*>, MOW : Maps.MarkerOptionsWrapper<*>> {
        var myLocationEnabled: Boolean

        var trafficEnabled: Boolean

        fun animateToMapLocation(mapLocation: MapLocation)

        fun setPadding(left: Int, top: Int, right: Int, bottom: Int)

        fun setOnMarkerClickListener(listener: OnMarkerClickListener)

        fun setOnMapLocationChangeListener(listener: OnMapLocationChangeListener)

        fun setOnMyLocationButtonClickListener(listener: OnMyLocationButtonClickListener)

        fun createMarkerOptions(): MOW

        fun addMarker(markerOptions: MOW): MW
    }

    interface MarkerWrapper<out T> {
        val marker: T

        fun remove()
    }

    interface MarkerOptionsWrapper<T> {
        val markerOptions: T

        fun icon(bitmap: Bitmap): MarkerOptionsWrapper<T>

        fun flat(flat: Boolean): MarkerOptionsWrapper<T>

        fun position(latitude: Double, longitude: Double): MarkerOptionsWrapper<T>

        fun anchor(x: Float, y: Float): MarkerOptionsWrapper<T>
    }

    interface OnMarkerClickListener {
        fun onMarkerClick(marker: MarkerWrapper<*>): Boolean
    }

    interface OnMapLocationChangeListener {
        fun onMapLocationChange(mapLocation: MapLocation)
    }

    interface OnMyLocationButtonClickListener {
        fun onMyLocationButtonClick(): Boolean
    }
}
