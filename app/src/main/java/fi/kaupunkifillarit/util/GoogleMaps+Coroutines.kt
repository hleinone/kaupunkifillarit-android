package fi.kaupunkifillarit.util

import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Since offer() can throw when the channel is closed (channel can close before the
// block within awaitClose), wrap `offer` calls inside `runCatching`.
// See: https://github.com/Kotlin/kotlinx.coroutines/issues/974
private fun <E> SendChannel<E>.offerCatching(element: E): Boolean {
    return runCatching { offer(element) }.getOrDefault(false)
}

suspend inline fun SupportMapFragment.awaitMap(): GoogleMap =
    suspendCancellableCoroutine { continuation ->
        getMapAsync {
            continuation.resume(it)
        }
    }

@ExperimentalCoroutinesApi
fun GoogleMap.cameraMoveStarted(): Flow<Int> = callbackFlow {
    setOnCameraMoveStartedListener {
        offerCatching(it)
    }
    awaitClose {
        setOnCameraMoveStartedListener(null)
    }
}

@ExperimentalCoroutinesApi
fun GoogleMap.cameraIdle(): Flow<Unit> = callbackFlow {
    setOnCameraIdleListener {
        offerCatching(Unit)
    }
    awaitClose {
        setOnCameraIdleListener(null)
    }
}

@RequiresPermission(anyOf = ["android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"])
suspend inline fun FusedLocationProviderClient.awaitLastLocation(): Location? =
    suspendCancellableCoroutine { continuation ->
        lastLocation.addOnSuccessListener { location ->
            continuation.resume(location)
        }.addOnFailureListener { e ->
            continuation.resumeWithException(e)
        }.addOnCanceledListener {
            continuation.cancel()
        }
    }

