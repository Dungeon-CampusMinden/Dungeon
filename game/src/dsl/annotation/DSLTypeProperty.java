package dsl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @return WTF? .
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DSLTypeProperty {
  /**
   * The name of the property, by which it should be accessible in a DSL program.
   *
   * @return the name.
   */
  String name();

  /**
   * The Java-Class corresponding to the the dsl type, which should be extended by this property.
   *
   * <p>For AggregateTypeAdapter instances, this should be the adapter-class.
   *
   * @return foo
   */
  Class<?> extendedType();

  /**
   * Is the property settable?
   *
   * @return true, if the property is settable, false otherwise
   */
  boolean isSettable() default true;

  /**
   * Is the property gettable?
   *
   * @return true, if the property is gettable, false otherwise
   */
  boolean isGettable() default true;
}
