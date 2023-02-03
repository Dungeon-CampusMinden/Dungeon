package level.generator;

import static org.junit.Assert.assertNotNull;

import level.elements.ILevel;
import level.generator.hamster.HamsterGenerator;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the hamster generator
 *
 * @author Maxim Fruendt
 */
public class HamsterGeneratorTest {

    private ILevel level;

    @Before
    public void setup() {
        level = new HamsterGenerator().getLevel();
    }

    @Test
    public void test_getLevel() {
        // Currently the hamster simulator levels don't have a goal, they are like a
        // sandbox, which is why we don't check for an end here
        assertNotNull(level);
        assertNotNull(level.getStartTile());
    }
}
