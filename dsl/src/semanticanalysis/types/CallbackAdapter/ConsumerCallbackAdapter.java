package semanticanalysis.types.CallbackAdapter;

import interpreter.DSLInterpreter;
import parser.ast.FuncDefNode;
import runtime.IMemorySpace;
import runtime.RuntimeEnvironment;
import runtime.Value;
import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.FunctionType;

import java.util.ArrayList;

public class ConsumerCallbackAdapter implements ICallbackAdapter {
    private RuntimeEnvironment rtEnv;
    private FunctionType functionType;
    private FuncDefNode funcDefNode;
    private IMemorySpace parentMemorySpace;
    private DSLInterpreter interpreter;

    ConsumerCallbackAdapter(
        RuntimeEnvironment rtEnv,
        FunctionSymbol functionSymbol,
        IMemorySpace parentMemorySpace,
        DSLInterpreter interpreter) {
        this.rtEnv = rtEnv;
        this.functionType = (FunctionType) functionSymbol.getDataType();
        this.funcDefNode = functionSymbol.getAstRootNode();
        this.parentMemorySpace = parentMemorySpace;
        this.interpreter = interpreter;
    }

    @Override
    public Object call(Object... params) {
        // cast parameter
        ArrayList<Value> parameterValues = new ArrayList<>();
        for (var param : params) {
            var paramObject = (Value) rtEnv.translateRuntimeObject(param, parentMemorySpace);
            parameterValues.add(paramObject);
        }
        var functionSymbol = rtEnv.getSymbolTable().getSymbolsForAstNode(funcDefNode).get(0);
        interpreter.executeUserDefinedFunctionConcreteParameterValues(
                    (FunctionSymbol) functionSymbol, parameterValues);
        return null;
    }
}
