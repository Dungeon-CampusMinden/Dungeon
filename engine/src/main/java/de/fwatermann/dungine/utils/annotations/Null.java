package de.fwatermann.dungine.utils.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The `Null` annotation is used to indicate that a field, method return value, parameter, or local variable
 * can be null. This annotation is for documentation purposes and is retained in the class file but not at runtime.
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.CLASS)
public @interface Null {}
