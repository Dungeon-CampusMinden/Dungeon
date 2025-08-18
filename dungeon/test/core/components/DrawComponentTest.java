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
import java.io.IOException;
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

  /** WTF? . */
  @BeforeEach
  public void setup() throws IOException {
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

  /** Tests a simple image loading properly into a DrawComponent */
  @Test
  public void simpleAnimation() {
    DrawComponent simpleAnimation = new DrawComponent(simplePath);
    assertEquals("idle", simpleAnimation.currentState().name);
  }

  /**
   * Tests if the complex animation loaded from the .json file properly loads all 4 images of the
   * animation
   */
  @Test
  public void complexAnimationLoad() {
    assertNotEquals(null, animationComponent.stateMachine().getState("idle"));
    assertNotEquals(null, animationComponent.stateMachine().getState("move"));
    assertNotEquals(null, animationComponent.stateMachine().getState("dead"));
    assertEquals(4, animationComponent.currentAnimation().getConfig().config().columns());
  }
}
