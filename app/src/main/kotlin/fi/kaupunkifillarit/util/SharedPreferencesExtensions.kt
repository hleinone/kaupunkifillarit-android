package fi.kaupunkifillarit.util

import android.content.SharedPreferences
import com.bluelinelabs.logansquare.LoganSquare
import com.bluelinelabs.logansquare.ParameterizedType
import rx.Observable

fun SharedPreferences.rx_getBoolean(key: String, defValue: Boolean): Observable<Boolean> = Observable.just(getBoolean(key, defValue))

fun SharedPreferences.rx_getFloat(key: String, defValue: Float): Observable<Float> = Observable.just(getFloat(key, defValue))

fun SharedPreferences.rx_getInt(key: String, defValue: Int): Observable<Int> = Observable.just(getInt(key, defValue))

fun SharedPreferences.rx_getLong(key: String, defValue: Long): Observable<Long> = Observable.just(getLong(key, defValue))

fun SharedPreferences.rx_getString(key: String, defValue: String?): Observable<String?> = Observable.just(getString(key, defValue))

fun SharedPreferences.rx_getStringSet(key: String, defValue: Set<String>?): Observable<Set<String>?> = Observable.just(getStringSet(key, defValue))

fun <T : Any> SharedPreferences.getObject(key: String, type: Class<T>, defValue: T?) = LoganSquare.parse(getString(key, null), type) ?: defValue

fun <T : Any> SharedPreferences.getObject(key: String, parameterizedType: ParameterizedType<T>, defValue: T?) = LoganSquare.parse(getString(key, null), parameterizedType) ?: defValue

fun <T : Any> SharedPreferences.rx_getObject(key: String, type: Class<T>, defValue: T?) = Observable.just(getObject(key, type, defValue))

fun <T : Any> SharedPreferences.rx_getObject(key: String, parameterizedType: ParameterizedType<T>, defValue: T?) = Observable.just(getObject(key, parameterizedType, defValue))
