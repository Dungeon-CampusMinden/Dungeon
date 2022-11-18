package symboltable;

public class AggregateType extends ScopedSymbol implements IType {
    public AggregateType(String name, IScope parentScope) {
        super(name, parentScope, null);
    }
}
