package fi.kaupunkifillarit.maps

import android.graphics.Bitmap

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

import fi.kaupunkifillarit.model.MapLocation
import rx.Observable
import rx.Subscriber

object GoogleMaps {
    fun create(mapFragment: MapFragment): Observable<Maps.MapWrapper<*, *>> {
        return Observable.create { subscriber: Subscriber<in Maps.MapWrapper<*, *>> ->
            mapFragment.getMapAsync { googleMap ->
                if (googleMap != null) {
                    subscriber.onNext(MapsWrapper(googleMap))
                } else {
                    subscriber.onError(NullPointerException("Could not obtain googleMap"))
                }
            }
        }
    }

    class MapsWrapper : Maps.MapWrapper<MarkerWrapper, MarkerOptionsWrapper> {
        private val googleMap: GoogleMap

        override var myLocationEnabled: Boolean
            get() = googleMap.isMyLocationEnabled
            set(value) {
                //noinspection MissingPermission
                googleMap.isMyLocationEnabled = value
            }

        override var trafficEnabled: Boolean
            get() = googleMap.isTrafficEnabled
            set(value) {
                googleMap.isTrafficEnabled = value
            }

        constructor(googleMap: GoogleMap) {
            this.googleMap = googleMap
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

    class MarkerOptionsWrapper : Maps.MarkerOptionsWrapper<MarkerOptions> {
        override val markerOptions: MarkerOptions

        internal constructor(markerOptions: MarkerOptions) {
            this.markerOptions = markerOptions
        }

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

    class MarkerWrapper : Maps.MarkerWrapper<Marker> {
        override val marker: Marker

        internal constructor(marker: Marker) {
            this.marker = marker
        }

        override fun setPosition(latitude: Double, longitude: Double) {
            marker.position = LatLng(latitude, longitude)
        }
    }
}
