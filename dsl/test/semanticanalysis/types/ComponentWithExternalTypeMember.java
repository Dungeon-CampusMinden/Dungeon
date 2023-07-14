package semanticanalysis.types;

import core.utils.position.Position;

import interpreter.mockecs.Component;
import interpreter.mockecs.Entity;

@DSLType
public class ComponentWithExternalTypeMember extends Component {
    public ComponentWithExternalTypeMember(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
    }

    @DSLTypeMember public Position position;
}
