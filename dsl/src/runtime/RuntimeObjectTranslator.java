package runtime;

import semanticanalysis.types.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * This class performs conversions from "runtime objects", meaning Java-objects from the dungeon
 * context (Entity, Components) to DSL Values
 */
public class RuntimeObjectTranslator {
    // used to store specific IObjectToValueTranslator instances for classes, which
    // require custom logic to translate into a DSL Value
    private final HashMap<Class<?>, IObjectToValueTranslator> translators = new HashMap<>();

    /**
     * Register an IObjectToValueTranslator instance for a specific class
     *
     * @param clazz The Class to register the IObjectToValueTranslator for
     * @param translator The translator to use for specified Class
     */
    public void loadObjectToValueTranslator(Class<?> clazz, IObjectToValueTranslator translator) {
        if (this.translators.containsKey(clazz)) {
            throw new RuntimeException(
                    "RuntimeObjectTranslator for class '" + clazz + "' is already registered");
        }
        this.translators.put(clazz, translator);
    }

    /**
     * Default translation for given Object to a DSL Value. This will look up the equivalent
     * DSL-type for the Objects-Class and depending on the kind of type it will perform different
     * operations:
     *
     * <p>If the object is of basic type (bool, int, float), it will be wrapped in a normal Value
     *
     * <p>If the object is of an aggregateType, it will be wrapped in an EncapsulatedObject, which
     * will be used as a MemorySpace by an AggregateValue
     *
     * <p>If the object is of an adapted type (an external type, which can not be directly modified
     * to be a DSL-Value), it is wrapped in a normal Value
     *
     * @param object the object to translate into an DSL Value
     * @param parentMemorySpace the memory space in which to add the value
     * @param environment the IEnvironment containing all runtime type information
     * @return the translated Value
     */
    protected Value translateRuntimeObjectDefault(
            Object object, IMemorySpace parentMemorySpace, IEvironment environment, IType targetType) {
        Value returnValue = Value.NONE;

        IType dslType = targetType;
        if (dslType == null) {
            dslType = environment.getDSLTypeForClass(object.getClass());
        }
        if (dslType != BuiltInType.noType) {
            switch (dslType.getTypeKind()) {
                case Basic:
                case PODAdapted:
                case AggregateAdapted:
                    // if the type is adapted, it is an external type and therefore should be
                    // represented as a non-complex Value
                    returnValue = new Value(dslType, object);
                    break;
                case Aggregate:
                    var aggregateType = (AggregateType) dslType;

                    returnValue = new AggregateValue(aggregateType, parentMemorySpace, object);
                    var encapsulatedObject =
                            new EncapsulatedObject(
                                    object, aggregateType, parentMemorySpace, environment);
                    ((AggregateValue) returnValue).setMemorySpace(encapsulatedObject);
                    break;
                case FunctionType:
                    // TODO
                    break;
                case ListType:
                    ListType listType = (ListType) dslType;
                    // create new list value
                    ListValue listValue = new ListValue((ListType)dslType);

                    // translate each element to target type
                    List<?> passedList = (List<?>) object;
                    for (Object element : passedList) {
                        Value elementValue = translateRuntimeObject(element, parentMemorySpace, environment, listType.getElementType());
                        listValue.addValue(elementValue);
                    }
                    returnValue = listValue;
                    break;
                case SetType:
                    SetType setType = (SetType) dslType;
                    // create new list value
                    SetValue setValue = new SetValue((SetType)dslType);

                    // translate each element to target type
                    Set<?> passedSet = (Set<?>) object;
                    for (Object element : passedSet) {
                        Value elementValue = translateRuntimeObject(element, parentMemorySpace, environment, setType.getElementType());
                        setValue.addValue(elementValue);
                    }
                    returnValue = setValue;
                    boolean c = true;
                    break;
            }
        }
        return returnValue;
    }

    public Value translateRuntimeObject(
        Object object, IMemorySpace parentMemorySpace, IEvironment environment) {
        return translateRuntimeObject(object, parentMemorySpace, environment, null);
    }

    /**
     * Translate a given Object into a DSL Value. Performs the translation from a registered
     * IObjectToValueTranslator or performs the default translation
     *
     * @param object The Object to translate
     * @param parentMemorySpace The IMemorySpace in which to create the Value
     * @param environment The IEnvironment containing all type information
     * @return The translated Value
     */
    public Value translateRuntimeObject(
            Object object, IMemorySpace parentMemorySpace, IEvironment environment, IType targetType) {

        var objectsClass = object.getClass();
        var translator = this.translators.get(objectsClass);
        Value returnValue;
        if (translator == null) {
            returnValue = translateRuntimeObjectDefault(object, parentMemorySpace, environment, targetType);
        } else {
            returnValue = translator.translate(object, parentMemorySpace, environment);
        }
        return returnValue;
    }
}
