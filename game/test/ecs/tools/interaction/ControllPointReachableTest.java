package ecs.tools.interaction;

import static org.junit.Assert.*;

import ecs.components.InteractionComponent;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import org.junit.Test;
import testinghelper.SimpleCounter;
import tools.Point;

public class ControllPointReachableTest {
    /** missing tests valid corner straight */
    /** missing tests nonvalid corner straight with wall inbetween */
    @Test
    public void controllDefault() {
        IReachable i = new ControllPointReachable();
        SimpleCounter s1 = new SimpleCounter();
        Entity e1 = new Entity();
        Point e1Point = new Point(0, 0);
        PositionComponent e1PC = new PositionComponent(e1, e1Point);
        InteractionComponent e1IC = new InteractionComponent(e1, 5, false, (e) -> s1.inc());

        Point unitDirectionalVector = Point.getUnitDirectionalVector(e1Point, e1Point);
        int dist = 3;
        boolean b =
                i.cheackReachable(new InteractionData(e1, e1PC, e1IC, dist, unitDirectionalVector));
        assertEquals(0, s1.getCount());
    }
}
