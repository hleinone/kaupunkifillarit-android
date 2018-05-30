package fi.kaupunkifillarit.rx

import com.github.kittinunf.fuel.Fuel
import fi.kaupunkifillarit.model.Rack
import fi.kaupunkifillarit.model.Racks
import fi.kaupunkifillarit.util.rx_object
import io.reactivex.Maybe
import io.reactivex.Observable
import kotlinx.serialization.serializer
import timber.log.Timber
import java.util.concurrent.TimeUnit

object Api {
    val racks: Observable<Set<Rack>> by lazy {
        Observable.interval(0, 10, TimeUnit.SECONDS)
                .flatMapMaybe {
                    Fuel.get("https://kaupunkifillarit.herokuapp.com/api/stations")
                            .rx_object(Racks::class.serializer())
                            .flatMapMaybe { (racks, error) ->
                                if (racks != null) {
                                    Maybe.just(racks.racks)
                                } else {
                                    Timber.w(error, "Rack retrieval failed")
                                    Maybe.empty()
                                }
                            }
                }
    }
}