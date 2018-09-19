package fi.kaupunkifillarit.maps

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import fi.kaupunkifillarit.R
import fi.kaupunkifillarit.rx.rx_getMapAsync
import io.reactivex.Single

object GoogleMaps {
    fun create(mapFragment: MapFragment): Single<GoogleMap> {
        return mapFragment.rx_getMapAsync()
                .doOnSuccess { googleMap ->
                    val style = MapStyleOptions.loadRawResourceStyle(mapFragment.activity.applicationContext, R.raw.map_style)
                    googleMap.setMapStyle(style)
                }
    }
}
