package fi.kaupunkifillarit.rx;

import android.content.SharedPreferences;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import rx.Observable;

public class JacksonSharedPreferenceObservable<T> extends SharedPreferenceObservable<T> {
    private final ObjectMapper objectMapper;
    private final Class<T> classOfT;

    public static <T> Observable<T> createObservable(ObjectMapper objectMapper, SharedPreferences sp, String key, Class<T> classOfT) {
        return Observable.create(new JacksonSharedPreferenceObservable<>(objectMapper, sp, key, classOfT));
    }

    private JacksonSharedPreferenceObservable(ObjectMapper objectMapper, SharedPreferences sp, String key, Class<T> classOfT) {
        super(sp, key);
        this.objectMapper = objectMapper;
        this.classOfT = classOfT;
    }

    @Override
    protected T readValue(SharedPreferences sp, String key) throws IOException {
        String json = sp.getString(key, null);
        if (json == null) {
            return null;
        }
        return objectMapper.readValue(json, classOfT);
    }
}
