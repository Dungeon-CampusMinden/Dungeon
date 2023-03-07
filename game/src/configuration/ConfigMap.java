package configuration;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.TYPE})
public @interface ConfigMap {

    /**
     * JSON-Path prefix for fields in annotated class
     *
     * @return JSON-Path prefix
     */
    String[] path() default {};
}
