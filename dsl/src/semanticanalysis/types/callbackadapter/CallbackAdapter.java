package semanticanalysis.types.callbackadapter;

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

/**
 * Encapsulates the {@link RuntimeEnvironment} and {@link DSLInterpreter} needed to execute a
 * callback-function defined in the DSL. Implements the functional interfaces needed for assigning
 * an instance of this class to the callback-fields in the components of the Dungeons ECS.
 */
public class CallbackAdapter implements Function, Consumer, TriConsumer {

    private final RuntimeEnvironment rtEnv;
    private final FunctionType functionType;
    private final FuncDefNode funcDefNode;
    private final DSLInterpreter interpreter;

    CallbackAdapter(
            RuntimeEnvironment rtEnv, FunctionSymbol functionSymbol, DSLInterpreter interpreter) {
        this.rtEnv = rtEnv;
        this.functionType = (FunctionType) functionSymbol.getDataType();
        this.funcDefNode = functionSymbol.getAstRootNode();
        this.interpreter = interpreter;
    }

    public Object call(Object... params) {
        var functionSymbol = rtEnv.getSymbolTable().getSymbolsForAstNode(funcDefNode).get(0);

        // TODO: handle encapsulated values (which should just be "unpacked" e.g. unencapsulated)
        // TODO: handle list- and set-types
        var returnValue =
                (Value)
                        interpreter.executeUserDefinedFunctionRawParameters(
                                (FunctionSymbol) functionSymbol, Arrays.stream(params).toList());

        return returnValue.getInternalValue();
    }

    // region interface implementation
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
    // endregion
}
