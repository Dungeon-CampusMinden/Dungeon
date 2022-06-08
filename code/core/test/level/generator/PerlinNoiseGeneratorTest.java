package level.generator;

import static org.junit.Assert.assertNotNull;

import level.elements.ILevel;
import level.generator.perlinNoise.PerlinNoiseGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PerlinNoiseGeneratorTest {

    private ILevel level;

    @Parameterized.Parameters
    public static ILevel[] data() {
        PerlinNoiseGenerator generator = new PerlinNoiseGenerator();
        ILevel[] params = new ILevel[10];
        for (int i = 0; i < params.length; i++) {
            params[i] = generator.getLevel();
        }
        return params;
    }

    public PerlinNoiseGeneratorTest(ILevel level) {
        this.level = level;
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
