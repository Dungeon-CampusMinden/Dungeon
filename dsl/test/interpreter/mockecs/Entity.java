package interpreter.mockecs;

import semanticanalysis.types.DSLContextPush;
import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

import java.util.ArrayList;
import java.util.List;

@DSLType(name = "entity")
@DSLContextPush(name = "entity")
public class Entity {
    private static int _idx;
    public List<Component> components = new ArrayList<>();

    @DSLTypeMember private int idx;

    public int getIdx() {
        return idx;
    }

    public Entity() {
        this.idx = _idx++;
    }
}
