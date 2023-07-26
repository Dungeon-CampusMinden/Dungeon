package interpreter.mockecs;

import semanticanalysis.types.DSLCallback;
import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;

import java.util.List;
import java.util.function.Function;

@DSLType
public class TestComponentListPassThroughCallback extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLCallback private Function<List<Entity>, List<Entity>> onInteraction;

    public Function<List<Entity>, List<Entity>> getOnInteraction() {
        return onInteraction;
    }

    public List<Entity> executeCallback(List<Entity> entities) {
        return onInteraction.apply(entities);
    }

    public TestComponentListPassThroughCallback(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }
}
