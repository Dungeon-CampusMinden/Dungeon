package dsl.interpreter.mockecs;

import core.utils.TriConsumer;

import dsl.semanticanalysis.types.DSLCallback;
import dsl.semanticanalysis.types.DSLContextMember;
import dsl.semanticanalysis.types.DSLType;

@DSLType
public class TestComponentWithTriConsumerCallback extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLCallback private TriConsumer<Entity, Entity, Boolean> onInteraction;

    public TestComponentWithTriConsumerCallback(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }
}
