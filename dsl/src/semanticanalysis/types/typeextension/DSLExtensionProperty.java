package semanticanalysis.types.typeextension;

import semanticanalysis.types.TypeBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DSLExtensionProperty {
    /**
     * The name to use for the corresponding member in the extended type. If it is not set,
     * the original name will be converted by {@link TypeBuilder}
     *
     * @return
     */
     String name() default "";
}
