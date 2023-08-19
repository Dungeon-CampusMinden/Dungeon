package interpreter.mockecs;

import semanticanalysis.types.*;

import java.util.ArrayList;
import java.util.List;

@DSLType(name = "entity")
@DSLContextPush(name = "entity")
public class Entity {
    @DSLTypeProperty(name = "test_component2", extendedType = Entity.class)
    public static class TestComponent2Property implements IDSLTypeProperty<Entity, TestComponent2> {
        public static Entity.TestComponent2Property instance = new Entity.TestComponent2Property();

        private TestComponent2Property() {}

        @Override
        public void set(Entity instance, TestComponent2 valueToSet) {}

        @Override
        public TestComponent2 get(Entity instance) {
            var list =
                    instance.components.stream().filter(c -> c instanceof TestComponent2).toList();
            if (list.size() == 0) {
                return null;
            } else {
                return (TestComponent2) list.get(0);
            }
        }

        @Override
        public boolean isSettable() {
            return false;
        }

        @Override
        public boolean isGettable() {
            return true;
        }
    }

    @DSLTypeProperty(name = "test_component1", extendedType = Entity.class)
    public static class TestComponent1Property implements IDSLTypeProperty<Entity, TestComponent1> {
        public static Entity.TestComponent1Property instance = new Entity.TestComponent1Property();

        private TestComponent1Property() {}

        @Override
        public void set(Entity instance, TestComponent1 valueToSet) {}

        @Override
        public TestComponent1 get(Entity instance) {
            var list =
                    instance.components.stream().filter(c -> c instanceof TestComponent1).toList();
            if (list.size() == 0) {
                return null;
            } else {
                return (TestComponent1) list.get(0);
            }
        }

        @Override
        public boolean isSettable() {
            return false;
        }

        @Override
        public boolean isGettable() {
            return true;
        }
    }

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
