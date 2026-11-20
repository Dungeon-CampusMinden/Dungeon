package engine.utils.components.draw.state;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.g2d.Sprite;
import engine.utils.components.draw.animation.Animation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StateTest {

  private Animation animation;
  private State state;

  @BeforeEach
  void setUp() {
    animation = mock(Animation.class);
    state = new State("run", animation);
  }

  @Test
  void keepsNameAndAnimation() {
    assertEquals("run", state.name);
    assertSame(animation, state.getAnimation());
    assertSame(animation, state.baseAnimation());
  }

  @Test
  void rejectsMissingNameOrAnimation() {
    assertThrows(IllegalArgumentException.class, () -> new State(null, animation));
    assertThrows(IllegalArgumentException.class, () -> new State("run", (Animation) null));
  }

  @Test
  void storesData() {
    assertNull(state.getData());

    state.setData("left");

    assertEquals("left", state.getData());
  }

  @Test
  void callsOnEnterAndOnExitWithItself() {
    List<String> calls = new ArrayList<>();
    state.setOnEnter(s -> calls.add("enter " + s.name));
    state.setOnExit(s -> calls.add("exit " + s.name));

    state.onEnter();
    state.onExit();

    assertEquals(List.of("enter run", "exit run"), calls);
  }

  @Test
  void enterAndExitWorkWithoutCallbacks() {
    assertDoesNotThrow(state::onEnter);
    assertDoesNotThrow(state::onExit);
  }

  @Test
  void newCallbackReplacesTheOldOne() {
    List<String> calls = new ArrayList<>();
    state.setOnEnter(s -> calls.add("old"));
    state.setOnEnter(s -> calls.add("new"));

    state.onEnter();

    assertEquals(List.of("new"), calls);
  }

  @Test
  void passesUpdateAndFrameToTheAnimation() {
    state.update();
    state.frameCount(3);

    verify(animation).update();
    verify(animation).frameCount(3);
  }

  @Test
  void readsSpriteAndSizesFromTheAnimation() {
    Sprite sprite = mock(Sprite.class);
    when(animation.getSprite()).thenReturn(sprite);
    when(animation.getWidth()).thenReturn(16f);
    when(animation.getHeight()).thenReturn(32f);
    when(animation.getSpriteWidth()).thenReturn(64f);
    when(animation.getSpriteHeight()).thenReturn(128f);
    when(animation.isFinished()).thenReturn(true);

    assertSame(sprite, state.getSprite());
    assertEquals(16f, state.getWidth());
    assertEquals(32f, state.getHeight());
    assertEquals(64f, state.getSpriteWidth());
    assertEquals(128f, state.getSpriteHeight());
    assertTrue(state.isAnimationFinished());
  }

  @Test
  void fromMapTakesTheAnimationWithTheSameName() {
    Animation idle = mock(Animation.class);

    State fromMap = State.fromMap(Map.of("idle", idle, "run", animation), "idle");

    assertEquals("idle", fromMap.name);
    assertSame(idle, fromMap.getAnimation());
  }

  @Test
  void fromMapRejectsMissingInput() {
    Map<String, Animation> map = Map.of("idle", animation);

    assertThrows(IllegalArgumentException.class, () -> State.fromMap(null, "idle"));
    assertThrows(IllegalArgumentException.class, () -> State.fromMap(map, null));
    assertThrows(IllegalArgumentException.class, () -> State.fromMap(map, "run"));
  }

  @Test
  void toStringContainsNameAndData() {
    state.setData("left");

    String text = state.toString();

    assertTrue(text.startsWith("State{"));
    assertTrue(text.contains("name='run'"));
    assertTrue(text.contains("data=left"));
  }
}
