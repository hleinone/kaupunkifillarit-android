package fi.kaupunkifillarit.util

import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.rx.rx_object
import kotlinx.serialization.KSerialLoader
import kotlinx.serialization.json.JSON

fun <T : Any> Request.rx_object(deserializer: KSerialLoader<T>) =
        rx_object(object : ResponseDeserializable<T> {
            override fun deserialize(content: String): T {
                return try {
                    JSON.nonstrict.parse(deserializer, content)
                } catch (e: IllegalArgumentException) {
                    throw FuelError(e)
                }
            }
        })