package symboltable;

import interpreter.DSLInterpreter;
import java.util.List;
import parser.AST.Node;

// TODO:
//  how to hook in implementation? -> siehe ASTdriven vorlesung
public class FunctionSymbol extends ScopedSymbol implements ICallable {

    private final Node astRootNode;

    /**
     * @param astRootNode
     */
    public FunctionSymbol(String name, IScope parentScope, Node astRootNode, IType retType) {
        super(name, parentScope, retType);

        this.astRootNode = astRootNode;
    }

    // TODO: this should just call interpreter-method, which does all the parameter pushing
    //  for native functions,
    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
        return interpreter.executeUserDefinedFunction(this, parameters);
    }

    @Override
    public ICallable.Type getCallableType() {
        return ICallable.Type.UserDefined;
    }
}
