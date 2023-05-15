package semanticAnalysis;

import interpreter.DSLInterpreter;

import parser.AST.Node;

import semanticAnalysis.types.IType;

import java.util.List;

public class FunctionSymbol extends ScopedSymbol implements ICallable {

    private final Node astRootNode;

    /**
     * @param astRootNode
     */
    public FunctionSymbol(String name, IScope parentScope, Node astRootNode, IType retType) {
        super(name, parentScope, retType);

        this.astRootNode = astRootNode;
    }

    @Override
    public Object call(DSLInterpreter interpreter, List<Node> parameters) {
        return interpreter.executeUserDefinedFunction(this, parameters);
    }

    @Override
    public ICallable.Type getCallableType() {
        return ICallable.Type.UserDefined;
    }
}
