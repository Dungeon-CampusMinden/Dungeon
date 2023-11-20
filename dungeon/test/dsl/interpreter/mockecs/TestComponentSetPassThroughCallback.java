package dsl.interpreter.mockecs;

import dsl.semanticanalysis.types.typebuilding.annotation.DSLCallback;
import dsl.semanticanalysis.types.typebuilding.annotation.DSLContextMember;
import dsl.semanticanalysis.types.typebuilding.annotation.DSLType;

import java.util.Set;
import java.util.function.Function;

@DSLType
public class TestComponentSetPassThroughCallback extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLCallback private Function<Set<Entity>, Set<Entity>> onInteraction;

    public Function<Set<Entity>, Set<Entity>> getOnInteraction() {
        return onInteraction;
    }

    public Set<Entity> executeCallback(Set<Entity> entities) {
        return onInteraction.apply(entities);
    }

    public TestComponentSetPassThroughCallback(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }
}
