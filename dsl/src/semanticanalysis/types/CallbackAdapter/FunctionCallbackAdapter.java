package semanticanalysis.types.CallbackAdapter;

import interpreter.DSLInterpreter;

import parser.ast.FuncDefNode;

import runtime.IMemorySpace;
import runtime.RuntimeEnvironment;
import runtime.Value;

import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.FunctionType;

import java.util.ArrayList;
import java.util.Arrays;

public class FunctionCallbackAdapter implements ICallbackAdapter {
    private RuntimeEnvironment rtEnv;
    private FunctionType functionType;
    private FuncDefNode funcDefNode;
    private DSLInterpreter interpreter;

    FunctionCallbackAdapter(
        RuntimeEnvironment rtEnv,
        FunctionSymbol functionSymbol,
        DSLInterpreter interpreter) {
        this.rtEnv = rtEnv;
        this.functionType = (FunctionType) functionSymbol.getDataType();
        this.funcDefNode = functionSymbol.getAstRootNode();
        this.interpreter = interpreter;
    }


    @Override
    public Object call(Object... params) {
        var functionSymbol = rtEnv.getSymbolTable().getSymbolsForAstNode(funcDefNode).get(0);
        var returnValue =
            (Value) interpreter.executeUserDefinedFunctionRawParameters(
            (FunctionSymbol) functionSymbol, Arrays.stream(params).toList());

        return returnValue.getInternalValue();
    }
}
