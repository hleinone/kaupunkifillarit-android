package fi.kaupunkifillarit.rx;

import android.content.SharedPreferences;

import rx.Observable;
import rx.Subscriber;

abstract class SharedPreferenceObservable<T> implements Observable.OnSubscribe<T> {
    private final SharedPreferences sp;
    private final String key;

    SharedPreferenceObservable(SharedPreferences sp, String key) {
        this.sp = sp;
        this.key = key;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        try {
            T value = readValue(sp, key);
            if (value != null) {
                subscriber.onNext(value);
            }
            subscriber.onCompleted();
        } catch (Exception e) {
            subscriber.onError(e);
        }
    }

    protected abstract T readValue(SharedPreferences sp, String key) throws Exception;
}
