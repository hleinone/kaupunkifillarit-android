package fi.kaupunkifillarit.rx

import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.coroutines.awaitObject
import fi.kaupunkifillarit.model.Rack
import fi.kaupunkifillarit.model.Racks
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

object Api {
    @FlowPreview
    @ObsoleteCoroutinesApi
    @InternalSerializationApi
    fun racks(): Flow<Set<Rack>> =
        ticker(10000, 0)
            .receiveAsFlow()
            .flatMapConcat {
                flow<Set<Rack>> {
                    val racks = Fuel.get("https://kaupunkifillarit.fi/api/stations")
                        .awaitObject(Racks::class.serializer()).racks
                    emit(racks)
                }.catch {
                    Log.w("Api", "Racks retrieval failed", it)
                    emit(emptySet())
                }
            }
}

suspend fun <T : Any> Request.awaitObject(deserializer: DeserializationStrategy<T>) =
    awaitObject(object : ResponseDeserializable<T> {
        override fun deserialize(content: String): T {
            return Json {
                ignoreUnknownKeys = true
            }.decodeFromString(deserializer, content)
        }
    })
