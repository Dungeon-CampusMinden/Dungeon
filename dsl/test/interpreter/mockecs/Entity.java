package interpreter.mockecs;

import semanticanalysis.types.DSLContextPush;
import semanticanalysis.types.DSLType;

import java.util.ArrayList;
import java.util.List;

@DSLType(name = "entity")
@DSLContextPush(name = "entity")
public class Entity {
    public List<Component> components = new ArrayList<>();
}
