package fi.kaupunkifillarit.rx;

import android.content.SharedPreferences;

import com.bluelinelabs.logansquare.LoganSquare;

import java.io.IOException;

import rx.Observable;

public class LoganSquareSharedPreferenceObservable<T> extends SharedPreferenceObservable<T> {
    private final Class<T> classOfT;

    public static <T> Observable<T> createObservable(SharedPreferences sp, String key, Class<T> classOfT) {
        return Observable.create(new LoganSquareSharedPreferenceObservable<>(sp, key, classOfT));
    }

    private LoganSquareSharedPreferenceObservable(SharedPreferences sp, String key, Class<T> classOfT) {
        super(sp, key);
        this.classOfT = classOfT;
    }

    @Override
    protected T readValue(SharedPreferences sp, String key) throws IOException {
        String json = sp.getString(key, null);
        if (json == null) {
            return null;
        }
        return LoganSquare.parse(json, classOfT);
    }
}
