package semanticanalysis;

import interpreter.DSLInterpreter;

import parser.ast.FuncDefNode;
import parser.ast.Node;

import semanticanalysis.types.FunctionType;

import java.util.List;
import java.util.function.Function;

public class FunctionSymbol extends ScopedSymbol implements ICallable {

    private final FuncDefNode astRootNode;

    /**
     * @param astRootNode
     */
    public FunctionSymbol(
            String name, IScope parentScope, FuncDefNode astRootNode, FunctionType functionType) {
        super(name, parentScope, functionType);

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

    @Override
    public FunctionType getFunctionType() {
        return (FunctionType)this.getDataType();
    }

    public FuncDefNode getAstRootNode() {
        return astRootNode;
    }
}
