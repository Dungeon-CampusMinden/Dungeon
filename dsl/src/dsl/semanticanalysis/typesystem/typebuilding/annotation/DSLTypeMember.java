package dsl.semanticanalysis.typesystem.typebuilding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface DSLTypeMember {
  /**
   * The name to use for the corresponding member in a {@link dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType}. If it is not set, the
   * original field name will be converted by {@link dsl.semanticanalysis.typesystem.typebuilding.TypeBuilder}
   *
   * @return
   */
  public String name() default "";
}
