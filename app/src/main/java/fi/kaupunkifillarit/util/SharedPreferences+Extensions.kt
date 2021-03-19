package fi.kaupunkifillarit.util

import android.content.SharedPreferences
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

fun <T : Any> SharedPreferences.getObject(
    key: String,
    deserializer: KSerializer<T>,
    defValue: T?
) = getString(key, null)?.let { Json.decodeFromString(deserializer, it) } ?: defValue

fun <T : Any> SharedPreferences.Editor.putObject(
    key: String,
    serializer: KSerializer<T>,
    value: T
): SharedPreferences.Editor = putString(key, Json.encodeToString(serializer, value))
