package semanticAnalysis.types;

import semanticAnalysis.IScope;
import semanticAnalysis.Scope;
import semanticAnalysis.Symbol;

// TODO: "game_object" type definition should be fixed part of the
//  built in type system
public class BuiltInType extends Symbol implements IType {
    public static BuiltInType intType = new BuiltInType("int", Scope.NULL);
    public static BuiltInType floatType = new BuiltInType("float", Scope.NULL);
    public static BuiltInType stringType = new BuiltInType("string", Scope.NULL);
    // TODO: this should not be a basic type
    public static BuiltInType graphType = new BuiltInType("dslToGame/graph", Scope.NULL);
    // TODO: is this a good idea?
    public static BuiltInType funcType = new BuiltInType("func", Scope.NULL);

    /**
     * Constructor
     *
     * @param name name of this type
     * @param parentScope parent scope of the type
     */
    public BuiltInType(String name, IScope parentScope) {
        super(name, parentScope, null);
    }

    @Override
    public Kind getTypeKind() {
        return Kind.Basic;
    }
}
