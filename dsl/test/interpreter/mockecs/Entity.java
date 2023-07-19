package interpreter.mockecs;

import semanticanalysis.types.DSLContextPush;
import semanticanalysis.types.DSLType;
import semanticanalysis.types.DSLTypeMember;

import java.util.ArrayList;
import java.util.List;

@DSLType(name = "entity")
@DSLContextPush(name = "entity")
public class Entity {
    public Entity() {
        this.idx = _idx++;
    }

    private static int _idx;
    private String name;
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<Component> components = new ArrayList<>();

    @DSLTypeMember private int idx;

    public int getIdx() {
        return idx;
    }

}
