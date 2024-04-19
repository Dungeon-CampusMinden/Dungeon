package dsl.runtime.value;

import dsl.runtime.callable.ICallable;
import dsl.semanticanalysis.typesystem.typebuilding.type.BuiltInType;
import dsl.semanticanalysis.typesystem.typebuilding.type.IType;

/** This Value represents the instance of an {@link ICallable}. */
public class FunctionValue extends Value {
  /** WTF? . */
  public static FunctionValue NONE = new FunctionValue(BuiltInType.noType, null);

  /**
   * WTF? .
   *
   * @return index of the symbol representing the function definition, which is called by this Value
   */
  public ICallable getCallable() {
    return (ICallable) this.getInternalValue();
  }

  /**
   * Constructor.
   *
   * @param functionReturnValue {@link IType} representing the return value of the called function
   * @param callable the callable represented by this value
   */
  public FunctionValue(IType functionReturnValue, ICallable callable) {
    super(functionReturnValue, callable);
  }

  @Override
  public Object clone() {
    return new FunctionValue(this.dataType, this.getCallable());
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public boolean isEmpty() {
    return this.getInternalValue() == null;
  }
}
