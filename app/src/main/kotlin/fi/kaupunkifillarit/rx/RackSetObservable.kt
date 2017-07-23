package fi.kaupunkifillarit.rx

import com.github.kittinunf.fuel.httpGet
import fi.kaupunkifillarit.model.Rack
import fi.kaupunkifillarit.model.Racks
import fi.kaupunkifillarit.util.rx_responseObject
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

object RackSetObservable {
    val racks: Observable<Set<Rack>> = Observable.interval(0, 10, TimeUnit.SECONDS).switchMap {
        "https://kaupunkifillarit.herokuapp.com/api/stations".httpGet().rx_responseObject(Racks::class.java).map { it.second.get().racks }.retry().toObservable()
    }
}
