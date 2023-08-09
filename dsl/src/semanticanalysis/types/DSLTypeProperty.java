package semanticanalysis.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DSLTypeProperty {
    /**
     *
     * @return
     */
    String name() default "";

    /**
     * The Java-Class corresponding to the the dsl type, which should be extended
     * by this property.
     *
     * For {@link AggregateTypeAdapter} instances, this should be the adapter-class.
     * @return
     */
     Class<?> extendedType();
}

