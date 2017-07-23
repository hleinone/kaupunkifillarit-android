package fi.kaupunkifillarit.util

import android.content.SharedPreferences
import com.bluelinelabs.logansquare.LoganSquare
import com.bluelinelabs.logansquare.ParameterizedType
import io.reactivex.Observable

fun <T : Any> SharedPreferences.getObject(key: String, type: Class<T>, defValue: T?) = getString(key, null).map { LoganSquare.parse(it, type) } ?: defValue

fun <T : Any> SharedPreferences.getObject(key: String, parameterizedType: ParameterizedType<T>, defValue: T?) = getString(key, null).map { LoganSquare.parse(it, parameterizedType) } ?: defValue

fun <T : Any> SharedPreferences.Editor.putObject(key: String, value: T?) = putString(key, value.map { LoganSquare.serialize(it) })

fun SharedPreferences.rx_getBoolean(key: String, defValue: Boolean): Observable<Boolean> = Observable.just(getBoolean(key, defValue))

fun SharedPreferences.rx_getFloat(key: String, defValue: Float): Observable<Float> = Observable.just(getFloat(key, defValue))

fun SharedPreferences.rx_getInt(key: String, defValue: Int): Observable<Int> = Observable.just(getInt(key, defValue))

fun SharedPreferences.rx_getLong(key: String, defValue: Long): Observable<Long> = Observable.just(getLong(key, defValue))

fun SharedPreferences.rx_getString(key: String, defValue: String?): Observable<String> {
    val s = getString(key, defValue)
    if (s != null) {
        return Observable.just(s)
    } else {
        return Observable.empty()
    }
}

fun SharedPreferences.rx_getStringSet(key: String, defValue: Set<String>?): Observable<Set<String>> {
    val s = getStringSet(key, defValue)
    if (s != null) {
        return Observable.just(s)
    } else {
        return Observable.empty()
    }
}

fun <T : Any> SharedPreferences.rx_getObject(key: String, type: Class<T>, defValue: T?): Observable<T> {
    val o = getObject(key, type, defValue)
    if (o != null) {
        return Observable.just(o)
    } else {
        return Observable.empty()
    }
}

fun <T : Any> SharedPreferences.rx_getObject(key: String, parameterizedType: ParameterizedType<T>, defValue: T?): Observable<T> {
    val o = getObject(key, parameterizedType, defValue)
    if (o != null) {
        return Observable.just(o)
    } else {
        return Observable.empty()
    }
}

