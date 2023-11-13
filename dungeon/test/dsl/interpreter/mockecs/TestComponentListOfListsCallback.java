package dsl.interpreter.mockecs;

import dsl.semanticanalysis.types.DSLCallback;
import dsl.semanticanalysis.types.DSLContextMember;
import dsl.semanticanalysis.types.DSLType;

import java.util.List;
import java.util.function.Function;

@DSLType
public class TestComponentListOfListsCallback extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLCallback private Function<List<List<Entity>>, Boolean> onInteraction;

    public Function<List<List<Entity>>, Boolean> getOnInteraction() {
        return onInteraction;
    }

    public TestComponentListOfListsCallback(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }
}
