package dsl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** WTF? . */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DSLTypeAdapter {
  /**
   * The name to use for the corresponding DSL data type. If it is not set, the class name of the
   * return type of the marked method will be converted by TypeBuilder.
   *
   * @return WTF? .
   */
  String name() default "";
}
