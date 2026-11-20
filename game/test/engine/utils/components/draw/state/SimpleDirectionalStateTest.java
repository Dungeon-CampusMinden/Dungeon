package engine.utils.components.draw.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import engine.utils.Direction;
import engine.utils.components.draw.animation.Animation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// checks the visible result (mirrored or not) after every step, not how often mirrored() is called
class SimpleDirectionalStateTest {

  private Animation animation;
  private SimpleDirectionalState state;

  // stands in for the mirroring flag of a real animation
  private boolean mirrored;

  @BeforeEach
  void setUp() {
    mirrored = false;
    animation = mock(Animation.class);
    when(animation.mirrored(anyBoolean()))
        .thenAnswer(
            call -> {
              mirrored = call.getArgument(0);
              return animation;
            });
    when(animation.mirrored()).thenAnswer(call -> mirrored);
    state = new SimpleDirectionalState("run", animation);
  }

  // sets the direction and returns whether the animation is shown mirrored afterwards
  private boolean look(Direction direction) {
    state.setData(direction);
    assertSame(animation, state.getAnimation());
    return mirrored;
  }

  @Test
  void mirrorsWhenLookingLeft() {
    assertTrue(look(Direction.LEFT));
  }

  @Test
  void doesNotMirrorWhenLookingRight() {
    mirrored = true;

    assertFalse(look(Direction.RIGHT));
  }

  @Test
  void keepsLookingLeftWhileWalkingUpAndDown() {
    assertTrue(look(Direction.LEFT));
    assertTrue(look(Direction.UP));
    assertTrue(look(Direction.UP));
    assertTrue(look(Direction.DOWN));
  }

  @Test
  void keepsLookingRightWhileWalkingUpAndDown() {
    mirrored = true;

    assertFalse(look(Direction.RIGHT));
    assertFalse(look(Direction.DOWN));
    assertFalse(look(Direction.UP));
  }

  @Test
  void standingStillKeepsTheLastSide() {
    assertTrue(look(Direction.LEFT));
    assertTrue(look(Direction.NONE));
    assertTrue(look(Direction.UP));
  }

  @Test
  void turnsAroundWhenChangingSides() {
    assertTrue(look(Direction.LEFT));
    assertFalse(look(Direction.RIGHT));
    assertTrue(look(Direction.LEFT));
  }

  @Test
  void turnsAroundAfterWalkingUp() {
    assertTrue(look(Direction.LEFT));
    assertTrue(look(Direction.UP));
    assertFalse(look(Direction.RIGHT));
    assertFalse(look(Direction.DOWN));
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void withoutASideTheAnimationStaysAsItWas(boolean before) {
    mirrored = before;

    assertSame(animation, state.getAnimation());
    assertEquals(before, mirrored);
    assertEquals(before, look(Direction.NONE));
    assertEquals(before, look(Direction.UP));
    assertEquals(before, look(Direction.DOWN));
  }
}
