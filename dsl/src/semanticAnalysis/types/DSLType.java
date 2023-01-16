package semanticAnalysis.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DSLType {
    /**
     * The name to use for the corresponding DSL data type. If it is not set, the original class
     * name will be converted by {@link TypeBuilder}
     *
     * @return
     */
    public String name() default "";
}
