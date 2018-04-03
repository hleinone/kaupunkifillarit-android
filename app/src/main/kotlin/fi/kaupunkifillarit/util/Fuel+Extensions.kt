package fi.kaupunkifillarit.util

import com.bluelinelabs.logansquare.LoganSquare
import com.bluelinelabs.logansquare.ParameterizedType
import com.github.kittinunf.fuel.core.*
import com.github.kittinunf.fuel.rx.rx_object
import com.github.kittinunf.fuel.rx.rx_responseObject
import com.github.kittinunf.result.Result
import java.io.InputStream

fun <T : Any> Request.responseObject(type: Class<T>, handler: (Request, Response, Result<T, FuelError>) -> Unit) = responseObject(LoganSquareClassResponseDeserializer(type), handler)

fun <T : Any> Request.responseObject(type: Class<T>, handler: Handler<T>) = responseObject(LoganSquareClassResponseDeserializer(type), handler)

fun <T : Any> Request.responseObject(type: Class<T>) = responseObject(LoganSquareClassResponseDeserializer(type))

fun <T : Any> Request.rx_responseObject(type: Class<T>) = rx_responseObject(LoganSquareClassResponseDeserializer(type))

fun <T : Any> Request.rx_object(type: Class<T>) = rx_object(LoganSquareClassResponseDeserializer(type))

fun <T : Any> Request.responseObject(parameterizedType: ParameterizedType<T>, handler: (Request, Response, Result<T, FuelError>) -> Unit) = responseObject(LoganSquareParameterizedTypeResponseDeserializer(parameterizedType), handler)

fun <T : Any> Request.responseObject(parameterizedType: ParameterizedType<T>, handler: Handler<T>) = responseObject(LoganSquareParameterizedTypeResponseDeserializer(parameterizedType), handler)

fun <T : Any> Request.responseObject(parameterizedType: ParameterizedType<T>) = responseObject(LoganSquareParameterizedTypeResponseDeserializer(parameterizedType))

fun <T : Any> Request.rx_responseObject(parameterizedType: ParameterizedType<T>) = rx_responseObject(LoganSquareParameterizedTypeResponseDeserializer(parameterizedType))

fun <T : Any> Request.rx_object(parameterizedType: ParameterizedType<T>) = rx_object(LoganSquareParameterizedTypeResponseDeserializer(parameterizedType))

/**
 * Parses Fuel responses to the desired object type using LoganSquare.
 */
private class LoganSquareClassResponseDeserializer<out T : Any>(private val type: Class<T>) : ResponseDeserializable<T> {
    override fun deserialize(inputStream: InputStream): T? {
        return LoganSquare.parse(inputStream, type)
    }
}

/**
 * Parses Fuel responses to the desired object type using LoganSquare.
 */
private class LoganSquareParameterizedTypeResponseDeserializer<out T : Any>(private val parameterizedType: ParameterizedType<T>) : ResponseDeserializable<T> {
    override fun deserialize(inputStream: InputStream): T? {
        return LoganSquare.parse(inputStream, parameterizedType)
    }
}
