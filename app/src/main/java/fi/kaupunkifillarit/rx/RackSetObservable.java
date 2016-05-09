package fi.kaupunkifillarit.rx;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import fi.kaupunkifillarit.model.Rack;
import fi.kaupunkifillarit.model.Racks;
import fj.data.Set;
import rx.Observable;
import rx.exceptions.OnErrorThrowable;
import timber.log.Timber;

public class RackSetObservable {
    private static final TypeReference<Racks> TYPE_REFERENCE = new TypeReference<Racks>() {
    };

    public static Observable<Set<Rack>> createObservable(ObjectMapper objectMapper, OkHttpClient client, Request request) {
        return Observable.interval(10, TimeUnit.SECONDS)
                .flatMap(seconds -> OkHttpObservable.createObservable(client, request))
                .map(response -> {
                    try {
                        return response.body().byteStream();
                    } catch (IOException e) {
                        throw OnErrorThrowable.from(e);
                    }
                })
                .flatMap(is -> JacksonObservable.createObservable(objectMapper, is, TYPE_REFERENCE)).doOnError(throwable -> {
                    Timber.w(throwable, "Rack retrieval failure");
                    throw OnErrorThrowable.from(throwable);
                }).retry().map(racks -> racks.racks);
    }
}
