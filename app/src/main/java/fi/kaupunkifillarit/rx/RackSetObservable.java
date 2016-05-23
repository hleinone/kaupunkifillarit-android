package fi.kaupunkifillarit.rx;

import com.bluelinelabs.logansquare.ParameterizedType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fi.kaupunkifillarit.model.Rack;
import fi.kaupunkifillarit.model.Racks;
import fj.Ord;
import fj.data.Set;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import timber.log.Timber;

public class RackSetObservable {
    private static final ParameterizedType<Racks> TYPE_REFERENCE = new ParameterizedType<Racks>() {
    };

    public static Observable<Set<Rack>> createObservable(OkHttpClient client, Request request) {
        return Observable.interval(0, 10, TimeUnit.SECONDS)
                .flatMap(seconds -> OkHttpObservable.createObservable(client, request))
                .map(response -> {
                    try {
                        return response.body().byteStream();
                    } catch (IOException e) {
                        throw OnErrorThrowable.from(e);
                    }
                })
                .flatMap(is -> LoganSquareObservable.createObservable(is, TYPE_REFERENCE)).doOnError(throwable -> {
                    Timber.w(throwable, "Rack retrieval failure");
                    throw OnErrorThrowable.from(throwable);
                }).retry().map(racks -> Set.iterableSet(Ord.hashOrd(), racks.racks));
    }
}
