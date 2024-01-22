package dsl.semanticanalysis.typesystem.typebuilding.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation to mark an {@link dsl.semanticanalysis.typesystem.extension.IDSLExtensionMethod} implementation. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface DSLExtensionMethod {
  /**
   * The name of the extension method, by which it should be accessible in a DSL program.
   *
   * @return the name.
   */
  String name();

  /**
   * The Java-Class corresponding to the dsl type, which should be extended by this method.
   *
   * <p>For {@link dsl.semanticanalysis.typesystem.extension.IDSLExtensionMethod} instances, this should be the adapter-class.
   */
  Class<?> extendedType();
}
