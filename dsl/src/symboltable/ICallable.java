package symboltable;

import interpreter.DSLInterpreter;
import java.util.List;
import parser.AST.Node;

public interface ICallable {
    // TODO: refine signature
    // Object call(AstVisitor<Object> interperter, List<Object> parameters);
    enum Type {
        Native,
        UserDefined
    }

    Object call(DSLInterpreter interperter, List<Node> parameters);

    Type getCallableType();
}
