package fi.kaupunkifillarit.util

import com.crashlytics.android.Crashlytics
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.rx.rx_object
import kotlinx.serialization.KSerialLoader
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JSON

fun <T : Any> Request.rx_object(deserializer: KSerialLoader<T>) =
        rx_object(object : ResponseDeserializable<T> {
            override fun deserialize(content: String): T {
                return try {
                    JSON.nonstrict.parse(deserializer, content)
                } catch (e: SerializationException) {
                    Crashlytics.log("SerializationException in parsing JSON:")
                    Crashlytics.setString("JSON", content)
                    throw e
                } catch (e: IllegalArgumentException) {
                    Crashlytics.log("IllegalArgumentException in parsing JSON")
                    Crashlytics.setString("JSON", content)
                    throw e
                }
            }
        })