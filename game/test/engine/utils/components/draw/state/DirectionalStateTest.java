package engine.utils.components.draw.state;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import engine.utils.Direction;
import engine.utils.components.draw.animation.Animation;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DirectionalStateTest {

  private Animation down;
  private Animation left;
  private Animation up;
  private Animation right;
  private DirectionalState state;

  @BeforeEach
  void setUp() {
    down = mock(Animation.class);
    left = mock(Animation.class);
    up = mock(Animation.class);
    right = mock(Animation.class);
    state = new DirectionalState("run", down, left, up, right);
  }

  private Animation animationFor(Direction direction) {
    state.setData(direction);
    return state.getAnimation();
  }

  @Test
  void showsDownWithoutDirection() {
    assertSame(down, state.getAnimation());
  }

  @Test
  void showsDownForDownAndNone() {
    assertSame(down, animationFor(Direction.DOWN));
    assertSame(down, animationFor(Direction.NONE));
  }

  @Test
  void showsTheAnimationOfTheDirection() {
    assertSame(left, animationFor(Direction.LEFT));
    assertSame(up, animationFor(Direction.UP));
    assertSame(right, animationFor(Direction.RIGHT));
  }

  @Test
  void baseAnimationStaysDown() {
    state.setData(Direction.LEFT);

    assertSame(down, state.baseAnimation());
    assertSame(down, state.downAnimation());
  }

  @Test
  void returnsEachDirectionDirectly() {
    assertSame(left, state.leftAnimation());
    assertSame(up, state.upAnimation());
    assertSame(right, state.rightAnimation());
  }

  @Test
  void takesAnimationsFromMapWithPrefix() {
    Map<String, Animation> map =
        Map.of("walk_down", down, "walk_left", left, "walk_up", up, "walk_right", right);

    DirectionalState fromMap = new DirectionalState("run", map, "walk");

    assertSame(down, fromMap.downAnimation());
    assertSame(left, fromMap.leftAnimation());
    assertSame(up, fromMap.upAnimation());
    assertSame(right, fromMap.rightAnimation());
  }

  @Test
  void usesTheNameAsPrefixByDefault() {
    Map<String, Animation> map =
        Map.of("idle_down", down, "idle_left", left, "idle_up", up, "idle_right", right);

    DirectionalState fromMap = new DirectionalState("idle", map);

    assertSame(down, fromMap.downAnimation());
    assertSame(right, fromMap.rightAnimation());
  }
}
