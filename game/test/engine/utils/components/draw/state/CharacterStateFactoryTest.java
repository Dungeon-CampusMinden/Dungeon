package engine.utils.components.draw.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.systems.VelocitySystem;
import engine.utils.Direction;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.animation.AnimationConfig;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class CharacterStateFactoryTest {

  private static final IPath PATH = new SimpleIPath("character/test");

  private MockedStatic<Animation> animations;

  @BeforeEach
  void setUp() {
    animations = mockStatic(Animation.class);
  }

  @AfterEach
  void tearDown() {
    animations.close();
  }

  private void spritesheet(Map<String, Animation> map) {
    animations.when(() -> Animation.loadAnimationSpritesheet(PATH)).thenReturn(map);
  }

  private static Animation animation() {
    Animation animation = mock(Animation.class);
    when(animation.getConfig()).thenReturn(new AnimationConfig());
    return animation;
  }

  private static Animation withClone(Animation original) {
    Animation copy = animation();
    when(original.clone()).thenReturn(copy);
    return copy;
  }

  private static State idleOf(StateMachine sm) {
    return sm.getState(StateMachine.IDLE_STATE);
  }

  private static State runOf(StateMachine sm) {
    return sm.getState(VelocitySystem.STATE_NAME);
  }

  @Test
  void buildsIdleAndFasterRunFromOneIdleAnimation() {
    Animation idle = animation();
    Animation run = withClone(idle);
    spritesheet(Map.of("idle", idle));

    StateMachine sm = CharacterStateFactory.createStateMachine(PATH);

    assertEquals(List.of("idle", "run"), sm.states().stream().map(s -> s.name).toList());
    assertSame(idleOf(sm), sm.getCurrentState());
    assertInstanceOf(SimpleDirectionalState.class, idleOf(sm));
    assertInstanceOf(SimpleDirectionalState.class, runOf(sm));
    assertSame(idle, idleOf(sm).getAnimation());
    assertSame(run, runOf(sm).getAnimation());
    assertEquals(10, idle.getConfig().framesPerSprite());
    assertEquals(5, run.getConfig().framesPerSprite());
  }

  @Test
  void runSpeedCanBeChosen() {
    Animation idle = animation();
    Animation run = withClone(idle);
    spritesheet(Map.of("idle", idle));

    CharacterStateFactory.createStateMachine(PATH, 5f);

    assertEquals(2, run.getConfig().framesPerSprite());
  }

  @Test
  void usesIdleAndRunAnimationsAsTheyAre() {
    Animation idle = animation();
    Animation run = animation();
    spritesheet(Map.of("idle", idle, "run", run));

    StateMachine sm = CharacterStateFactory.createStateMachine(PATH);

    assertSame(idle, idleOf(sm).getAnimation());
    assertSame(run, runOf(sm).getAnimation());
    assertEquals(10, run.getConfig().framesPerSprite());
    verify(idle, never()).clone();
  }

  @Test
  void buildsDirectionalStatesFromFourIdleAnimations() {
    Map<String, Animation> map = new HashMap<>();
    Map<String, Animation> runs = new HashMap<>();
    for (String side : List.of("down", "left", "up", "right")) {
      Animation idle = animation();
      map.put("idle_" + side, idle);
      runs.put(side, withClone(idle));
    }
    spritesheet(map);

    StateMachine sm = CharacterStateFactory.createStateMachine(PATH);

    DirectionalState idle = assertInstanceOf(DirectionalState.class, idleOf(sm));
    DirectionalState run = assertInstanceOf(DirectionalState.class, runOf(sm));
    assertSame(map.get("idle_left"), idle.leftAnimation());
    assertSame(runs.get("down"), run.downAnimation());
    assertSame(runs.get("left"), run.leftAnimation());
    assertSame(runs.get("up"), run.upAnimation());
    assertSame(runs.get("right"), run.rightAnimation());
    runs.values().forEach(a -> assertEquals(5, a.getConfig().framesPerSprite()));
  }

  @Test
  void buildsDirectionalStatesFromIdleAndRunAnimations() {
    Map<String, Animation> map = new HashMap<>();
    for (String side : List.of("down", "left", "up", "right")) {
      map.put("idle_" + side, animation());
      map.put("run_" + side, animation());
    }
    spritesheet(map);

    StateMachine sm = CharacterStateFactory.createStateMachine(PATH);

    DirectionalState idle = assertInstanceOf(DirectionalState.class, idleOf(sm));
    DirectionalState run = assertInstanceOf(DirectionalState.class, runOf(sm));
    assertSame(map.get("idle_up"), idle.upAnimation());
    assertSame(map.get("run_down"), run.downAnimation());
    assertSame(map.get("run_right"), run.rightAnimation());
  }

  @Test
  void switchesBetweenIdleAndRunWithMovementSignals() {
    spritesheet(Map.of("idle", animation(), "run", animation()));
    StateMachine sm = CharacterStateFactory.createStateMachine(PATH);

    sm.sendSignal(new Signal(VelocitySystem.MOVE_SIGNAL, Direction.LEFT));
    assertSame(runOf(sm), sm.getCurrentState());

    sm.sendSignal(new Signal(VelocitySystem.MOVE_SIGNAL, Direction.UP));
    assertSame(runOf(sm), sm.getCurrentState());
    assertEquals(Direction.UP, sm.getCurrentState().getData());

    sm.sendSignal(new Signal(VelocitySystem.IDLE_SIGNAL));
    assertSame(idleOf(sm), sm.getCurrentState());
  }

  @Test
  void rejectsMissingOrEmptySpritesheet() {
    spritesheet(null);
    assertThrows(
        IllegalArgumentException.class, () -> CharacterStateFactory.createStateMachine(PATH));

    spritesheet(Map.of());
    assertThrows(
        IllegalArgumentException.class, () -> CharacterStateFactory.createStateMachine(PATH));
  }

  @Test
  void rejectsSpritesheetWithoutIdleAnimation() {
    spritesheet(Map.of("jump", animation(), "run", animation()));

    assertThrows(
        IllegalArgumentException.class, () -> CharacterStateFactory.createStateMachine(PATH));
  }
}
