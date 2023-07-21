package interpreter.mockecs;

import semanticanalysis.types.DSLCallback;
import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;

import java.util.List;
import java.util.function.Function;

@DSLType
public class TestComponentWithFunctionCallbackListParameter extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLCallback
    private Function<List<Entity>, Boolean> onInteraction;

    public Function<List<Entity>, Boolean> getOnInteraction() {
        return onInteraction;
    }

    public TestComponentWithFunctionCallbackListParameter(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }
}
