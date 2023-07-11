package semanticanalysis.types.CallbackAdapter;

import interpreter.DSLInterpreter;

import parser.ast.FuncDefNode;

import runtime.RuntimeEnvironment;

import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.FunctionType;

import java.util.Arrays;

public class ConsumerCallbackAdapter implements ICallbackAdapter {
    private RuntimeEnvironment rtEnv;
    private FunctionType functionType;
    private FuncDefNode funcDefNode;
    private DSLInterpreter interpreter;

    ConsumerCallbackAdapter(
            RuntimeEnvironment rtEnv, FunctionSymbol functionSymbol, DSLInterpreter interpreter) {
        this.rtEnv = rtEnv;
        this.functionType = (FunctionType) functionSymbol.getDataType();
        this.funcDefNode = functionSymbol.getAstRootNode();
        this.interpreter = interpreter;
    }

    @Override
    public Object call(Object... params) {
        // cast parameter
        var functionSymbol = rtEnv.getSymbolTable().getSymbolsForAstNode(funcDefNode).get(0);
        interpreter.executeUserDefinedFunctionRawParameters(
                (FunctionSymbol) functionSymbol, Arrays.stream(params).toList());
        return null;
    }
}
