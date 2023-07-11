package semanticanalysis.types.CallbackAdapter;

import interpreter.DSLInterpreter;

import parser.ast.FuncDefNode;

import runtime.IMemorySpace;
import runtime.RuntimeEnvironment;
import runtime.Value;

import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.FunctionType;

import java.util.ArrayList;

public class FunctionCallbackAdapter implements ICallbackAdapter {
    private RuntimeEnvironment rtEnv;
    private FunctionType functionType;
    private FuncDefNode funcDefNode;
    private IMemorySpace parentMemorySpace;
    private DSLInterpreter interpreter;

    FunctionCallbackAdapter(
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
        var paramObject = (Value) rtEnv.translateRuntimeObject(params[0], parentMemorySpace);
        var functionSymbol = rtEnv.getSymbolTable().getSymbolsForAstNode(funcDefNode).get(0);
        ArrayList<Value> parameterValues = new ArrayList<>();
        parameterValues.add(paramObject);
        var returnValue =
            (Value)
                interpreter.executeUserDefinedFunctionConcreteParameterValues(
                    (FunctionSymbol) functionSymbol, parameterValues);
        return returnValue.getInternalValue();
    }
}
