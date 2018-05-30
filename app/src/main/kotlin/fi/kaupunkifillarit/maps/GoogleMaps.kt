package fi.kaupunkifillarit.maps

import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.*
import fi.kaupunkifillarit.R
import fi.kaupunkifillarit.model.MapLocation
import fi.kaupunkifillarit.rx.rx_getMapAsync
import io.reactivex.Single

object GoogleMaps {
    fun create(mapFragment: MapFragment): Single<Maps.MapWrapper<*, *>> {
        return mapFragment.rx_getMapAsync()
                .doOnSuccess { googleMap ->
                    val style = MapStyleOptions.loadRawResourceStyle(mapFragment.activity.applicationContext, R.raw.map_style)
                    googleMap.setMapStyle(style)
                }
                .map { MapsWrapper(it) }
    }

    class MapsWrapper(private val googleMap: GoogleMap) : Maps.MapWrapper<MarkerWrapper, MarkerOptionsWrapper> {
        override var myLocationEnabled: Boolean
            get() = googleMap.isMyLocationEnabled
            set(value) {
                @SuppressLint("MissingPermission")
                googleMap.isMyLocationEnabled = value
            }

        override var trafficEnabled: Boolean
            get() = googleMap.isTrafficEnabled
            set(value) {
                googleMap.isTrafficEnabled = value
            }

        override fun animateToMapLocation(mapLocation: MapLocation) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.Builder().target(LatLng(mapLocation.latitude, mapLocation.longitude)).zoom(mapLocation.zoom).bearing(mapLocation.bearing).tilt(mapLocation.tilt).build()))
        }

        override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
            googleMap.setPadding(left, top, right, bottom)
        }

        override fun setOnMarkerClickListener(listener: Maps.OnMarkerClickListener) {
            googleMap.setOnMarkerClickListener { marker: Marker -> listener.onMarkerClick(MarkerWrapper(marker)) }
        }

        override fun setOnMapLocationChangeListener(listener: Maps.OnMapLocationChangeListener) {
            googleMap.setOnCameraChangeListener { cameraPosition: CameraPosition ->
                listener.onMapLocationChange(
                        MapLocation(cameraPosition.target.latitude,
                                cameraPosition.target.longitude,
                                cameraPosition.zoom,
                                cameraPosition.bearing,
                                cameraPosition.tilt))
            }
        }

        override fun setOnMyLocationButtonClickListener(listener: Maps.OnMyLocationButtonClickListener) {
            googleMap.setOnMyLocationButtonClickListener { listener.onMyLocationButtonClick() }
        }

        override fun createMarkerOptions(): MarkerOptionsWrapper {
            return MarkerOptionsWrapper(MarkerOptions())
        }

        override fun addMarker(markerOptions: MarkerOptionsWrapper): MarkerWrapper {
            return MarkerWrapper(googleMap.addMarker(markerOptions.markerOptions))
        }
    }

    class MarkerOptionsWrapper internal constructor(override val markerOptions: MarkerOptions) : Maps.MarkerOptionsWrapper<MarkerOptions> {
        override fun icon(bitmap: Bitmap): Maps.MarkerOptionsWrapper<MarkerOptions> {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
            return this
        }

        override fun flat(flat: Boolean): Maps.MarkerOptionsWrapper<MarkerOptions> {
            markerOptions.flat(flat)
            return this
        }

        override fun position(latitude: Double, longitude: Double): Maps.MarkerOptionsWrapper<MarkerOptions> {
            markerOptions.position(LatLng(latitude, longitude))
            return this
        }

        override fun anchor(x: Float, y: Float): Maps.MarkerOptionsWrapper<MarkerOptions> {
            markerOptions.anchor(x, y)
            return this
        }
    }

    class MarkerWrapper internal constructor(override val marker: Marker) : Maps.MarkerWrapper<Marker> {
        override fun remove() {
            marker.remove()
        }
    }
}
