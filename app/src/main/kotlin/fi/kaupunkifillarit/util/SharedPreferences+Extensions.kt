package fi.kaupunkifillarit.util

import android.content.SharedPreferences
import io.reactivex.Maybe
import io.reactivex.Single
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JSON

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

fun <T : Any> SharedPreferences.getObject(key: String, deserializer: KSerializer<T>, defValue: T?) =
        getString(key, null)?.let { JSON.parse(deserializer, it) } ?: defValue

fun <T : Any> SharedPreferences.Editor.putObject(key: String, serializer: KSerializer<T>, value: T) =
        putString(key, JSON.stringify(serializer, value))

fun <T : Any> SharedPreferences.rx_getObject(key: String, deserializer: KSerializer<T>, defValue: T): Single<T> = Single.just(getObject(key, deserializer, defValue))

fun <T : Any> SharedPreferences.rx_getObject(key: String, deserializer: KSerializer<T>): Maybe<T> = Maybe.create { maybeEmitter ->
    getObject(key, deserializer, null)?.let {
        maybeEmitter.onSuccess(it)
    }
    maybeEmitter.onComplete()
}
