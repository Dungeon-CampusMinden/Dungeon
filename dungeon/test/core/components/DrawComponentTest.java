package core.components;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.badlogic.gdx.graphics.Texture;
import core.utils.Direction;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.DirectionalState;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/** Tests for the {@link DrawComponent} class. */
public class DrawComponentTest {

  private final IPath animationPath =
      new SimpleIPath("test_assets/textures/test_hero/test_hero.png");
  private final IPath simplePath = new SimpleIPath("test_assets/textures/mailbox.png");
  private DrawComponent animationComponent;

  /** Creates a {@link DrawComponent} to be used in testing, similar to a basic hero. */
  @BeforeEach
  public void setup() {
    // Create file system handle. WARNING: This will assume all future paths to be relative to the
    // working directory (probably the root of the project)
    TextureMap.instance().clear(); // reset any existing mappings

    // Replace internal map logic to skip real texture loading
    TextureMap instance = TextureMap.instance();
    Texture dummyTexture = Mockito.mock(Texture.class);

    // Trick: preload the keys you need for your test with dummy textures. Paths must be relative to
    // working directory.
    instance.put(animationPath.pathString(), dummyTexture);
    instance.put(simplePath.pathString(), dummyTexture);

    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(animationPath);
    State stIdle = new DirectionalState("idle", animationMap);
    State stMove = new DirectionalState("move", animationMap, "run");
    State stDead = new State("dead", animationMap.get("die"));
    StateMachine sm = new StateMachine(Arrays.asList(stIdle, stMove, stDead));
    sm.addTransition(stIdle, "move", stMove);
    sm.addTransition(stMove, "move", stMove);
    sm.addTransition(stMove, "idle", stIdle);
    sm.addTransition(stIdle, "died", stDead);
    sm.addTransition(stMove, "died", stDead);
    animationComponent = new DrawComponent(sm);
  }

  /**
   * Tests sending multiple signals to the {@link DrawComponent} and ensures that state transitions
   * are applied correctly.
   *
   * <p>Checks initial state, transition to a new state, and self-transitions with updated data.
   */
  @Test
  public void complexAnimationTransition() {
    // Ensure that the current animation is initially set to the expected value
    assertEquals("idle", animationComponent.currentState().name);
    assertNull(animationComponent.currentState().getData());

    // Set a new animation and ensure that it is correctly set
    animationComponent.sendSignal("move", Direction.RIGHT);
    assertEquals("move", animationComponent.currentState().name);
    assertEquals(Direction.RIGHT, animationComponent.currentState().getData());

    // Test self transition
    animationComponent.sendSignal("move", Direction.UP);
    assertEquals("move", animationComponent.currentState().name);
    assertEquals(Direction.UP, animationComponent.currentState().getData());
  }

  /**
   * Tests that a simple image can be loaded into a {@link DrawComponent} and that the default state
   * is correctly set to "idle".
   */
  @Test
  public void simpleAnimation() {
    DrawComponent simpleAnimation = new DrawComponent(simplePath);
    assertEquals("idle", simpleAnimation.currentState().name);
  }

  /**
   * Tests that a complex animation loaded from a spritesheet (.json) correctly loads all states and
   * configuration.
   *
   * <p>Verifies that all expected states exist and that the animation configuration is correct.
   */
  @Test
  public void complexAnimationLoad() {
    assertNotEquals(null, animationComponent.stateMachine().getState("idle"));
    assertNotEquals(null, animationComponent.stateMachine().getState("move"));
    assertNotEquals(null, animationComponent.stateMachine().getState("dead"));
    assertEquals(4, animationComponent.currentAnimation().getConfig().config().get().columns());
  }
}
