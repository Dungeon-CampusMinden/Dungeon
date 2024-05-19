package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.runtime.callable.NativeMethod;
import dsl.runtime.value.SetValue;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;

/** SetType. */
public class SetType extends ScopedSymbol implements IType {
  /**
   * WTF? .
   *
   * @return foo
   */
  public IType getElementType() {
    return this.dataType;
  }

  /**
   * WTF? .
   *
   * @param elementType foo
   * @return foo
   */
  public static String getSetTypeName(IType elementType) {
    return elementType.getName() + "<>";
  }

  /**
   * Constructor.
   *
   * @param elementType foo
   * @param parentScope foo
   */
  public SetType(IType elementType, IScope parentScope) {
    super(getSetTypeName(elementType), parentScope, elementType);

    NativeMethod addMethod =
        new NativeMethod(
            "add",
            this,
            new FunctionType(BuiltInType.boolType, elementType),
            SetValue.AddMethod.instance);
    this.bind(addMethod);

    NativeMethod sizeMethod =
        new NativeMethod(
            "size",
            this,
            new FunctionType(BuiltInType.intType, BuiltInType.noType),
            SetValue.SizeMethod.instance);
    this.bind(sizeMethod);

    NativeMethod getMethod =
        new NativeMethod(
            "contains",
            this,
            new FunctionType(BuiltInType.boolType, elementType),
            SetValue.ContainsMethod.instance);
    this.bind(getMethod);
  }

  @Override
  public Kind getTypeKind() {
    return Kind.SetType;
  }
}
