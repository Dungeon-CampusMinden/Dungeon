package dsl.interpreter.mockecs;

import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLCallback;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLContextMember;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLType;

import java.util.function.Function;

@DSLType
public class TestComponentWithStringFunctionCallback extends Component {
    private Entity entity;

    public Entity getEntity() {
        return entity;
    }

    @DSLCallback private Function<String, String> onInteraction;

    public TestComponentWithStringFunctionCallback(
            @DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.entity = entity;
    }

    public String executeCallbackWithText(String text) {
        return onInteraction.apply(text);
    }
}
