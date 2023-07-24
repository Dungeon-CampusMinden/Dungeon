package semanticanalysis.types.callbackadapter;

import core.utils.TriConsumer;

import interpreter.DSLInterpreter;

import parser.ast.FuncDefNode;

import runtime.*;

import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.FunctionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

        var returnValue =
                (Value)
                        interpreter.executeUserDefinedFunctionRawParameters(
                                (FunctionSymbol) functionSymbol, Arrays.stream(params).toList());

        return convertValueToObject(returnValue);
    }

    protected Object convertValueToObject(Value value) {
        var valuesType = value.getDataType();
        switch (valuesType.getTypeKind()) {
            case Basic:
                if (valuesType.equals(Prototype.PROTOTYPE)) {
                    throw new RuntimeException("Can't convert prototype to object");
                }
                return value.getInternalValue();
            case Aggregate:
            case PODAdapted:
            case AggregateAdapted:
                // TODO: does this always work?
                return value.getInternalValue();
            case FunctionType:
                // TODO: convert to callback adapter?
                break;
            case SetType:
                var set = new HashSet<>();
                SetValue setValue = (SetValue) value;
                for (var entry : setValue.getValues()) {
                    Object entryObject = convertValueToObject(entry);
                    set.add(entryObject);
                }
                return set;
            case ListType:
                var list = new ArrayList<>();
                ListValue listValue = (ListValue) value;
                for (var entry : listValue.getValues()) {
                    Object entryObject = convertValueToObject(entry);
                    list.add(entryObject);
                }
                return list;
        }
        return null;
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
