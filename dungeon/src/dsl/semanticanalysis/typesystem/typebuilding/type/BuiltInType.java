package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.runtime.value.Value;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.Symbol;

// TODO: "entity_type" type definition should be fixed part of the
//  built in type system
/** BuiltInType. */
public class BuiltInType extends Symbol implements IType {
  /** WTF? . */
  public interface AsBooleanFunction {
    /**
     * WTF? .
     *
     * @param param foo
     * @return foo
     */
    boolean run(Value param);
  }

  /** WTF? . */
  public static BuiltInType noType = new BuiltInType("none", Scope.NULL, (v) -> false);

  /** WTF? . */
  public static BuiltInType boolType =
      new BuiltInType("bool", Scope.NULL, (v) -> (boolean) v.getInternalValue());

  /** WTF? . */
  public static BuiltInType intType =
      new BuiltInType("int", Scope.NULL, (v) -> (int) v.getInternalValue() != 0);

  /** WTF? . */
  public static BuiltInType floatType =
      new BuiltInType("float", Scope.NULL, (v) -> (float) v.getInternalValue() != 0.0);

  /** WTF? . */
  public static BuiltInType stringType = new BuiltInType("string", Scope.NULL, (v) -> true);

  // TODO: this should not be a basic type
  /** WTF? . */
  public static BuiltInType graphType = new BuiltInType("graph", Scope.NULL, (v) -> true);

  /** WTF? . */
  public final AsBooleanFunction asBooleanFunction;

  /**
   * Constructor.
   *
   * @param name name of this type
   * @param parentScope parent scope of the type
   * @param asBooleanFunction function to convert a value of this type to a boolean
   */
  public BuiltInType(String name, IScope parentScope, AsBooleanFunction asBooleanFunction) {
    super(name, parentScope, null);
    this.asBooleanFunction = asBooleanFunction;
  }

  @Override
  public Kind getTypeKind() {
    return Kind.Basic;
  }
}
