package fi.kaupunkifillarit.rx

import android.content.Context
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import io.reactivex.Single
import io.reactivex.SingleEmitter

fun MapFragment.rx_getMapAsync(): Single<GoogleMap> =
        Single.create { source: SingleEmitter<GoogleMap> ->
            this.getMapAsync { googleMap ->
                if (googleMap != null) {
                    source.onSuccess(googleMap)
                } else {
                    source.onError(NullPointerException("Could not obtain googleMap"))
                }
            }
        }

fun GoogleApiAvailability.rx_isGooglePlayServicesAvailable(context: Context): Single<Int> {
    return Single.create<Int> { singleEmitter ->
        singleEmitter.onSuccess(isGooglePlayServicesAvailable(context))
    }
}