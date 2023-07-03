package dslToGame;

import core.Entity;
import interpreter.DSLInterpreter;
import runtime.*;
import semanticanalysis.IScope;
import semanticanalysis.Symbol;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.IType;
import semanticanalysis.types.TypeBuilder;

import java.util.AbstractMap;
import java.util.HashMap;

public class RuntimeObjectTranslator {
    private HashMap<Class<?>, IRuntimeObjectTranslator> translators = new HashMap<>();

    public void loadRuntimeTranslator(Class<?> clazz, IRuntimeObjectTranslator translator) {
        if (this.translators.containsKey(clazz)) {
            throw new RuntimeException("RuntimeObjectTranslator for class '" + clazz + "' is already registered");
        }
        this.translators.put(clazz, translator);
    }

    public Value translateRuntimeObject(Object object, DSLInterpreter interpreter) {
        IMemorySpace parentMemorySpace = interpreter.getCurrentMemorySpace();
        IScope globalScope = interpreter.getRuntimeEnvironment().getGlobalScope();

        var objectsClass = object.getClass();
        var translator = this.translators.get(objectsClass);
        Value returnValue = Value.NONE;
        if (translator == null) {
            // TODO: lookup type
            IType dslType = TypeBuilder.getDSLTypeForClass(objectsClass);
            if (dslType != null) {
                // create plain value
                returnValue = new Value(dslType, object);
            } else {
                String dslTypeName = TypeBuilder.getDSLName(objectsClass);
                Symbol dslTypeSymbol = globalScope.resolve(dslTypeName);
                if (dslTypeSymbol.equals(Symbol.NULL)) {
                    throw new RuntimeException(
                        "Could not translate object of type '"
                            + objectsClass
                            + "' to dsl value, type could not be resolved!");
                } else {
                    dslType = (IType)dslTypeSymbol;
                    if (dslType.getTypeKind() == IType.Kind.Aggregate) {
                        var aggregateType = (AggregateType)dslType;

                        returnValue = new AggregateValue(aggregateType, parentMemorySpace);
                        var encapsulatedObject = new EncapsulatedObject(object, aggregateType, parentMemorySpace);
                        ((AggregateValue)returnValue).setMemorySpace(encapsulatedObject);
                    }
                    // TODO: Add other branches or figure out, if the distinction based on
                    //  typekind is even necessary here
                }
            }
        } else {
            returnValue = translator.translate(
                object, globalScope, parentMemorySpace);
        }
        return returnValue;
    }
}
