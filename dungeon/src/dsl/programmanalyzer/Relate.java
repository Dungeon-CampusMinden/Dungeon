package dsl.programmanalyzer;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface Relate {
  String TYPE = "type";

  String type() default "";

  boolean persistObject() default true;
}
