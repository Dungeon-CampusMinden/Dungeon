package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.runtime.callable.NativeMethod;
import dsl.runtime.value.MapValue;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;

public class MapType extends ScopedSymbol implements IType {
  private final IType keyType;
  private final IType elementType;

  public IType getKeyType() {
    return this.keyType;
  }

  public IType getElementType() {
    return this.elementType;
  }

  public static String getMapTypeName(IType keyType, IType elementType) {
    return "[" + keyType.getName() + "->" + elementType.getName() + "]";
  }

  public MapType() {
    super();
    keyType = BuiltInType.noType;
    elementType = BuiltInType.noType;
  }

  MapType(IType keyType, IType elementType, IScope parentScope, TypeFactory typeFactory) {
    super(getMapTypeName(keyType, elementType), parentScope, elementType);
    this.keyType = keyType;
    this.elementType = elementType;

    NativeMethod addMethod =
        new NativeMethod(
            "add",
            this,
            TypeFactory.INSTANCE.functionType(BuiltInType.noType, keyType, elementType),
            MapValue.AddMethod.instance);
    this.bind(addMethod);

    // var keyListType = new ListType(keyType, parentScope);
    var keyListType = typeFactory.listType(keyType, parentScope);
    NativeMethod getKeysMethod =
        new NativeMethod(
            "get_keys",
            this,
            TypeFactory.INSTANCE.functionType(keyListType),
            MapValue.GetKeysMethod.instance);
    this.bind(getKeysMethod);

    // var elementListType = new ListType(elementType, parentScope);
    var elementListType = typeFactory.listType(elementType, parentScope);
    NativeMethod getElementsMethod =
        new NativeMethod(
            "get_elements",
            this,
            TypeFactory.INSTANCE.functionType(elementListType),
            MapValue.GetElementsMethod.instance);
    this.bind(getElementsMethod);

    NativeMethod clearMethod =
        new NativeMethod(
            "clear",
            this,
            TypeFactory.INSTANCE.functionType(BuiltInType.noType, BuiltInType.noType),
            MapValue.ClearMethod.instance);
    this.bind(clearMethod);
  }

  @Override
  public Kind getTypeKind() {
    return Kind.MapType;
  }
}
