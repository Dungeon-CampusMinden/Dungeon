package dsl.semanticanalysis.typesystem.instantiation;

import dsl.annotation.DSLCallback;
import dsl.annotation.DSLContextMember;
import dsl.annotation.DSLTypeMember;
import dsl.annotation.DSLTypeNameMember;
import dsl.interpreter.DSLInterpreter;
import dsl.runtime.callable.ICallable;
import dsl.runtime.memoryspace.EncapsulatedObject;
import dsl.runtime.memoryspace.IMemorySpace;
import dsl.runtime.value.*;
import dsl.semanticanalysis.symbol.FunctionSymbol;
import dsl.semanticanalysis.symbol.PropertySymbol;
import dsl.semanticanalysis.typesystem.callbackadapter.CallbackAdapter;
import dsl.semanticanalysis.typesystem.callbackadapter.CallbackAdapterBuilder;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import dsl.semanticanalysis.typesystem.typebuilding.*;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateType;
import dsl.semanticanalysis.typesystem.typebuilding.type.AggregateTypeAdapter;
import dsl.semanticanalysis.typesystem.typebuilding.type.EnumType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/** WTF? . */
public class TypeInstantiator {
  private static final Set<IType.Kind> directlyConvertableTypeKinds =
      Collections.unmodifiableSet(
          EnumSet.of(
              IType.Kind.SetType,
              IType.Kind.ListType,
              IType.Kind.Basic,
              IType.Kind.EnumType,
              IType.Kind.FunctionType));
  private final HashMap<String, Object> context = new HashMap<>();
  private final CallbackAdapterBuilder callbackAdapterBuilder;

  /**
   * WTF? .
   *
   * @param interpreter foo
   */
  public TypeInstantiator(DSLInterpreter interpreter) {
    callbackAdapterBuilder = new CallbackAdapterBuilder(interpreter);
  }

  /**
   * Instantiate a new Object corresponding to the {@link AggregateType} of a {@link AggregateValue}
   * containing all needed values.
   *
   * @param value the aggregateValue to instantiate an object from
   * @return the instantiated object
   */
  public Object instantiate(Value value) {
    IType valuesType = value.getDataType();

    // instantiation of prototypes is handled by the native `instantiate` function
    if (valuesType instanceof PrototypeValue) {
      return null;
    }

    if (directlyConvertableTypeKinds.contains(valuesType.getTypeKind())) {
      return convertValueToObject(value);
    }

    AggregateType aggregateType = (AggregateType) valuesType;
    if (aggregateType.getTypeKind().equals(IType.Kind.AggregateAdapted)) {
      return convertValueToObject(value);
    }

    return instantiateAsType((AggregateValue) value, aggregateType);
  }

  /**
   * Instantiate a new Object corresponding to the passed {@link AggregateType} with the values from
   * an {@link AggregateValue}.
   *
   * @param value the aggregateValue to instantiate an object from
   * @param type the type to use for instantiation
   * @return the instantiated object
   */
  public Object instantiateAsType(AggregateValue value, AggregateType type) {
    Object instance = convertValueToObject(value, type);

    // set properties
    IMemorySpace ms = value.getMemorySpace();
    setProperties(instance, type, ms);

    return instance;
  }

  void setProperties(Object instance, AggregateType type, IMemorySpace ms) {
    var properties =
        type.getSymbols().stream()
            .filter(symbol -> symbol instanceof PropertySymbol)
            .map(symbol -> (PropertySymbol) symbol)
            .toList();

    for (PropertySymbol propertySymbol : properties) {
      IDSLExtensionProperty property = propertySymbol.getProperty();
      if (propertySymbol.isSettable()) {
        // get corresponding value from memorySpace
        Value value = ms.resolve(propertySymbol.getName());

        boolean isNoneOrEmptyAggregateValue =
            value == Value.NONE
                || (value instanceof AggregateValue aggregateValue && aggregateValue.isEmpty());
        if (!isNoneOrEmptyAggregateValue) {
          Object valueAsObject = convertValueToObject(value);
          if (valueAsObject != null) {
            property.set(instance, valueAsObject);
          }
        }
      }
    }
  }

