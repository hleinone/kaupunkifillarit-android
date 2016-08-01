package fi.kaupunkifillarit.util

import android.content.SharedPreferences
import com.bluelinelabs.logansquare.LoganSquare
import com.bluelinelabs.logansquare.ParameterizedType
import rx.Observable
import rx.lang.kotlin.observable

fun SharedPreferences.rx_getBoolean(key: String, defValue: Boolean): Observable<String> = observable { getBoolean(key, defValue) }

fun SharedPreferences.rx_getFloat(key: String, defValue: Float): Observable<String> = observable { getFloat(key, defValue) }

fun SharedPreferences.rx_getInt(key: String, defValue: Int): Observable<String> = observable { getInt(key, defValue) }

fun SharedPreferences.rx_getLong(key: String, defValue: Long): Observable<String> = observable { getLong(key, defValue) }

fun SharedPreferences.rx_getString(key: String, defValue: String): Observable<String> = observable { getString(key, defValue) }

fun SharedPreferences.rx_getStringSet(key: String, defValue: Set<String>): Observable<String> = observable { getStringSet(key, defValue) }

fun <T : Any> SharedPreferences.rx_object(key: String, type: Class<T>) = rx_getString(key, "").map { LoganSquare.parse(it, type) }

fun <T : Any> SharedPreferences.rx_object(key: String, parameterizedType: ParameterizedType<T>) = rx_getString(key, "").map { LoganSquare.parse(it, parameterizedType) }
