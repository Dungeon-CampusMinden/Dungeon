package dsl.semanticanalysis.typesystem.typebuilding.type;

import dsl.runtime.value.Value;
import dsl.semanticanalysis.scope.IScope;
import dsl.semanticanalysis.scope.Scope;
import dsl.semanticanalysis.symbol.Symbol;
import org.neo4j.ogm.annotation.NodeEntity;

// TODO: "entity_type" type definition should be fixed part of the
//  built in type system
@NodeEntity
public class BuiltInType extends Symbol implements IType {
  public interface AsBooleanFunction {
    boolean run(Value param);
  }

  public static BuiltInType noType = new BuiltInType("none", Scope.NULL, (v) -> false);
  public static BuiltInType boolType =
      new BuiltInType("bool", Scope.NULL, (v) -> (boolean) v.getInternalValue());
  public static BuiltInType intType =
      new BuiltInType("int", Scope.NULL, (v) -> (int) v.getInternalValue() != 0);
  public static BuiltInType floatType =
      new BuiltInType("float", Scope.NULL, (v) -> (float) v.getInternalValue() != 0.0);
  public static BuiltInType stringType = new BuiltInType("string", Scope.NULL, (v) -> true);
  // TODO: this should not be a basic type
  public static BuiltInType graphType = new BuiltInType("graph", Scope.NULL, (v) -> true);

  public final AsBooleanFunction asBooleanFunction;

  /**
   * Constructor
   *
   * @param name name of this type
   * @param parentScope parent scope of the type
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
