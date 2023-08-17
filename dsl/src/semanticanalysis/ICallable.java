package semanticanalysis;

import interpreter.DSLInterpreter;

import parser.ast.Node;

import semanticanalysis.types.FunctionType;

import java.util.List;

public interface ICallable {
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

    FunctionType getFunctionType();
}
