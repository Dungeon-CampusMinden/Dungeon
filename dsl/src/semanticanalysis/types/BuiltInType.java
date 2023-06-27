package semanticanalysis.types;

import runtime.Value;
import semanticanalysis.IScope;
import semanticanalysis.Scope;
import semanticanalysis.Symbol;

// TODO: "entity_type" type definition should be fixed part of the
//  built in type system
public class BuiltInType extends Symbol implements IType {
    public interface IsBooleanFunction {
        boolean run(Value param);
    }

    public static BuiltInType noType = new BuiltInType("none", Scope.NULL, (v) -> false);
    public static BuiltInType intType = new BuiltInType("int", Scope.NULL, (v) -> (int)v.getInternalObject() != 0);
    public static BuiltInType floatType = new BuiltInType("float", Scope.NULL, (v) -> (float)v.getInternalObject() != 0.0);
    public static BuiltInType stringType = new BuiltInType("string", Scope.NULL, (v) -> true);
    // TODO: this should not be a basic type
    public static BuiltInType graphType = new BuiltInType("dslToGame/graph", Scope.NULL, (v) -> true);

    public final IsBooleanFunction isBooleanFunction;

    /**
     * Constructor
     *
     * @param name name of this type
     * @param parentScope parent scope of the type
     */
    public BuiltInType(String name, IScope parentScope, IsBooleanFunction isBoolean) {
        super(name, parentScope, null);
        isBooleanFunction = isBoolean;
    }

    @Override
    public Kind getTypeKind() {
        return Kind.Basic;
    }
}
