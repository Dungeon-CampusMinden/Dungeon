package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.runtime.callable.NativeMethod;
import dsl.runtime.value.ListValue;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;

public class ListType extends ScopedSymbol implements IType {
  public IType getElementType() {
    return this.dataType;
  }

  public static String getListTypeName(IType elementType) {
    return elementType.getName() + "[]";
  }

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

    NativeMethod clearMethod =
        new NativeMethod(
            "clear",
            this,
            new FunctionType(BuiltInType.noType, BuiltInType.noType),
            ListValue.ClearMethod.instance);
    this.bind(clearMethod);
  }

  @Override
  public Kind getTypeKind() {
    return Kind.ListType;
  }
}
