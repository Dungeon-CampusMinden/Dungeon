package semanticanalysis.types.CallbackAdapter;

import core.utils.TriConsumer;
import interpreter.DSLInterpreter;
import parser.ast.FuncDefNode;
import runtime.RuntimeEnvironment;
import runtime.Value;
import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.FunctionType;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

public class CallbackAdapter implements Function, Consumer, TriConsumer {

    private RuntimeEnvironment rtEnv;
    private FunctionType functionType;
    private FuncDefNode funcDefNode;
    private DSLInterpreter interpreter;

    CallbackAdapter(
        RuntimeEnvironment rtEnv, FunctionSymbol functionSymbol, DSLInterpreter interpreter) {
        this.rtEnv = rtEnv;
        this.functionType = (FunctionType) functionSymbol.getDataType();
        this.funcDefNode = functionSymbol.getAstRootNode();
        this.interpreter = interpreter;
    }

    public Object call(Object... params) {
        var functionSymbol = rtEnv.getSymbolTable().getSymbolsForAstNode(funcDefNode).get(0);
        var returnValue =
            (Value)
                interpreter.executeUserDefinedFunctionRawParameters(
                    (FunctionSymbol) functionSymbol, Arrays.stream(params).toList());

        return returnValue.getInternalValue();
    }

    @Override
    public Object apply(Object o) {
        return this.call(o);
    }

    @Override
    public void accept(Object o) {
        this.call(o);
    }

    @Override
    public void accept(Object o, Object o2, Object o3) {
        this.call(o, o2, o3);
    }
}
