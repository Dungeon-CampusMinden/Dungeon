package interpreter.mockecs;

import semanticanalysis.types.DSLCallback;
import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;

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
