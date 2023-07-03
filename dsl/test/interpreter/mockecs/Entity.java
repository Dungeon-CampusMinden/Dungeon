package interpreter.mockecs;

import semanticanalysis.types.DSLContextPush;
import semanticanalysis.types.DSLType;

import java.util.ArrayList;
import java.util.List;

// TODO: how to link this class to the type definitions in semantic
//  analysis (e.g. the syntax-concept of 'entity_type')?
@DSLType(name = "entity")
@DSLContextPush(name = "entity")
public class Entity {
    public List<Component> components = new ArrayList<>();
}
