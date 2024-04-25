package core.level.generator;

import static org.junit.Assert.assertNotNull;

import core.level.elements.ILevel;
import core.level.generator.randomwalk.RandomWalkGenerator;
import org.junit.Before;
import org.junit.Test;

/** Tests for the {@link RandomWalkGenerator} class. */
public class RandomWalkGeneratorTest {

  private RandomWalkGenerator generator;
  private ILevel level;

  /** WTF? . */
  @Before
  public void setup() {
    generator = new RandomWalkGenerator();
    level = generator.level();
  }

  /** WTF? . */
  @Test
  public void test_getLevel() {
    assertNotNull(level);
    assertNotNull(level.endTile());
    assertNotNull(level.startTile());
    // if the path is bigger than 0 it means, there is a path form start to end, so the level
    // can be beaten.
    assert ((level.findPath(level.startTile(), level.endTile()).getCount() > 0));
  }
}
