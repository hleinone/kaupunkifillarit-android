package fi.kaupunkifillarit.util

import android.content.SharedPreferences
import com.bluelinelabs.logansquare.LoganSquare
import com.bluelinelabs.logansquare.ParameterizedType
import io.reactivex.Maybe
import io.reactivex.Single

fun SharedPreferences.rx_getBoolean(key: String, defValue: Boolean): Single<Boolean> = Single.just(getBoolean(key, defValue))

fun SharedPreferences.rx_getFloat(key: String, defValue: Float): Single<Float> = Single.just(getFloat(key, defValue))

fun SharedPreferences.rx_getInt(key: String, defValue: Int): Single<Int> = Single.just(getInt(key, defValue))

fun SharedPreferences.rx_getLong(key: String, defValue: Long): Single<Long> = Single.just(getLong(key, defValue))

fun SharedPreferences.rx_getString(key: String, defValue: String): Single<String> = Single.just(getString(key, defValue))

fun SharedPreferences.rx_getStringSet(key: String, defValue: Set<String>): Single<Set<String>> = Single.just(getStringSet(key, defValue))

fun SharedPreferences.rx_getString(key: String): Maybe<String> = Maybe.create { maybeEmitter ->
    getString(key, null)?.let {
        maybeEmitter.onSuccess(it)
    }
    maybeEmitter.onComplete()
}

fun SharedPreferences.rx_getStringSet(key: String): Maybe<Set<String>> = Maybe.create { maybeEmitter ->
    getStringSet(key, null)?.let {
        maybeEmitter.onSuccess(it)
    }
    maybeEmitter.onComplete()
}

fun <T : Any> SharedPreferences.getObject(key: String, type: Class<T>, defValue: T?) = getString(key, null)?.let { LoganSquare.parse(it, type) }
        ?: defValue

fun <T : Any> SharedPreferences.getObject(key: String, parameterizedType: ParameterizedType<T>, defValue: T?) = getString(key, null)?.let { LoganSquare.parse(it, parameterizedType) }
        ?: defValue

fun <T : Any> SharedPreferences.Editor.putObject(key: String, value: T) = putString(key, LoganSquare.serialize(value))

fun <T : Any> SharedPreferences.rx_getObject(key: String, type: Class<T>, defValue: T): Single<T> = Single.just(getObject(key, type, defValue))

fun <T : Any> SharedPreferences.rx_getObject(key: String, parameterizedType: ParameterizedType<T>, defValue: T): Single<T> = Single.just(getObject(key, parameterizedType, defValue))

fun <T : Any> SharedPreferences.rx_getObject(key: String, type: Class<T>): Maybe<T> = Maybe.create { maybeEmitter ->
    getObject(key, type, null)?.let {
        maybeEmitter.onSuccess(it)
    }
    maybeEmitter.onComplete()
}

fun <T : Any> SharedPreferences.rx_getObject(key: String, parameterizedType: ParameterizedType<T>): Maybe<T> = Maybe.create { maybeEmitter ->
    getObject(key, parameterizedType, null)?.let {
        maybeEmitter.onSuccess(it)
    }
    maybeEmitter.onComplete()
}
