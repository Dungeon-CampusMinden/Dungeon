package dsl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @return WTF? .
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface DSLContextMember {
  /**
   * The name to use for the lookup in the context for this member.
   *
   * @return foo
   */
  public String name();
}
