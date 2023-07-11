package interpreter.mockecs;

import semanticanalysis.types.DSLCallback;
import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

import java.util.function.Consumer;

@DSLType
public class TestComponentWithStringConsumerCallback extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLCallback
    private Consumer<String> onInteraction;

    public TestComponentWithStringConsumerCallback(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }

    public void executeCallbackWithText(String text) {
        onInteraction.accept(text);
    }
}
