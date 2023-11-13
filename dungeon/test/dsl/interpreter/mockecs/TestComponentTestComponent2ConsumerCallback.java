package dsl.interpreter.mockecs;

import dsl.semanticanalysis.types.DSLCallback;
import dsl.semanticanalysis.types.DSLContextMember;
import dsl.semanticanalysis.types.DSLType;

import java.util.function.Consumer;

@DSLType(name = "test_component_with_callback")
public class TestComponentTestComponent2ConsumerCallback extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLCallback public Consumer<TestComponent2> consumer;

    public TestComponentTestComponent2ConsumerCallback(
            @DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }
}
