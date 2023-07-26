package semanticanalysis.types.callbackadapter;

import interpreter.DSLInterpreter;

import semanticanalysis.FunctionSymbol;
import semanticanalysis.types.BuiltInType;
import semanticanalysis.types.FunctionType;

/**
 * Builder class for {@link CallbackAdapter}. Stores a reference to the {@link DSLInterpreter},
 * because it is needed to actually perform the execution of a callback defined in the DSL.
 */
public class CallbackAdapterBuilder {
    DSLInterpreter interpreter;

    public CallbackAdapterBuilder(DSLInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Build a {@link CallbackAdapter} for a concrete DSL function for assigning to a callback-Field
     * in a Component of the Dungeons ECS.
     *
     * @param functionSymbol The Symbol representing the function definition
     * @return The created {@link CallbackAdapter}
     */
    public CallbackAdapter buildAdapter(FunctionSymbol functionSymbol) {
        FunctionType functionType = (FunctionType) functionSymbol.getDataType();
        if (functionType.getReturnType() != BuiltInType.noType &&
            functionType.getParameterTypes().size() == 1) {
            return new FunctionCallbackAdapter(
                interpreter.getRuntimeEnvironment(), functionSymbol, interpreter);
        } else if (functionType.getReturnType() != BuiltInType.noType &&
                   functionType.getParameterTypes().size() == 2) {
            return new BiFunctionCallbackAdapter(
                interpreter.getRuntimeEnvironment(), functionSymbol, interpreter);
        } else {
            return new CallbackAdapter(
                interpreter.getRuntimeEnvironment(), functionSymbol, interpreter);
        }
    }
}
