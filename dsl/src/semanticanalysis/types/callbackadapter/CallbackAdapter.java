package semanticanalysis.types.callbackadapter;

import core.utils.TriConsumer;

import interpreter.DSLInterpreter;

import runtime.*;

import semanticanalysis.ICallable;
import semanticanalysis.types.FunctionType;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Encapsulates the {@link RuntimeEnvironment} and {@link DSLInterpreter} needed to execute a
 * callback-function defined in the DSL. Implements the functional interfaces needed for assigning
 * an instance of this class to the callback-fields in the components of the Dungeons ECS.
 */
public class CallbackAdapter implements Consumer, TriConsumer, BiConsumer {

    private final RuntimeEnvironment rtEnv;
    private final FunctionType functionType;
    private final ICallable callable;
    private final DSLInterpreter interpreter;

    CallbackAdapter(RuntimeEnvironment rtEnv, ICallable callable, DSLInterpreter interpreter) {
        this.rtEnv = rtEnv;
        this.functionType = callable.getFunctionType();
        this.callable = callable;
        this.interpreter = interpreter;
    }

    public Object call(Object... params) {
        Value returnValue =
                (Value)
                        interpreter.callCallableRawParameters(
                                this.callable, Arrays.stream(params).toList());

        return convertValueToObject(returnValue);
    }

    protected Object convertValueToObject(Value value) {
        return this.rtEnv.getTypeInstantiator().instantiate(value);
    }

    // region interface implementation
    @Override
    public void accept(Object o) {
        this.call(o);
    }

    @Override
    public void accept(Object o, Object o2, Object o3) {
        this.call(o, o2, o3);
    }

    @Override
    public void accept(Object o, Object o2) {
        this.call(o, o2);
    }
    // endregion
}
