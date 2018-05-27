package fi.kaupunkifillarit.rx

import com.github.kittinunf.fuel.Fuel
import fi.kaupunkifillarit.model.Rack
import fi.kaupunkifillarit.model.Racks
import fi.kaupunkifillarit.util.rx_object
import io.reactivex.Observable
import kotlinx.serialization.serializer
import java.util.concurrent.TimeUnit

object Api {
    val racks: Observable<Set<Rack>> by lazy {
        Observable.interval(0, 10, TimeUnit.SECONDS)
                .switchMapSingle {
                    Fuel.get("https://kaupunkifillarit.herokuapp.com/api/stations")
                            .rx_object(Racks::class.serializer())
                            .doOnSuccess { println("result $it") }
                            .map { it.get().racks }
                }
    }
}