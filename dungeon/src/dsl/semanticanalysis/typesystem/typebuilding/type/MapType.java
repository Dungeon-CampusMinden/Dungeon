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

  public MapType(IType keyType, IType elementType, IScope parentScope) {
    super(getMapTypeName(keyType, elementType), parentScope, elementType);
    this.keyType = keyType;
    this.elementType = elementType;

    NativeMethod addMethod =
        new NativeMethod(
            "add",
            this,
            new FunctionType(BuiltInType.noType, keyType, elementType),
            MapValue.AddMethod.instance);
    this.bind(addMethod);

    var keyListType = new ListType(keyType, parentScope);
    NativeMethod getKeysMethod =
        new NativeMethod(
            "get_keys", this, new FunctionType(keyListType), MapValue.GetKeysMethod.instance);
    this.bind(getKeysMethod);

    var elementListType = new ListType(elementType, parentScope);
    NativeMethod getElementsMethod =
        new NativeMethod(
            "get_elements",
            this,
            new FunctionType(elementListType),
            MapValue.GetElementsMethod.instance);
    this.bind(getElementsMethod);

    NativeMethod clearMethod =
        new NativeMethod(
            "clear",
            this,
            new FunctionType(BuiltInType.noType, BuiltInType.noType),
            MapValue.ClearMethod.instance);
    this.bind(clearMethod);
  }

  @Override
  public Kind getTypeKind() {
    return Kind.MapType;
  }
}