  private Object instantiateEnum(EnumValue value) {
    var valuesDataType = (EnumType) value.getDataType();
    Class<? extends Enum> originType = valuesDataType.getOriginType();
    var variantSymbol = value.getEnumVariantSymbol();
    String variantName = variantSymbol.getName();

    // It is ensured in the TypeBuilder that the origin type of the EnumType
    // is actually an enum and the names of the Symbols representing the variants
    // do directly correlate to the name of the original enum variants
    @SuppressWarnings("unchecked")
    var enumInstance = Enum.valueOf(originType, variantName);
    return enumInstance;
  }

  private Object instantiateFunctionValue(FunctionValue value) {
    ICallable callable = value.getCallable();
    return this.callbackAdapterBuilder.buildAdapter(callable);
  }

  /**
   * Instantiate a {@link List} instance from a {@link ListValue}. Convert every entry of the {@link
   * ListValue} into an Object.
   *
   * @param listValue The ListValue to convert
   * @return the converted List
   */
  public List<?> instantiateList(ListValue listValue) {
    ArrayList arrayListInstance = new ArrayList<>();
    for (Value entryValue : (ArrayList<Value>) listValue.getInternalValue()) {
      var convertedEntryValue = convertValueToObject(entryValue);
      arrayListInstance.add(convertedEntryValue);
    }
    return arrayListInstance;
  }

  /**
   * Instantiate a {@link Set} instance from a {@link SetValue}. Convert every entry of the {@link
   * SetValue} into an Object.
   *
   * @param setValue The SetValue to convert
   * @return the converted Set
   */
  public Set<?> instantiateSet(SetValue setValue) {
    HashSet hashSetInstance = new HashSet<>();
    for (Value entryValue : (HashSet<Value>) setValue.getInternalValue()) {
      var convertedEntryValue = convertValueToObject(entryValue);
      hashSetInstance.add(convertedEntryValue);
    }
    return hashSetInstance;
  }

  /**
   * WTF? .
   *
   * @param mapValue foo
   * @return foo
   */
  public Map<?, ?> instantiateMap(MapValue mapValue) {
    HashMap<Object, Object> hashMapInstance = new HashMap<>();
    HashMap<Value, Value> internalMap = mapValue.internalMap();

    for (var mapEntry : internalMap.entrySet()) {
      Value keyValue = mapEntry.getKey();
      var convertedKeyValue = convertValueToObject(keyValue);
      Value entryValue = mapEntry.getValue();
      var convertedEntryValue = convertValueToObject(entryValue);
      hashMapInstance.put(convertedKeyValue, convertedEntryValue);
    }
    return hashMapInstance;
  }

  /**
   * Push an object as part of the context (so it can be looked up, if it is referenced by {@link
   * DSLContextMember} by a constructor parameter).
   *
   * @param name the name to use for the contextMember
   * @param contextMember the Object to push
   */
  public void pushContextMember(String name, Object contextMember) {
    context.put(name, contextMember);
  }

  /**
   * Remove a context member with name.
   *
   * @param name the name of the context member to remove
   */
  public void removeContextMember(String name) {
    context.remove(name);
  }

  private Object convertValueToObject(Value value) {
    var valuesType = value.getDataType();
    return convertValueToObject(value, valuesType);
  }

