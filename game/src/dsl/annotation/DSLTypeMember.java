package dsl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @return WTF? .
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface DSLTypeMember {
  /**
   * The name to use for the corresponding member in a AggregateType. If it is not set, the original
   * field name will be converted by TypeBuilder.
   *
   * @return foo
   */
  public String name() default "";
}
