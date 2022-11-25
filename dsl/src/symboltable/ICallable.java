package symboltable;

import interpreter.DSLInterpreter;
import java.util.List;

public interface ICallable {
    // TODO: refine signature
    // Object call(AstVisitor<Object> interperter, List<Object> parameters);
    enum Type {
        Native,
        UserDefined
    }

    Object call(DSLInterpreter interperter, List<Object> parameters);

    Type getCallableType();
}
