package interpreter;

import interpreter.mockecs.Entity;
import interpreter.mockecs.MockEntityTranslator;

import runtime.GameEnvironment;

import semanticanalysis.types.TypeBuilder;

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
        return new Class[]{CustomQuestConfig.class};
    }

    @Override
    protected void registerDefaultRuntimeObjectTranslators() {
        this.runtimeObjectTranslator.loadObjectToValueTranslator(
                Entity.class, MockEntityTranslator.instance);
    }
}
