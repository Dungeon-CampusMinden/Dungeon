package semanticanalysis.types;

import core.utils.Point;

import interpreter.mockecs.Component;
import interpreter.mockecs.Entity;

@DSLType
public class ComponentWithExternalTypeMember extends Component {
    public ComponentWithExternalTypeMember(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
    }

    @DSLTypeMember public Point point;
}
