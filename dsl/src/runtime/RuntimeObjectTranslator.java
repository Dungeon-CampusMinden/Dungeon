package dslToGame;

import runtime.*;

import semanticanalysis.IScope;
import semanticanalysis.Symbol;
import semanticanalysis.types.AggregateType;
import semanticanalysis.types.IType;
import semanticanalysis.types.TypeBuilder;

import java.util.HashMap;

// TODO: javadoc
public class RuntimeObjectTranslator {
    private HashMap<Class<?>, IObjectToValueTranslator> translators = new HashMap<>();

    // TODO: javadoc and rename
    public void loadRuntimeTranslator(Class<?> clazz, IObjectToValueTranslator translator) {
        if (this.translators.containsKey(clazz)) {
            throw new RuntimeException(
                    "RuntimeObjectTranslator for class '" + clazz + "' is already registered");
        }
        this.translators.put(clazz, translator);
    }

    protected Value translateRuntimeObjectDefault(
            Object object, IScope globalScope, IMemorySpace parentMemorySpace, IEvironment environment) {
        Value returnValue = Value.NONE;
        var objectsClass = object.getClass();
        IType dslType = TypeBuilder.getDSLTypeForClass(objectsClass);

        if (dslType != null) {
            // create plain value
            returnValue = new Value(dslType, object);
        } else {
            String dslTypeName = TypeBuilder.getDSLName(objectsClass);
            Symbol dslTypeSymbol = globalScope.resolve(dslTypeName);
            if (dslTypeSymbol != Symbol.NULL) {
                dslType = (IType) dslTypeSymbol;
                IType.Kind typeKind = dslType.getTypeKind();
                if (typeKind == IType.Kind.Aggregate) {
                    var aggregateType = (AggregateType) dslType;

                    returnValue = new AggregateValue(aggregateType, parentMemorySpace, object);
                    var encapsulatedObject =
                            new EncapsulatedObject(object, aggregateType, parentMemorySpace, environment);
                    ((AggregateValue) returnValue).setMemorySpace(encapsulatedObject);
                } else if (typeKind == IType.Kind.PODAdapted ||
                           typeKind == IType.Kind.AggregateAdapted) {
                    // if the type is adapted, it is an external type and therefore should be represented as
                    // a non-complex Value
                    returnValue = new Value(dslType, object);
                }
            }
        }
        return returnValue;
    }

    // TODO: javadoc
    public Value translateRuntimeObject(
            Object object,
            IScope globalScope,
            IMemorySpace parentMemorySpace,
            IEvironment environment) {

        var objectsClass = object.getClass();
        var translator = this.translators.get(objectsClass);
        Value returnValue;
        if (translator == null) {
            returnValue = translateRuntimeObjectDefault(object, globalScope, parentMemorySpace, environment);
        } else {
            returnValue = translator.translate(object, globalScope, parentMemorySpace, environment);
        }
        return returnValue;
    }
}
