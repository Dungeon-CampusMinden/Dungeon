package symboltable;

public class BuiltInType extends Symbol implements IType {
    public static BuiltInType intType = new BuiltInType("int", Scope.NULL);
    public static BuiltInType stringType = new BuiltInType("string", Scope.NULL);
    public static BuiltInType graphType = new BuiltInType("graph", Scope.NULL);

    public BuiltInType(String name, IScope parentScope) {
        super(name, parentScope, null);
    }
}
