package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.runtime.callable.NativeMethod;
import dsl.runtime.value.ListValue;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;

/** ListType. */
public class ListType extends ScopedSymbol implements IType {
  /**
   * WTF? .
   *
   * @return foo
   */
  public IType getElementType() {
    return this.dataType;
  }

  /**
   * Returns the list type name for the given element type.
   *
   * @param elementType the type of the list element
   * @return the type name of the list
   */
  public static String getListTypeName(IType elementType) {
    return elementType.getName() + "[]";
  }

  /**
   * Constructor.
   *
   * @param elementType
   * @param parentScope
   */
  public ListType(IType elementType, IScope parentScope) {
    super(getListTypeName(elementType), parentScope, elementType);

    NativeMethod addMethod =
        new NativeMethod(
            "add",
            this,
            new FunctionType(BuiltInType.noType, elementType),
            ListValue.AddMethod.instance);
    this.bind(addMethod);

    NativeMethod sizeMethod =
        new NativeMethod(
            "size",
            this,
            new FunctionType(BuiltInType.intType, BuiltInType.noType),
            ListValue.SizeMethod.instance);
    this.bind(sizeMethod);

    NativeMethod getMethod =
        new NativeMethod(
            "get",
            this,
            new FunctionType(elementType, BuiltInType.intType),
            ListValue.GetMethod.instance);
    this.bind(getMethod);
  }

  @Override
  public Kind getTypeKind() {
    return Kind.ListType;
  }
}
