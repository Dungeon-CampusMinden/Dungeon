package semanticAnalysis.types;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface DSLContextMember {
    /**
     * The name to use for the lookup in the context for this member
     *
     * @return
     */
    public String name();
}
