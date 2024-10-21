package de.fwatermann.dungine.utils.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code Nullable} annotation is used to indicate that a method return value, field, parameter,
 * or local variable can be null. It serves as a hint for developers and tools that null checks may
 * be necessary to prevent null pointer exceptions.
 *
 * <p>Applying this annotation does not enforce any behavior or add any runtime functionality.
 * Instead, it is used purely for documentation and analysis purposes. Tools such as static code
 * analyzers, IDEs, or the Java compiler can utilize this annotation to generate warnings or errors
 * when potentially unsafe operations are performed on annotated elements without null checks.
 *
 * <p>This annotation can be applied to:
 *
 * <ul>
 *   <li>Methods - indicating the method might return null.
 *   <li>Fields - indicating the field can be null.
 *   <li>Parameters - indicating the parameter can be null.
 *   <li>Local variables - indicating the local variable can be null.
 * </ul>
 *
 * <p>It is recommended to use this annotation to improve code clarity and safety, especially in
 * APIs where nullability might not be obvious. However, developers should still perform null checks
 * as necessary based on the logic of their applications.
 */
@Documented
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@Retention(RetentionPolicy.CLASS)
public @interface Nullable {}
