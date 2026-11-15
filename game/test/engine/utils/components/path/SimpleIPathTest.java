package engine.utils.components.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/** Tests for {@link SimpleIPath}. */
class SimpleIPathTest {

  @Test
  public void keepsTheOriginalPathAndUsesZeroPriority() {
    SimpleIPath path = new SimpleIPath("textures/../Hero.png");

    assertEquals("textures/../Hero.png", path.pathString());
    assertEquals(0, path.priority());
  }

  @Test
  public void distinctInstancesWithTheSamePathAreNotEqual() {
    SimpleIPath first = new SimpleIPath("textures/Hero.png");
    SimpleIPath second = new SimpleIPath("textures/Hero.png");

    assertNotEquals(first, second);
  }
}
