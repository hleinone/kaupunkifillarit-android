package fi.kaupunkifillarit.jackson.datatype.fj.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionLikeType;

import java.io.IOException;

import fj.Ord;
import fj.data.Set;

public class SetDeserializer<T extends Set<Object>>
        extends FunctionalJavaCollectionDeserializer<T> {
    private static final long serialVersionUID = 1L;

    public SetDeserializer(CollectionLikeType type,
                           TypeDeserializer typeDeser, JsonDeserializer<?> deser) {
        super(type, typeDeser, deser);
    }

    @Override
    public SetDeserializer withResolved(TypeDeserializer typeDeser,
                                             JsonDeserializer<?> valueDeser) {
        return new SetDeserializer(_containerType, typeDeser,
                valueDeser);
    }

    @Override
    protected T _deserializeContents(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonDeserializer<?> valueDes = _valueDeserializer;
        JsonToken t;
        final TypeDeserializer typeDeser = _typeDeserializerForValue;
        // No way to pass actual type parameter; but does not matter, just
        // compiler-time fluff:
        Set<Object> set = Set.empty(Ord.hashOrd());

        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            Object value;

            if (t == JsonToken.VALUE_NULL) {
                value = null;
            } else if (typeDeser == null) {
                value = valueDes.deserialize(jp, ctxt);
            } else {
                value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
            }
            set = set.insert(value);
        }
        // No class outside of the package will be able to subclass us,
        // and we provide the proper builder for the subclasses we implement.
        @SuppressWarnings("unchecked")
        T collection = (T) set;
        return collection;
    }

    @Override
    protected T _deserializeFromSingleValue(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonDeserializer<?> valueDes = _valueDeserializer;
        final TypeDeserializer typeDeser = _typeDeserializerForValue;
        JsonToken t = jp.getCurrentToken();

        Object value;

        if (t == JsonToken.VALUE_NULL) {
            value = null;
        } else if (typeDeser == null) {
            value = valueDes.deserialize(jp, ctxt);
        } else {
            value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
        }
        @SuppressWarnings("unchecked")
        T result = (T) Set.set(Ord.hashOrd(), value);
        return result;
    }
}