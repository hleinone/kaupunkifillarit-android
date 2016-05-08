package fi.kaupunkifillarit.jackson.datatype.fj;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionLikeType;

import fi.kaupunkifillarit.jackson.datatype.fj.deser.ArrayDeserializer;
import fi.kaupunkifillarit.jackson.datatype.fj.deser.SetDeserializer;
import fj.data.Array;
import fj.data.Set;

/**
 * Custom deserializers module offers.
 */
public class FunctionalJavaDeserializers
        extends Deserializers.Base {
    @Override
    public JsonDeserializer<?> findCollectionLikeDeserializer(CollectionLikeType type, DeserializationConfig config, BeanDescription beanDesc, TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer) throws JsonMappingException {
        if (type.hasRawClass(Set.class)) {
            return new SetDeserializer(type, elementTypeDeserializer, elementDeserializer);
        } else if (type.hasRawClass(Array.class)) {
            return new ArrayDeserializer(type, elementTypeDeserializer, elementDeserializer);
        }
        return super.findCollectionLikeDeserializer(type, config, beanDesc, elementTypeDeserializer, elementDeserializer);
    }
}