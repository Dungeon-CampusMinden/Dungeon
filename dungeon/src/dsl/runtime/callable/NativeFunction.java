package dsl.runtime.callable;

import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.symbol.ScopedSymbol;
import dsl.semanticanalysis.symbol.Symbol;
import dsl.semanticanalysis.typesystem.typebuilding.type.FunctionType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

/** WTF. ? */
public abstract class NativeFunction extends ScopedSymbol implements ICallable {
  protected NativeFunction(String name, IScope parentScope, FunctionType type) {
    super(name, parentScope, type);

    // create generically named parameter symbols
    for (int i = 0; i < type.getParameterTypes().size(); i++) {
      IType parameterType = type.getParameterTypes().get(i);
      String parameterName = "param" + i;
      Symbol parameterSymbol = new Symbol(parameterName, this, parameterType);
      this.bind(parameterSymbol);
    }
  }

  /**
   * WTF. ?
   *
   * @param type foo
   */
  public void overwriteFunctionType(FunctionType type) {
    this.dataType = type;
  }

  @Override
  public FunctionType getFunctionType() {
    return (FunctionType) this.getDataType();
  }
}