  /**
   * Converts a {@link Value} to a regular Java Object. The conversion is dependent on the kind of
   * datatype of the {@link Value} instance.
   *
   * @param value the Value to convert
   * @param valuesType foo
   * @return the converted Object
   */
  private Object convertValueToObject(Value value, IType valuesType) {
    Object convertedObject = value.getInternalValue();
    try {
      if (valuesType.getTypeKind().equals(IType.Kind.AggregateAdapted)) {
        var aggregateFieldValue = (AggregateValue) value;
        if (aggregateFieldValue.getMemorySpace() instanceof EncapsulatedObject) {
          // if the memoryspace of the value already encapsulates an object,
          // return this object
          convertedObject = value.getInternalValue();
        } else {
          // if the value is a prototype, instantiation is handled by
          // DSLInterpreter::instantiateDSLValue and subsequent calls to
          // instantiateRuntimeValue; don't do it here
          if (valuesType instanceof PrototypeValue) {
            return null;
          }
          // call builder -> store values from memory space in order of parameters
          // of builder-method
          var adaptedType = (AggregateTypeAdapter) valuesType;
          var method = adaptedType.builderMethod();
          var parameters = new ArrayList<>(method.getParameterCount());
          for (var parameter : method.getParameters()) {
            String memberName;
            if (parameter.isAnnotationPresent(DSLTypeNameMember.class)) {
              memberName = AggregateType.NAME_SYMBOL_NAME;
            } else {
              memberName = TypeBuilder.getDSLParameterName(parameter);
            }
            Value memberValue = aggregateFieldValue.getMemorySpace().resolve(memberName);
            Object internalObject;
            if (parameter.isAnnotationPresent(DSLContextMember.class)) {
              String name = parameter.getAnnotation(DSLContextMember.class).name();
              internalObject = context.get(name);
            } else {
              internalObject = convertValueToObject(memberValue);
            }
            parameters.add(internalObject);
          }

          convertedObject = method.invoke(null, parameters.toArray());
        }
      } else if (valuesType.getTypeKind().equals(IType.Kind.ListType)) {
        convertedObject = instantiateList((ListValue) value);
      } else if (valuesType.getTypeKind().equals(IType.Kind.SetType)) {
        convertedObject = instantiateSet((SetValue) value);
      } else if (valuesType.getTypeKind().equals(IType.Kind.MapType)) {
        convertedObject = instantiateMap((MapValue) value);
      } else if (valuesType.getTypeKind().equals(IType.Kind.EnumType)) {
        convertedObject = instantiateEnum((EnumValue) value);
      } else if (valuesType.getTypeKind().equals(IType.Kind.FunctionType)) {
        convertedObject = instantiateFunctionValue((FunctionValue) value);
      } else if (valuesType.getTypeKind().equals(IType.Kind.Aggregate)) {
        if (convertedObject == null) {
          // if the value is a prototype, instantiation is handled by
          // DSLInterpreter::instantiateDSLValue and subsequent calls to
          // instantiateRuntimeValue; don't do it here
          if (valuesType instanceof PrototypeValue) {
            return null;
          }
          var originalJavaClass = ((AggregateType) valuesType).getOriginType();
          if (null == originalJavaClass) {
            return null;
          }

          if (originalJavaClass.isRecord()) {
            convertedObject = instantiateRecord(originalJavaClass, value);
          } else {
            convertedObject =
                instantiateAggregateValueAsClass(
                    (AggregateType) valuesType, (AggregateValue) value);
          }
        }
      }
    } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return convertedObject;
  }

