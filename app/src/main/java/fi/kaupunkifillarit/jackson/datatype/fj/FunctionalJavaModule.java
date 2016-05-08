package fi.kaupunkifillarit.jackson.datatype.fj;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;

import java.lang.reflect.Type;

import fj.data.Array;
import fj.data.Set;

public class FunctionalJavaModule extends Module {

    @Override
    public String getModuleName() {
        return "FunctionalJavaModule";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        context.addDeserializers(new FunctionalJavaDeserializers());
        context.addTypeModifier(new TypeModifier() {
            @Override
            public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
                if (type.hasRawClass(Set.class)) {
                    return CollectionLikeType.construct(Set.class, type.containedType(0) != null ? type.containedType(0) : SimpleType.construct(Object.class));
                } else if (type.hasRawClass(Array.class)) {
                    return CollectionLikeType.construct(Array.class, type.containedType(0) != null ? type.containedType(0) : SimpleType.construct(Object.class));
                }
                return type;
            }
        });
    }

    @Override
    public int hashCode() {
        return FunctionalJavaModule.class.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o;
    }
}
