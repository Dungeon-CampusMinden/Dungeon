package semanticanalysis.types.CallbackAdapter;

import interpreter.DSLInterpreter;

import semanticanalysis.FunctionSymbol;

/**
 * Builder class for {@link CallbackAdapter}. Stores a reference to
 * the DSLInterpreter, because it is needed to actually perform the
 * execution of a callback defined in the DSL.
 */
public class CallbackAdapterBuilder {
    DSLInterpreter interpreter;

    public CallbackAdapterBuilder(DSLInterpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Build a {@link CallbackAdapter} for a concrete DSL function for assigning
     * to a callback-Field in a Component of the Dungeons ECS.
     *
     * @param functionSymbol The Symbol representing the function definition
     * @return The created {@link CallbackAdapter}
     */
    public CallbackAdapter buildAdapter(FunctionSymbol functionSymbol) {
        return new CallbackAdapter(
                interpreter.getRuntimeEnvironment(), functionSymbol, interpreter);
    }
}
