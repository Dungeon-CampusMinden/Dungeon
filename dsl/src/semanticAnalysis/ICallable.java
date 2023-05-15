package semanticAnalysis;

import interpreter.DSLInterpreter;

import parser.AST.Node;

import java.util.List;

public interface ICallable {
    // TODO: refine signature
    // Object call(AstVisitor<Object> interperter, List<Object> parameters);
    enum Type {
        Native,
        UserDefined
    }

    /**
     * Call this ICallable with a given interpreter and given parameters
     *
     * @param interperter a {@link DSLInterpreter}, which is used for performing the actual function
     *     call
     * @param parameters List of ASTNodes, corresponding to the parameters of the function call
     * @return The return value of the function call
     */
    Object call(DSLInterpreter interperter, List<Node> parameters);

    /**
     * Get the type of
     *
     * @return
     */
    Type getCallableType();
}
