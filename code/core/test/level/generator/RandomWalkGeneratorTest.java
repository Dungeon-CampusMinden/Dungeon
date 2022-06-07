package level.generator;

import static org.junit.Assert.assertNotNull;

import level.elements.ILevel;
import level.generator.randomwalk.RandomWalkGenerator;
import org.junit.Before;
import org.junit.Test;

public class RandomWalkGeneratorTest {

    private RandomWalkGenerator generator;
    private ILevel level;

    @Before
    public void setup() {
        generator = new RandomWalkGenerator();
        level = generator.getLevel();
    }

    @Test
    public void test_getLevel() {
        assertNotNull(level);
        assertNotNull(level.getEndTile());
        assertNotNull(level.getStartTile());
        // if the path is bigger than 0 it means, there is a path form start to end, so the level
        // can be beaten.
        assert ((level.findPath(level.getStartTile(), level.getEndTile()).getCount() > 0));
    }
}
