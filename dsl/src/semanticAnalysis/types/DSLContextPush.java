package semanticAnalysis.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DSLContextPush {
    /**
     * The name to use to push this object on the context
     *
     * @return
     */
    public String name();
}
