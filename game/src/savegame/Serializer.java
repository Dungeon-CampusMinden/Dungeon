package savegame;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Serializer {

    public Class<?> value() default NotSerializable.class;

    class NotSerializable {}
}
