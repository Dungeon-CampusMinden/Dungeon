package interpreter.mockecs;

import semanticanalysis.types.DSLCallback;
import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;

import java.util.function.Function;

@DSLType
public class TestComponentWithStringFunctionCallback extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLCallback
    private Function<String, String> onInteraction;

    public TestComponentWithStringFunctionCallback(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }

    public String executeCallbackWithText(String text) {
        return onInteraction.apply(text);
    }
}
