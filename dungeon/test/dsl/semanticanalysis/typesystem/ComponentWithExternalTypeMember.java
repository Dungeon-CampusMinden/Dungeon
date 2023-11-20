package dsl.semanticanalysis.typesystem;

import core.utils.Point;

import dsl.interpreter.mockecs.Component;
import dsl.interpreter.mockecs.Entity;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLContextMember;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLType;
import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLTypeMember;

@DSLType
public class ComponentWithExternalTypeMember extends Component {
    public ComponentWithExternalTypeMember(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
    }

    @DSLTypeMember
    public Point point;
}
