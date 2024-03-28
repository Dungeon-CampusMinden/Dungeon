package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.runtime.callable.NativeMethod;
import dsl.runtime.value.MapValue;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;

/** MapType. */
public class MapType extends ScopedSymbol implements IType {
  private final IType keyType;
  private final IType elementType;

  /**
   * Gets the key type of the object.
   *
   * @return the key type of the object
   */
  public IType getKeyType() {
    return this.keyType;
  }

  /**
   * Gets the type of element.
   *
   * @return the type of element
   */
  public IType getElementType() {
    return this.elementType;
  }

  /**
   * WTF? .
   *
   * @param keyType foo
   * @param elementType foo
   * @return foo
   */
  public static String getMapTypeName(IType keyType, IType elementType) {
    return "[" + keyType.getName() + "->" + elementType.getName() + "]";
  }

  /**
   * Constructor.
   *
   * @param keyType foo
   * @param elementType foo
   * @param parentScope foo
   */
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
  }

  @Override
  public Kind getTypeKind() {
    return Kind.MapType;
  }
}
