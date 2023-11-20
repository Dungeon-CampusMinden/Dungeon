package dsl.semanticanalysis.typesystem.typebuilding.annotation;

import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateTypeAdapter;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Annotation to mark an {@link IDSLExtensionMethod} implementation. */
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
     * <p>For {@link AggregateTypeAdapter} instances, this should be the adapter-class.
     */
    Class<?> extendedType();
}