  private Object instantiateRecord(Class<?> originalJavaClass, Value value) {
    IMemorySpace ms = value.getMemorySpace();

    Constructor<?> ctor = getConstructor(originalJavaClass);
    if (null == ctor) {
      throw new RuntimeException(
          "Could not find a suitable constructor to instantiate record "
              + originalJavaClass.getName());
    }

    try {
      // find the corresponding record-field to the constructor-parameter, get the according
      // value from the memory space and pass it as a parameter to the constructor
      ArrayList<Object> parameters = new ArrayList<>(ctor.getParameters().length);
      for (var param : ctor.getParameters()) {
        var field = originalJavaClass.getDeclaredField(param.getName());
        if (field.isAnnotationPresent(DSLTypeMember.class)) {
          String fieldName = TypeBuilder.getDSLFieldName(field);
          var fieldValue = ms.resolve(fieldName);

          // if a certain value is not found in the memory space,
          // the record cannot be instantiated -> early return
          if (fieldValue == null || fieldValue == Value.NONE) {
            throw new RuntimeException(
                "The name of field "
                    + field.getName()
                    + " cannot be resolved in the supplied memory space");
          } else {
            Object internalValue = convertValueToObject(fieldValue);
            parameters.add(internalValue);
          }
        } else if (field.isAnnotationPresent(DSLCallback.class)) {
          String fieldName = TypeBuilder.getDSLFieldName(field);
          var fieldValue = ms.resolve(fieldName);

          assert fieldValue.getDataType().getTypeKind() == IType.Kind.FunctionType;
          assert fieldValue.getInternalValue() instanceof FunctionSymbol;

          CallbackAdapter adapter =
              callbackAdapterBuilder.buildAdapter((FunctionSymbol) fieldValue.getInternalValue());
          parameters.add(adapter);
        } else {
          throw new RuntimeException(
              "Instantiating a record using the TypeInstantiator requires that all "
                  + "record members must be marked with @DSLTypeMember. Otherwise, no constructor invocation is possible");
        }
      }
      ctor.setAccessible(true);
      return ctor.newInstance(parameters.toArray());
    } catch (NoSuchFieldException
        | InvocationTargetException
        | InstantiationException
        | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private Object instantiateAggregateValueAsClass(AggregateType type, AggregateValue value) {
    var originalJavaClass = type.getOriginType();
    if (null == originalJavaClass) {
      return null;
    }
    if (originalJavaClass.isMemberClass()) {
      throw new RuntimeException("Cannot instantiate an inner class");
    }

    Constructor<?> ctor = getConstructor(originalJavaClass);
    if (null == ctor) {
      throw new RuntimeException(
          "Could not find a suitable constructor to instantiate class "
              + originalJavaClass.getName());
    }

    Object instance;
    try {
      // get constructor
      ctor.setAccessible(true);
      ArrayList<Object> parameterValues = new ArrayList<>(ctor.getParameterCount());
      for (var param : ctor.getParameters()) {
        if (param.isAnnotationPresent(DSLContextMember.class)) {
          String contextMemberName = param.getAnnotation(DSLContextMember.class).name();
          Object contextMember = context.get(contextMemberName);
          parameterValues.add(contextMember);
        } else {
          throw new RuntimeException(
              "Constructor parameter with name "
                  + param.getName()
                  + " is not marked as context parameter, cannot "
                  + "instantiate class "
                  + originalJavaClass.getName());
        }
      }

      instance = ctor.newInstance(parameterValues.toArray());

      // set values of the fields marked as DSLTypeMembers to corresponding values from
      // the memory space
      IMemorySpace ms = value.getMemorySpace();
      for (Field field : originalJavaClass.getDeclaredFields()) {
        String fieldName = TypeBuilder.getDSLFieldName(field);
        var fieldValue = ms.resolve(fieldName);
        if (field.isAnnotationPresent(DSLTypeMember.class)) {
          // we only should set the field value explicitly,
          // if it was set in the program (indicated by the dirty-flag)
          if (fieldValue != Value.NONE && fieldValue.isDirty()) {
            Object internalValue = convertValueToObject(fieldValue);

            field.setAccessible(true);
            field.set(instance, internalValue);
          }
        }
        if (field.isAnnotationPresent(DSLCallback.class)) {
          if (fieldValue != Value.NONE
              && fieldValue != FunctionValue.NONE
              && fieldValue instanceof FunctionValue funcValue
              && !funcValue.isEmpty()) {
            assert fieldValue.getDataType().getTypeKind() == IType.Kind.FunctionType;
            if (!(funcValue.getCallable() instanceof FunctionSymbol functionSymbol)) {
              throw new RuntimeException(
                  "Usage of non-FunctionSymbol callables as DSLCallback currently not supported");
            } else {
              CallbackAdapter adapter = callbackAdapterBuilder.buildAdapter(functionSymbol);
              field.setAccessible(true);
              field.set(instance, adapter);
            }
          }
        }
      }
    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    return instance;
  }

  private Constructor<?> getConstructor(Class<?> originalJavaClass) {
    Constructor<?> ctor = null;
    for (Constructor<?> constructor : originalJavaClass.getDeclaredConstructors()) {
      ctor = constructor;
      boolean unmarkedCtorParameter = false;
      for (var parameter : ctor.getParameters()) {
        if (!parameter.isAnnotationPresent(DSLContextMember.class)) {
          unmarkedCtorParameter = true;
          break;
        }
      }

      if (!unmarkedCtorParameter) {
        break;
      }
    }

    return ctor;
  }
}
