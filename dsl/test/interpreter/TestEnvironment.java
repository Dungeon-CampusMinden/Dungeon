package interpreter;

import runtime.GameEnvironment;

import semanticanalysis.Symbol;
import semanticanalysis.types.IDSLTypeProperty;
import semanticanalysis.types.TypeBuilder;

import java.util.ArrayList;
import java.util.List;

// TODO: revise class-structure.. maybe the GameEnvironment should extend the
//  'DefaultEnvironment`, which only bind the basic built in types and the
//  'TestEnvironment' would be a parallel implementation, not a deriving implementation
public class TestEnvironment extends GameEnvironment {

    @Override
    public TypeBuilder getTypeBuilder() {
        return super.getTypeBuilder();
    }

    public TestEnvironment() {
        super();
    }

    @Override
    public Class<?>[] getBuiltInAggregateTypeClasses() {
        return new Class[] {CustomQuestConfig.class};
    }

    @Override
    public List<IDSLTypeProperty<?, ?>> getBuiltInProperties() {
        return new ArrayList<>();
    }

    @Override
    protected void registerDefaultRuntimeObjectTranslators() {}

    @Override
    protected void registerDefaultTypeAdapters() {}

    @Override
    protected ArrayList<Symbol> buildDependantNativeFunctions() {
        return new ArrayList<>();
    }

    @Override
    protected void bindBuiltInProperties() {}

    @Override
    protected void bindBuiltInMethods() {}
}
