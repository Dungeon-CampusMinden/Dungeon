package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.semanticanalysis.scope.IScope;
import java.lang.reflect.Method;
import java.util.HashMap;

public class TypeFactory {
  private HashMap<String, IType> types;

  // public static TypeFactory INSTANCE = new TypeFactory();

  public TypeFactory() {
    this.types = new HashMap<>();
  }

  // TODO: function types are currently created with no parent scope and just bound in
  //  a scope; using the TypeFactory for all function type definitions (in native functions etc.)
  //  is a lot of refactoring -> do it, when more time, or when it's a real problem
  /*public FunctionType functionType(IType returnType, IType... parameterTypes) {
    String typeName = FunctionType.calculateTypeName(returnType, List.of(parameterTypes));
    if (this.types.containsKey(typeName)) {
      return (FunctionType) this.types.get(typeName);
    }
    FunctionType newType = new FunctionType(returnType, parameterTypes);
    this.types.put(typeName, newType);
    return newType;
  }*/

  /*public FunctionType functionType(IType returnType, List<IType> parameterTypes) {
    String typeName = FunctionType.calculateTypeName(returnType, parameterTypes);
    if (this.types.containsKey(typeName)) {
      return (FunctionType) this.types.get(typeName);
    }
    FunctionType newType = new FunctionType(returnType, parameterTypes);
    this.types.put(typeName, newType);
    return newType;
  }*/

  public SetType setType(IType elementType, IScope parentScope) {
    if (elementType.getName().equals("int")) {
      boolean b = true;
    }
    String typeName = SetType.getSetTypeName(elementType);
    if (this.types.containsKey(typeName)) {
      return (SetType) this.types.get(typeName);
    }

    SetType newType = new SetType(elementType, parentScope);
    parentScope.bind(newType);
    this.types.put(typeName, newType);
    return newType;
  }

  public MapType mapType(IType keyType, IType elementType, IScope parentScope) {
    String typeName = MapType.getMapTypeName(keyType, elementType);
    if (this.types.containsKey(typeName)) {
      return (MapType) this.types.get(typeName);
    }

    MapType newType = new MapType(keyType, elementType, parentScope, this);
    parentScope.bind(newType);
    this.types.put(typeName, newType);
    return newType;
  }

  public ListType listType(IType elementType, IScope parentScope) {
    String typeName = ListType.getListTypeName(elementType);
    if (this.types.containsKey(typeName)) {
      return (ListType) this.types.get(typeName);
    }

    ListType newType = new ListType(elementType, parentScope);
    parentScope.bind(newType);
    this.types.put(typeName, newType);
    return newType;
  }

  public AggregateType aggregateType(String name, IScope parentScope) {
    if (this.types.containsKey(name)) {
      return (AggregateType) this.types.get(name);
    }

    AggregateType newType = new AggregateType(name, parentScope);
    parentScope.bind(newType);
    this.types.put(name, newType);
    return newType;
  }

  public AggregateType aggregateType(String name, IScope parentScope, Class<?> originType) {
    if (this.types.containsKey(name)) {
      return (AggregateType) this.types.get(name);
    }

    AggregateType newType = new AggregateType(name, parentScope, originType);
    parentScope.bind(newType);
    this.types.put(name, newType);
    return newType;
  }

  public AggregateTypeAdapter aggregateTypeAdapter(
      String name, IScope parentScope, Class<?> originType, Method builderMethod) {
    if (this.types.containsKey(name)) {
      return (AggregateTypeAdapter) this.types.get(name);
    }

    AggregateTypeAdapter newType =
        new AggregateTypeAdapter(name, parentScope, originType, builderMethod);
    parentScope.bind(newType);
    this.types.put(name, newType);
    return newType;
  }

  public EnumType enumType(String name, IScope parentScope, Class<? extends Enum<?>> originType) {
    if (this.types.containsKey(name)) {
      return (EnumType) this.types.get(name);
    }

    EnumType newType = new EnumType(name, parentScope, originType);
    parentScope.bind(newType);
    this.types.put(name, newType);
    return newType;
  }

  public ImportAggregateTypeSymbol importAggregateTypeSymbol(
      AggregateType originalTypeSymbol, IScope parentScope) {
    // TODO: this does not work for import type symbols, because they can have the same name as
    //  some other type because they are typed!!
    //  OH FUCK. Does this break semantically predicated lexing?
    // TODO: TEST THIS
    //  This likely only concernes entity_types and item_types, of which no objects
    //  can be created.. so this may be a non-concern
    /*if (this.types.containsKey(name)) {
      return (EnumType) this.types.get(name);
    }

    EnumType newType = new EnumType(name, parentScope, originType);
    parentScope.bind(newType);
    this.types.put(name, newType);
    return newType;*/
    throw new UnsupportedOperationException();
  }

  public ImportAggregateTypeSymbol importAggregateTypeSymbol(
      AggregateType originalTypeSymbol, String name, IScope parentScope) {
    throw new UnsupportedOperationException();
  }
}
