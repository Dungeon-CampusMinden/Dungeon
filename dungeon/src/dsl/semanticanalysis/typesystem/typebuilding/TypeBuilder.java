package dsl.semanticanalysis.typesystem.typebuilding;

import core.utils.TriConsumer;
import dsl.annotation.*;
import dsl.runtime.callable.ExtensionMethod;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.PropertySymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionMethod;
import dsl.semanticanalysis.typesystem.extension.IDSLExtensionProperty;
import dsl.semanticanalysis.typesystem.typebuilding.type.*;
import graph.taskdependencygraph.TaskDependencyGraph;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** TypeBuilder. */
public class TypeBuilder {
  private final HashMap<Class<?>, List<Method>> typeAdapters;
  private final HashMap<Type, IType> javaTypeToDSLType;
  private final HashSet<Type> currentLookedUpTypes;
  private final HashMap<Class<?>, IFunctionTypeBuilder> functionTypeBuilders;

  /** Constructor. */
  public TypeBuilder() {
    this.typeAdapters = new HashMap<>();
    this.javaTypeToDSLType = new HashMap<>();
    this.currentLookedUpTypes = new HashSet<>();
    this.functionTypeBuilders = new HashMap<>();

    setupFunctionTypeBuilders();
  }

  private void setupFunctionTypeBuilders() {
    functionTypeBuilders.put(Consumer.class, ConsumerFunctionTypeBuilder.instance);
    functionTypeBuilders.put(TriConsumer.class, ConsumerFunctionTypeBuilder.instance);
    functionTypeBuilders.put(BiConsumer.class, ConsumerFunctionTypeBuilder.instance);
    functionTypeBuilders.put(Function.class, FunctionFunctionTypeBuilder.instance);
    functionTypeBuilders.put(BiFunction.class, BiFunctionFunctionTypeBuilder.instance);
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public HashMap<Type, IType> getJavaTypeToDSLTypeMap() {
    // create copy of the hashmap
    return new HashMap<>(javaTypeToDSLType);
  }

  /**
   * WTF? .
   *
   * @param callbackClass foo
   * @return foo
   */
  public IFunctionTypeBuilder getFunctionTypeBuilder(Class<?> callbackClass) {
    // create copy of the hashmap
    return this.functionTypeBuilders.get(callbackClass);
  }

  /**
   * Replaces all small letters followed by a capital letter in small letters spaced by '_'.
   *
   * @param name the name to convert
   * @return converted name
   */
  public static String convertToDSLName(String name) {
    Pattern pattern = Pattern.compile("([a-z0-9_])([A-Z])");
    Matcher matcher = pattern.matcher(name);
    var underscored = matcher.replaceAll(mr -> mr.group(1) + '_' + mr.group(2).toLowerCase());
    return underscored.toLowerCase();
  }

  /**
   * @param type the class to get the corresponding {@link IType} for
   * @return the corresponding {@link IType} for the passed type, or null, if the passed type does
   *     not correspond to a basic type
   */
  public static IType getBuiltInDSLType(Type type) {
    boolean canBeCastToClass = true;
    Class<?> clazz = null;
    try {
      clazz = (Class<?>) type;
    } catch (ClassCastException ex) {
      canBeCastToClass = false;
    }
    if (!canBeCastToClass) {
      return null;
    }
    // check for basic types
    if (int.class.equals(clazz)
        || short.class.equals(clazz)
        || long.class.equals(clazz)
        || Integer.class.isAssignableFrom(clazz)) {
      return BuiltInType.intType;
    } else if (float.class.equals(clazz)
        || double.class.equals(clazz)
        || Float.class.isAssignableFrom(clazz)) {
      return BuiltInType.floatType;
    } else if (boolean.class.equals(clazz) || Boolean.class.isAssignableFrom(clazz)) {
      return BuiltInType.boolType;
    } else if (String.class.equals(clazz) || String.class.isAssignableFrom(clazz)) {
      return BuiltInType.stringType;
    } else if (Void.class.isAssignableFrom(clazz)) {
      return BuiltInType.noType;
    } else if (TaskDependencyGraph.class.equals(clazz)
        || TaskDependencyGraph.class.isAssignableFrom(clazz)) {
      return BuiltInType.graphType;
    }

    return null;
  }

  /**
   * Generate a map, which maps the member names of an DSL {@link AggregateType} to the Fields of
   * it's origin java class.
   *
   * @param type the type
   * @return the map, containing mapping between member names and java field names
   */
  public static HashMap<String, Field> mapTypeMembersToField(AggregateType type) {
    var originClass = type.getOriginType();
    HashMap<String, String> nameMap = new HashMap<>();
    for (Field field : originClass.getDeclaredFields()) {
      // bind new Symbol
      if (field.isAnnotationPresent(DSLTypeMember.class)
          || field.isAnnotationPresent(DSLCallback.class)) {
        String fieldName = getDSLFieldName(field);
        nameMap.put(fieldName, field.getName());
      }
    }

    HashMap<String, Field> typeMemberToField = new HashMap<>();
    for (var member : type.getSymbols()) {
      var fieldName = nameMap.get(member.getName());
      if (fieldName != null) {
        try {
          Field field = originClass.getDeclaredField(fieldName);
          typeMemberToField.put(member.getName(), field);
        } catch (NoSuchFieldException e) {
          // TODO: handle
        }
      }
    }
    return typeMemberToField;
  }

  protected static String getDSLNameOfBasicType(Class<?> clazz) {
    var basicType = getBuiltInDSLType(clazz);
    return basicType != null ? basicType.getName() : "";
  }

  /**
   * @param clazz the Class to get the DSL name for
   * @return converted name (conversion by {@link #convertToDSLName(String)}) or the 'name'
   *     parameter of {@link DSLType}
   */
  public static String getDSLTypeName(Class<?> clazz) {
    // check for basic type
    String dslName = getDSLNameOfBasicType(clazz);
    if (dslName.isEmpty()) {
      var classAnnotation = clazz.getAnnotation(DSLType.class);
      return classAnnotation == null || classAnnotation.name().equals("")
          ? convertToDSLName(clazz.getSimpleName())
          : classAnnotation.name();
    } else {
      return dslName;
    }
  }

  /**
   * @param field the field to get the DSL name for
   * @return converted name (conversion by {@link #convertToDSLName(String)}) or the 'name'
   *     parameter of {@link DSLTypeMember}
   */
  public static String getDSLFieldName(Field field) {
    var fieldAnnotation = field.getAnnotation(DSLTypeMember.class);
    return fieldAnnotation == null || fieldAnnotation.name().equals("")
        ? convertToDSLName(field.getName())
        : fieldAnnotation.name();
  }

  /**
   * @param parameter the parameter to get the DSL name for
   * @return converted name (conversion by {@link #convertToDSLName(String)}) or the 'name'
   *     parameter of {@link DSLTypeMember}
   */
  public static String getDSLParameterName(Parameter parameter) {
    var parameterAnnotation = parameter.getAnnotation(DSLTypeMember.class);
    return parameterAnnotation == null || parameterAnnotation.name().equals("")
        ? convertToDSLName(parameter.getName())
        : parameterAnnotation.name();
  }

  /**
   * A method to check if the parameter types of two given methods match.
   *
   * @param m1 foo
   * @param m2 foo
   * @return foo
   */
  public static boolean doParameterTypesMatch(Method m1, Method m2) {
    // check, if registered adapter matches signature of new adapter
    if (m1.getParameterCount() != m2.getParameterCount()) {
      return false;
    }
    boolean parametersMatch = true;
    for (int i = 0; parametersMatch && i < m1.getParameterCount(); i++) {
      Class<?> m1Parameter = m1.getParameterTypes()[i];
      Class<?> m2Parameter = m2.getParameterTypes()[i];
      parametersMatch = m2Parameter.equals(m1Parameter);
    }
    return parametersMatch;
  }

  /**
   * Register a new type adapter (which will be used to instantiate a class, which is not converted
   * to a DSLType).
   *
   * @param adapterClass the adapter to register
   * @param parentScope the scope in which the adapter should be registered
   */
  public void registerTypeAdapter(Class<?> adapterClass, IScope parentScope) {
    for (var method : adapterClass.getDeclaredMethods()) {
      if (method.isAnnotationPresent(DSLTypeAdapter.class)
          && Modifier.isStatic(method.getModifiers())) {
        DSLTypeAdapter annotation = method.getAnnotation(DSLTypeAdapter.class);

        var forType = method.getReturnType();
        if (!this.typeAdapters.containsKey(forType)) {
          this.typeAdapters.put(forType, new ArrayList<>());
        }

        List<Method> typeAdaptersForType = this.typeAdapters.get(forType);
        for (Method adapter : typeAdaptersForType) {
          if (doParameterTypesMatch(adapter, method)) {
            throw new UnsupportedOperationException(
                "An adapter for class "
                    + forType.getName()
                    + " with the same signature was already registered");
          }
        }

        this.typeAdapters.get(forType).add(method);

        String dslTypeName =
            annotation.name().equals("")
                ? convertToDSLName(forType.getSimpleName())
                : annotation.name();

        createAdapterType(forType, dslTypeName, method, parentScope);

        return;
      }
    }
  }

  /**
   * WTF? .
   *
   * @param forType foo
   * @param dslTypeName foo
   * @param adapterMethod foo
   * @param parentScope foo
   * @return foo
   */
  public IType createAdapterType(
      Class<?> forType, String dslTypeName, Method adapterMethod, IScope parentScope) {
    if (adapterMethod.getParameterCount() == 0) {
      // TODO: handle
      throw new RuntimeException("Builder methods with zero arguments are currently not supported");
    }

    var typeAdapter = new AggregateTypeAdapter(dslTypeName, parentScope, forType, adapterMethod);
    this.javaTypeToDSLType.put(forType, typeAdapter);
    parentScope.bind(typeAdapter);

    // bind symbol for each parameter in the adapterMethod
    for (var parameter : adapterMethod.getParameters()) {
      // translate parameters type into DSL type system
      Type parametersType = parameter.getType();
      var parametersAnnotatedType = parameter.getAnnotatedType();

      // if the underlying Type of the AnnotatedType is not equal to the plain Type returned
      // by parameter.getType(), then the parameter is declared with an annotated type (List,
      // Set, Map or
      // an implementation of a functional interface) and we need to either create a
      // FunctionType from
      // it (see below) or pass the Type with annotation information to
      // createDSLTypeForJavaTypeInScope
      var underlyingType = parametersAnnotatedType.getType();
      boolean typeIsAnnotated = !underlyingType.equals(parametersType);

      IType paramDSLType;
      if (typeIsAnnotated) {
        Class<?> parametersClass = (Class<?>) parametersType;
        if (parametersClass.isAnnotationPresent(FunctionalInterface.class)) {
          // If the parameters class is annotated with @FunctionalInterface, we need to
          // create a FunctionType for the parameter. For this we
          // need the *Parameterized* Type of the parameter (which stores the information
          // about the
          // types used in the declaration of the generic type, i.e. `Integer` in
          // `List<Integer>`).
          // This CANNOT be integrated in `createDSLTypeForJavaTypeInScope` (see below),
          // because
          // the *Parameterized* Type is ONLY accessible via the Parameter of the method,
          // which is
          // not available in the `createDSLTypeForJavaTypeInScope`-method!
          var parameterizedParameterType = (ParameterizedType) parameter.getParameterizedType();
          paramDSLType = createFunctionType(parameterizedParameterType, parentScope);
        } else {
          paramDSLType =
              createDSLTypeForJavaTypeInScope(parentScope, parametersAnnotatedType.getType());
        }
      } else {
        paramDSLType = createDSLTypeForJavaTypeInScope(parentScope, parametersType);
      }

      String parameterName;
      if (parameter.isAnnotationPresent(DSLTypeNameMember.class)) {
        parameterName = AggregateType.NAME_SYMBOL_NAME;
      } else {
        parameterName = getDSLParameterName(parameter);
      }

      Symbol parameterSymbol = new Symbol(parameterName, typeAdapter, paramDSLType);
      typeAdapter.bind(parameterSymbol);
    }
    return typeAdapter;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Set<Map.Entry<Class<?>, List<Method>>> getRegisteredTypeAdapters() {
    return this.typeAdapters.entrySet();
  }

  /**
   * WTF? .
   *
   * @param clazz foo
   * @return foo
   */
  public List<Method> getRegisteredTypeAdaptersForType(Class<?> clazz) {
    return this.typeAdapters.getOrDefault(clazz, new ArrayList<>());
  }

  protected IType bindOrResolveTypeInScope(IType type, IScope scope) {
    IType returnedType = type;
    Symbol resolvedType = scope.resolve(type.getName());
    if (resolvedType != Symbol.NULL) {
      if (resolvedType instanceof IType) {
        returnedType = (IType) resolvedType;
      } else {
        // symbol with the name of the function type is already bound in
        // global scope but not a type
        throw new RuntimeException(
            "A symbol with the name "
                + type.getName()
                + " is already bound in the global scope but is not a type");
      }
    } else {
      // bind newly created function type in the global scope
      scope.bind((Symbol) type);
    }
    return returnedType;
  }

  // create a symbol in parentType for given field, representing a callback
  protected Symbol createCallbackMemberSymbol(
      Field field, AggregateType parentType, IScope globalScope) {
    String callbackName = getDSLFieldName(field);
    IType callbackType =
        createFunctionType((ParameterizedType) field.getGenericType(), globalScope);

    return new Symbol(callbackName, parentType, callbackType);
  }

  protected IType createFunctionType(
      ParameterizedType parameterizedFunctionalInterfaceType, IScope globalScope) {
    var rawType = parameterizedFunctionalInterfaceType.getRawType();
    var functionTypeBuilder = functionTypeBuilders.get(rawType);

    IType functionType = BuiltInType.noType;
    if (functionTypeBuilder != null) {
      functionType =
          functionTypeBuilder.buildFunctionType(
              parameterizedFunctionalInterfaceType, this, globalScope);
      functionType = bindOrResolveTypeInScope(functionType, globalScope);
    }

    return functionType;
  }

  /**
   * Create a new {@link SetType} from the passed {@link ParameterizedType}.
   *
   * @param setType the {@link ParameterizedType} to convert into a {@link SetType}
   * @param globalScope foo
   * @return the created type
   */
  public IType createSetType(ParameterizedType setType, IScope globalScope) {
    var elementType = setType.getActualTypeArguments()[0];
    IType elementDSLType = this.createDSLTypeForJavaTypeInScope(globalScope, elementType);

    if (javaTypeToDSLType.get(setType) == null) {
      IType dslSetType = new SetType(elementDSLType, globalScope);
      dslSetType = bindOrResolveTypeInScope(dslSetType, globalScope);
      javaTypeToDSLType.put(setType, dslSetType);
    }
    return javaTypeToDSLType.get(setType);
  }

  /**
   * WTF? .
   *
   * @param mapType foo
   * @param globalScope foo
   * @return foo
   */
  public IType createMapType(ParameterizedType mapType, IScope globalScope) {
    var keyType = mapType.getActualTypeArguments()[0];
    IType keyDSLType = this.createDSLTypeForJavaTypeInScope(globalScope, keyType);

    var elementType = mapType.getActualTypeArguments()[1];
    IType elementDSLType = this.createDSLTypeForJavaTypeInScope(globalScope, elementType);

    if (javaTypeToDSLType.get(mapType) == null) {
      IType dslMapType = new MapType(keyDSLType, elementDSLType, globalScope);
      dslMapType = bindOrResolveTypeInScope(dslMapType, globalScope);
      javaTypeToDSLType.put(mapType, dslMapType);
    }
    return javaTypeToDSLType.get(mapType);
  }

  /**
   * Create a new {@link ListType} from the passed {@link ParameterizedType}.
   *
   * @param listType the {@link ParameterizedType} to convert into a {@link ListType}
   * @param globalScope foo
   * @return the created type
   */
  public IType createListType(ParameterizedType listType, IScope globalScope) {
    var elementType = listType.getActualTypeArguments()[0];
    IType elementDSLType = this.createDSLTypeForJavaTypeInScope(globalScope, elementType);

    if (javaTypeToDSLType.get(listType) == null) {
      IType dslListType = new ListType(elementDSLType, globalScope);
      dslListType = bindOrResolveTypeInScope(dslListType, globalScope);
      javaTypeToDSLType.put(listType, dslListType);
    }
    return javaTypeToDSLType.get(listType);
  }

  protected IType getFieldsDSLType(Field field, Class<?> fieldsType, IScope globalScope) {
    // get datatype
    var fieldsDSLType = getBuiltInDSLType(fieldsType);
    if (fieldsDSLType == null) {
      // is list or set?
      if (List.class.isAssignableFrom(fieldsType)) {
        fieldsDSLType = createListType((ParameterizedType) field.getGenericType(), globalScope);
      } else if (Set.class.isAssignableFrom(fieldsType)) {
        fieldsDSLType = createSetType((ParameterizedType) field.getGenericType(), globalScope);
      } else if (Map.class.isAssignableFrom(fieldsType)) {
        fieldsDSLType = createMapType((ParameterizedType) field.getGenericType(), globalScope);
      }
    }
    if (fieldsDSLType == null) {
      // lookup the type in already converted types
      // if it is not already in the converted types, try to convert it -> check for
      // DSLType
      // annotation
      fieldsDSLType = createDSLTypeForJavaTypeInScope(globalScope, field.getType());
    }
    return fieldsDSLType;
  }

  protected Symbol createDataMemberSymbolWithTemplateType(
      Field field,
      Class<?> parentClass,
      AggregateType parentType,
      IScope globalScope,
      Type[] templateTypes) {

    var genericType = field.getGenericType();
    var typeParameters = parentClass.getTypeParameters();

    // find index of type parameter
    Type declaredTemplateType = null;
    for (int i = 0; i < typeParameters.length && i < templateTypes.length; i++) {
      var typeParameter = typeParameters[i];
      if (typeParameter.equals(genericType)) {
        declaredTemplateType = templateTypes[i];
      }
    }
    if (declaredTemplateType == null) {
      throw new RuntimeException("Could not find a type for template types!");
    }

    Class<?> fieldsType = (Class<?>) declaredTemplateType;
    IType fieldsDSLType = getFieldsDSLType(field, fieldsType, globalScope);

    String fieldName;
    if (field.isAnnotationPresent(DSLTypeNameMember.class)) {
      fieldName = AggregateType.NAME_SYMBOL_NAME;
    } else {
      fieldName = getDSLFieldName(field);
    }
    return new Symbol(fieldName, parentType, fieldsDSLType);
  }

  // create a symbol in parentType for given field, representing data in parentClass
  protected Symbol createDataMemberSymbol(
      Field field, AggregateType parentType, IScope globalScope) {

    Class<?> fieldsType = field.getType();
    IType fieldsDSLType = getFieldsDSLType(field, fieldsType, globalScope);

    String fieldName;
    if (field.isAnnotationPresent(DSLTypeNameMember.class)) {
      fieldName = AggregateType.NAME_SYMBOL_NAME;
    } else {
      fieldName = getDSLFieldName(field);
    }
    return new Symbol(fieldName, parentType, fieldsDSLType);
  }

  /**
   * Creates a DSL {@link IType} from a java {@link Type}. Based on the kind of passed {@link Type},
   * different kinds of {@link IType} will be created. The most common scenario is the creation of
   * an {@link AggregateType} from a class or a record. This requires the class to be marked * with
   * the {@link DSLType} annotation. Each field marked with the {@link DSLTypeMember} annotation
   * will be converted to a member of the created {@link AggregateType}, if the field's type can be
   * mapped to a DSL data type. This requires the field's type to be either one of the types
   * declared in {@link BuiltInType} or another class marked with {@link DSLType}. If the passed
   * {@link Type} implements {@link ParameterizedType}, it will either be converted into a {@link
   * ListType} or {@link SetType}, if it assignable to {@link List} or {@link Set} respectively. If
   * the name of the newly created type can be resolved in the passed {@link IScope}, the resolved
   * {@link IType} will be returned.
   *
   * @param globalScope the global scope to use for resolving any DSL datatype
   * @param type the java {@link Type} to create a DSL {@link IType} from
   * @return foo
   */
  public IType createDSLTypeForJavaTypeInScope(IScope globalScope, Type type) {
    IType returnType;
    if (type == null) {
      return BuiltInType.noType;
    }

    // catch recursion
    if (this.currentLookedUpTypes.contains(type)) {
      throw new RuntimeException("RECURSIVE TYPE DEF");
    }

    if (this.javaTypeToDSLType.containsKey(type)) {
      var returnedType = this.javaTypeToDSLType.get(type);
      return this.javaTypeToDSLType.get(type);
    }

    var builtInType = getBuiltInDSLType(type);
    if (builtInType != null) {
      return builtInType;
    }

    // Try to cast the passed Type to Class<?> (needed for further operations).
    // The passed Type will be either a Class<?> or a ParameterizedType (used to
    // create List- and Set-Types).
    Class<?> clazz = null;
    try {
      clazz = (Class<?>) type;
    } catch (ClassCastException ex) {
      if (type instanceof ParameterizedType parameterizedType) {
        var rawType = parameterizedType.getRawType();
        try {
          clazz = (Class<?>) rawType;
        } catch (ClassCastException exc) {
          throw new UnsupportedOperationException(
              "The TypeBuilder does not support conversion of type " + type);
        }

        // if the cast fails, the type may be a parameterized type (e.g. list or set)
        if (List.class.isAssignableFrom(clazz)) {
          return createListType((ParameterizedType) type, globalScope);
        } else if (Set.class.isAssignableFrom(clazz)) {
          return createSetType((ParameterizedType) type, globalScope);
        } else if (Map.class.isAssignableFrom(clazz)) {
          return createMapType((ParameterizedType) type, globalScope);
        }
      }
    }

    // try to resolve the typename in global scope
    String typeName = getDSLTypeName(clazz);
    Symbol resolved = globalScope.resolve(typeName);
    if (resolved != Symbol.NULL) {
      if (resolved instanceof IType) {
        return (IType) resolved;
      } else {
        // symbol with the typename is already bound in the global scope
        // but is not a type
        throw new RuntimeException(
            "Symbol with name "
                + typeName
                + " is already bound in global scope, "
                + "but not a type");
      }
    }

    if (!clazz.isAnnotationPresent(DSLType.class)) {
      return null;
    }
    DSLType dslTypeAnnotation = clazz.getAnnotation(DSLType.class);

    if (clazz.isEnum()) {
      // because we check, that the clazz is an Enum (by `.isEnum()`)
      // we can ignore the unchecked warning
      @SuppressWarnings("unchecked")
      Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) clazz;

      var enumType = new EnumType(typeName, globalScope, enumClass);
      var variants = clazz.getDeclaredFields();
      for (var variant : variants) {
        var variantsType = variant.getType();
        if (variantsType.isArray()) {
          continue;
        }

        String name = variant.getName();
        Symbol variantSymbol = new Symbol(name, enumType, enumType);
        enumType.bind(variantSymbol);
      }
      returnType = enumType;
    } else {
      // create new AggregateType for clazz
      var aggregateType = new AggregateType(typeName, globalScope, clazz);

      this.currentLookedUpTypes.add(clazz);
      for (Field field : clazz.getDeclaredFields()) {
        // bind new Symbol
        if (field.isAnnotationPresent(DSLTypeMember.class)
            || field.isAnnotationPresent(DSLTypeNameMember.class)) {
          var genericType = field.getGenericType();
          Symbol fieldSymbol;
          if (genericType instanceof TypeVariable<?>
              && dslTypeAnnotation.templateArguments().length > 0) {
            fieldSymbol =
                createDataMemberSymbolWithTemplateType(
                    field,
                    clazz,
                    aggregateType,
                    globalScope,
                    dslTypeAnnotation.templateArguments());
          } else {
            fieldSymbol = createDataMemberSymbol(field, aggregateType, globalScope);
          }
          aggregateType.bind(fieldSymbol);
        }
        if (field.isAnnotationPresent(DSLCallback.class)) {
          var callbackSymbol = createCallbackMemberSymbol(field, aggregateType, globalScope);
          aggregateType.bind(callbackSymbol);
        }
      }
      this.currentLookedUpTypes.remove(clazz);

      var typeMemberToFieldMap = mapTypeMembersToField(aggregateType);
      aggregateType.setTypeMemberToField(typeMemberToFieldMap);

      returnType = aggregateType;
    }

    this.javaTypeToDSLType.put(clazz, returnType);
    globalScope.bind((Symbol) returnType);
    return returnType;
  }

  /**
   * Bind a {@link IDSLExtensionProperty} as an {@link PropertySymbol} in the DSL datatype
   * corresponding to the {@link DSLTypeProperty#extendedType()} field.
   *
   * @param globalScope the global scope to use for resolving data types
   * @param property the {@link IDSLExtensionProperty} to bind
   */
  public void bindProperty(IScope globalScope, IDSLExtensionProperty<?, ?> property) {
    // get extended type
    Class<?> propertyClass = property.getClass();
    if (propertyClass.isAnnotationPresent(DSLTypeProperty.class)) {
      var annotation = propertyClass.getAnnotation(DSLTypeProperty.class);
      var extendedClass = annotation.extendedType();
      IType extendedType = createDSLTypeForJavaTypeInScope(globalScope, extendedClass);
      if (extendedType instanceof AggregateType aggregateExtendedType) {
        var genericInterfaces = propertyClass.getGenericInterfaces();
        var type = genericInterfaces[0];
        ParameterizedType parameterizedType = (ParameterizedType) type;

        // get properties datatype
        var valueType = parameterizedType.getActualTypeArguments()[1];
        IType valueDSLType = null;
        if (valueType instanceof ParameterizedType parameterizedParameterType) {
          try {
            Class<?> rawType = (Class<?>) parameterizedParameterType.getRawType();
            if (rawType.isAnnotationPresent(FunctionalInterface.class)) {
              valueDSLType = this.createFunctionType(parameterizedParameterType, globalScope);
            }
          } catch (ClassCastException ex) {
            //
          }
        }
        if (valueDSLType == null) {
          valueDSLType = createDSLTypeForJavaTypeInScope(globalScope, valueType);
        }

        // create and bind property symbol
        PropertySymbol propertySymbol =
            new PropertySymbol(annotation.name(), aggregateExtendedType, valueDSLType, property);
        aggregateExtendedType.bind(propertySymbol);
      }
    }
  }

  /**
   * Bind a {@link IDSLExtensionMethod} as an {@link ExtensionMethod} symbol in the DSL datatype
   * corresponding to the {@link DSLExtensionMethod#extendedType()} field.
   *
   * @param globalScope the global scope to use for resolving data types
   * @param method the {@link IDSLExtensionMethod} to bind
   */
  public void bindMethod(IScope globalScope, IDSLExtensionMethod<?, ?> method) {
    // get extended type
    Class<?> methodClass = method.getClass();
    if (methodClass.isAnnotationPresent(DSLExtensionMethod.class)) {
      var annotation = methodClass.getAnnotation(DSLExtensionMethod.class);
      var extendedClass = annotation.extendedType();
      IType extendedType = createDSLTypeForJavaTypeInScope(globalScope, extendedClass);
      if (extendedType instanceof AggregateType aggregateExtendedType) {
        var genericInterfaces = methodClass.getGenericInterfaces();
        var type = genericInterfaces[0];
        ParameterizedType parameterizedType = (ParameterizedType) type;

        // create FunctionType
        Type returnType = parameterizedType.getActualTypeArguments()[1];
        IType returnDSLType = createDSLTypeForJavaTypeInScope(globalScope, returnType);

        List<Type> parameterTypes = method.getParameterTypes();
        List<IType> parameterDSLTypes = new ArrayList<>(parameterTypes.size());
        for (Type parameterType : parameterTypes) {
          IType dslType = null;
          if (parameterType instanceof ParameterizedType parameterizedParameterType) {
            try {
              Class<?> rawType = (Class<?>) parameterizedParameterType.getRawType();
              if (rawType.isAnnotationPresent(FunctionalInterface.class)) {
                dslType = this.createFunctionType(parameterizedParameterType, globalScope);
              }
            } catch (ClassCastException ex) {
              //
            }
          }
          if (dslType == null) {
            dslType = createDSLTypeForJavaTypeInScope(globalScope, parameterType);
          }
          parameterDSLTypes.add(dslType);
        }

        FunctionType functionType = new FunctionType(returnDSLType, parameterDSLTypes);

        // create and bind method symbol
        ExtensionMethod nativeMethodSymbol =
            new ExtensionMethod(
                annotation.name(),
                aggregateExtendedType,
                functionType,
                (IDSLExtensionMethod<Object, Object>) method);
        aggregateExtendedType.bind(nativeMethodSymbol);
      }
    }
  }
}
