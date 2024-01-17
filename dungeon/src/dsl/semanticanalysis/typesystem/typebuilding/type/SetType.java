package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.runtime.callable.NativeMethod;
import dsl.runtime.value.SetValue;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;

public class SetType extends ScopedSymbol implements IType {
  public IType getElementType() {
    return this.dataType;
  }

  public static String getSetTypeName(IType elementType) {
    return elementType.getName() + "<>";
  }

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

    NativeMethod clearMethod =
        new NativeMethod(
            "clear",
            this,
            new FunctionType(BuiltInType.noType, BuiltInType.noType),
            SetValue.ClearMethod.instance);
    this.bind(clearMethod);
  }

  @Override
  public Kind getTypeKind() {
    return Kind.SetType;
  }
}
