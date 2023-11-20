package dsl.interpreter.mockecs;

import dsl.semanticanalysis.types.annotation.DSLCallback;
import dsl.semanticanalysis.types.annotation.DSLContextMember;
import dsl.semanticanalysis.types.annotation.DSLType;

import java.util.function.Function;

@DSLType
public class TestComponentWithFunctionCallback extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLCallback private Function<Entity, Boolean> onInteraction;
    @DSLCallback private Function<Entity, MyEnum> getEnum;
    @DSLCallback private Function<MyEnum, Boolean> functionWithEnumParam;

    public Function<Entity, Boolean> getOnInteraction() {
        return onInteraction;
    }

    public Function<Entity, MyEnum> getGetEnum() {
        return getEnum;
    }

    public Function<MyEnum, Boolean> getFunctionWithEnumParam() {
        return functionWithEnumParam;
    }

    public TestComponentWithFunctionCallback(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }
}
