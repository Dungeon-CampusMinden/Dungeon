package core.components;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import core.utils.components.draw.CoreAnimations;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

/** Tests for the {@link DrawComponent} class. */
public class DrawComponentTest {

  private final IPath animationPath = new SimpleIPath("textures/test_hero");
  private DrawComponent animationComponent;

  /** WTF? . */
  @Before
  public void setup() throws IOException {
    animationComponent = new DrawComponent(animationPath);
  }

  /** WTF? . */
  @Test
  public void currentAnimation() {
    // Ensure that the current animation is initially set to the expected value
    assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_LEFT));
    // Set a new animation and ensure that it is correctly set
    animationComponent.currentAnimation(CoreAnimations.IDLE_RIGHT);
    assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_RIGHT));
  }

  /** WTF? . */
  @Test
  public void currentAnimationWithMultiplePaths() {
    // IDLE_DOWN and IDLE_UP don't exist, so IDLE_LEFT is expected
    animationComponent.currentAnimation(
        CoreAnimations.IDLE_DOWN, CoreAnimations.IDLE_UP, CoreAnimations.IDLE_LEFT);
    assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_LEFT));

    // existing animation, not existing animation, existing animation
    animationComponent.currentAnimation(
        CoreAnimations.IDLE_RIGHT, CoreAnimations.IDLE_DOWN, CoreAnimations.IDLE_RIGHT);
    assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_RIGHT));
  }

  /** WTF? . */
  @Test
  public void currentAnimationWithMultiplePathsAllValid() {
    // IDLE_LEFT and IDLE_RIGHT exist, but IDLE_RIGHT is the first animation, so expected value
    animationComponent.currentAnimation(CoreAnimations.IDLE_LEFT, CoreAnimations.IDLE_RIGHT);
    assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_LEFT));
  }

  /** WTF? . */
  @Test
  public void animationWithMultiplePathsNoValid() {
    // Animation does not exist, no change in animation
    animationComponent.currentAnimation(CoreAnimations.IDLE_DOWN);
    assertTrue(animationComponent.isCurrentAnimation(CoreAnimations.IDLE_LEFT));
  }

  /** WTF? . */
  @Test
  public void getAnimations() {
    assertTrue(animationComponent.animation(CoreAnimations.RUN_LEFT).isPresent());
  }

  /** WTF? . */
  @Test
  public void hasAnimations() {
    assertTrue(animationComponent.hasAnimation(CoreAnimations.RUN_LEFT));
    assertFalse(animationComponent.hasAnimation(CoreAnimations.RUN_DOWN));
  }
}
