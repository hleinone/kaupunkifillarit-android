package fi.kaupunkifillarit.rx;

import com.bluelinelabs.logansquare.LoganSquare;
import com.bluelinelabs.logansquare.ParameterizedType;

import java.io.IOException;
import java.io.InputStream;

import rx.Observable;
import rx.Subscriber;

public class LoganSquareObservable<T> implements Observable.OnSubscribe<T> {
    private final InputStream is;
    private final ParameterizedType<T> typeOfT;

    public static <T> Observable<T> createObservable(InputStream is, ParameterizedType<T> typeOfT) {
        return Observable.create(new LoganSquareObservable<>(is, typeOfT));
    }

    private LoganSquareObservable(InputStream is, ParameterizedType<T> typeOfT) {
        this.is = is;
        this.typeOfT = typeOfT;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        try {
            subscriber.onNext(LoganSquare.parse(is, typeOfT));
            subscriber.onCompleted();
        } catch (IOException e) {
            subscriber.onError(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) { }
        }
    }
}
