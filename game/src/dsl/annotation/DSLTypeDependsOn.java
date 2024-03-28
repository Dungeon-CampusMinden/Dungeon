package dsl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** WTF? . */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DSLTypeDependsOn {
  // TODO: implement

  /**
   * WTF? .
   *
   * @return foo
   */
  Class<?>[] type();
}
