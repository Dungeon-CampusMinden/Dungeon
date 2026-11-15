package engine.utils.components.draw.state;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import engine.utils.Direction;
import engine.utils.components.draw.animation.Animation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleDirectionalStateTest {

  private Animation animation;
  private SimpleDirectionalState state;

  @BeforeEach
  void setUp() {
    animation = mock(Animation.class);
    state = new SimpleDirectionalState("run", animation);
  }

  private void look(Direction... directions) {
    for (Direction direction : directions) {
      state.setData(direction);
      assertSame(animation, state.getAnimation());
    }
  }

  @Test
  void leavesTheAnimationAloneWithoutDirection() {
    assertSame(animation, state.getAnimation());
    look(Direction.NONE);

    verify(animation, never()).mirrored(anyBoolean());
  }

  @Test
  void mirrorsWhenLookingLeft() {
    look(Direction.LEFT);

    verify(animation).mirrored(true);
  }

  @Test
  void doesNotMirrorWhenLookingRight() {
    look(Direction.RIGHT);

    verify(animation).mirrored(false);
  }

  @Test
  void keepsLookingLeftWhileWalkingUpAndDown() {
    look(Direction.LEFT, Direction.UP, Direction.UP, Direction.DOWN);

    verify(animation, times(2)).mirrored(true);
    verify(animation, never()).mirrored(false);
  }

  @Test
  void keepsLookingRightWhileWalkingUpAndDown() {
    look(Direction.RIGHT, Direction.DOWN, Direction.UP);

    verify(animation, times(2)).mirrored(false);
    verify(animation, never()).mirrored(true);
  }

  @Test
  void standingStillKeepsTheLastSide() {
    look(Direction.LEFT, Direction.NONE, Direction.UP);

    verify(animation, times(2)).mirrored(true);
  }

  @Test
  void turnsAroundWhenChangingSides() {
    look(Direction.LEFT, Direction.RIGHT);

    verify(animation).mirrored(true);
    verify(animation).mirrored(false);
  }

  @Test
  void upAndDownWithoutEarlierSideChangeNothing() {
    look(Direction.UP, Direction.DOWN);

    verify(animation, never()).mirrored(anyBoolean());
  }
}
