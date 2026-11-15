package engine.utils.components.draw.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.g2d.Sprite;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

class StateMachineTest {

  private static final String MOVE = "move";
  private static final String STOP = "stop";

  private State idle;
  private State run;
  private State jump;
  private StateMachine machine;

  @BeforeEach
  void setUp() {
    idle = state(StateMachine.IDLE_STATE);
    run = state("run");
    jump = state("jump");
    machine = machine(idle, run, jump);
  }

  @AfterEach
  void tearDown() {
    StateMachine.setResetFrame(true);
  }

  private static State state(String name) {
    return new State(name, mock(Animation.class));
  }

  private static StateMachine machine(State... states) {
    return new StateMachine(new ArrayList<>(List.of(states)));
  }

  private static Set<String> names(List<State> states) {
    return states.stream().map(s -> s.name).collect(Collectors.toSet());
  }

  @Nested
  class Construction {

    @Test
    void startsInIdleWhenTheListHasIt() {
      StateMachine sm = machine(run, idle);

      assertSame(idle, sm.getCurrentState());
      assertEquals(StateMachine.IDLE_STATE, sm.getCurrentStateName());
    }

    @Test
    void startsInTheFirstStateWithoutIdle() {
      StateMachine sm = machine(run, jump);

      assertSame(run, sm.getCurrentState());
    }

    @Test
    void startsInTheGivenDefaultState() {
      StateMachine sm = new StateMachine(new ArrayList<>(List.of(idle, run)), run);

      assertSame(run, sm.getCurrentState());
    }

    @Test
    void fallsBackToIdleWithoutDefaultState() {
      StateMachine sm = new StateMachine(new ArrayList<>(List.of(run, idle)), null);

      assertSame(idle, sm.getCurrentState());
    }

    @Test
    void rejectsDefaultStateOutsideTheList() {
      List<State> states = new ArrayList<>(List.of(idle, run));

      assertThrows(IllegalArgumentException.class, () -> new StateMachine(states, jump));
    }

    @Test
    void rejectsMissingOrEmptyStateList() {
      assertThrows(IllegalArgumentException.class, () -> new StateMachine((List<State>) null));
      assertThrows(IllegalArgumentException.class, () -> new StateMachine(new ArrayList<>()));
    }

    @Test
    void wrapsASingleAnimationInAnIdleState() {
      Animation animation = mock(Animation.class);

      StateMachine sm = new StateMachine(animation);

      assertEquals(1, sm.states().size());
      assertEquals(StateMachine.IDLE_STATE, sm.getCurrentStateName());
      assertSame(animation, sm.getCurrentState().getAnimation());
    }

    @Test
    void buildsOneStatePerSpritesheetAnimation() {
      IPath path = new SimpleIPath("character/test");
      Animation idleAnimation = mock(Animation.class);
      Animation runAnimation = mock(Animation.class);
      try (MockedStatic<Animation> animations = mockStatic(Animation.class)) {
        animations
            .when(() -> Animation.loadAnimationSpritesheet(path))
            .thenReturn(Map.of("idle", idleAnimation, "run", runAnimation));

        StateMachine sm = new StateMachine(path);

        assertEquals(Set.of("idle", "run"), names(sm.states()));
        assertEquals(StateMachine.IDLE_STATE, sm.getCurrentStateName());
        assertSame(runAnimation, sm.getState("run").getAnimation());
      }
    }

    @Test
    void startsInTheNamedSpritesheetState() {
      IPath path = new SimpleIPath("character/test");
      try (MockedStatic<Animation> animations = mockStatic(Animation.class)) {
        animations
            .when(() -> Animation.loadAnimationSpritesheet(path))
            .thenReturn(Map.of("idle", mock(Animation.class), "run", mock(Animation.class)));

        StateMachine sm = new StateMachine(path, "run");

        assertEquals("run", sm.getCurrentStateName());
        assertThrows(IllegalArgumentException.class, () -> new StateMachine(path, "jump"));
      }
    }

    @Test
    void usesTheWholeImageAsIdleWithoutSpritesheet() {
      IPath path = new SimpleIPath("character/test.png");
      try (MockedStatic<Animation> animations = mockStatic(Animation.class);
          MockedConstruction<Animation> created = mockConstruction(Animation.class)) {
        animations.when(() -> Animation.loadAnimationSpritesheet(path)).thenReturn(null);

        StateMachine sm = new StateMachine(path);

        assertEquals(1, sm.states().size());
        assertEquals(StateMachine.IDLE_STATE, sm.getCurrentStateName());
        assertSame(created.constructed().get(0), sm.getCurrentState().getAnimation());
      }
    }

    @Test
    void stateListCannotBeChangedFromOutside() {
      List<State> states = machine.states();

      assertThrows(UnsupportedOperationException.class, () -> states.add(state("fall")));
    }
  }

  @Nested
  class Signals {

    @Test
    void switchesStateOnAMatchingSignal() {
      machine.addTransition(idle, MOVE, run);

      machine.sendSignal(new Signal(MOVE));

      assertSame(run, machine.getCurrentState());
    }

    @Test
    void ignoresSignalsWithoutTransition() {
      machine.addTransition(idle, MOVE, run);

      machine.sendSignal(new Signal(STOP));

      assertSame(idle, machine.getCurrentState());
    }

    @Test
    void onlyTransitionsOfTheCurrentStateCount() {
      machine.addTransition(run, MOVE, jump);

      machine.sendSignal(new Signal(MOVE));

      assertSame(idle, machine.getCurrentState());
    }

    @Test
    void passesTheSignalDataToTheNewState() {
      machine.addTransition(idle, MOVE, run);

      machine.sendSignal(new Signal(MOVE, "left"));

      assertEquals("left", run.getData());
    }

    @Test
    void signalWithoutDataClearsOldData() {
      machine.addTransition(idle, MOVE, run);
      run.setData("left");

      machine.sendSignal(new Signal(MOVE));

      assertNull(run.getData());
    }

    @Test
    void newTransitionForTheSameSignalReplacesTheOldOne() {
      Transition first = machine.addTransition(idle, MOVE, run);
      Transition replaced = machine.addTransition(idle, MOVE, jump);

      machine.sendSignal(new Signal(MOVE));

      assertNull(first);
      assertEquals(new Transition(MOVE, run), replaced);
      assertSame(jump, machine.getCurrentState());
    }

    @Test
    void addsTransitionsByStateName() {
      machine.addTransition(StateMachine.IDLE_STATE, MOVE, "run");

      machine.sendSignal(new Signal(MOVE));

      assertSame(run, machine.getCurrentState());
    }

    @Test
    void rejectsTransitionsBetweenUnknownStates() {
      assertThrows(
          IllegalArgumentException.class, () -> machine.addTransition("fall", MOVE, "run"));
      assertThrows(
          IllegalArgumentException.class,
          () -> machine.addTransition(StateMachine.IDLE_STATE, MOVE, "fall"));
    }

    @Test
    void removedTransitionNoLongerSwitches() {
      machine.addTransition(idle, MOVE, run);

      assertTrue(machine.removeTransition(StateMachine.IDLE_STATE, MOVE));
      assertFalse(machine.removeTransition(idle, MOVE));
      machine.sendSignal(new Signal(MOVE));

      assertSame(idle, machine.getCurrentState());
    }

    @Test
    void rejectsRemovingTransitionsOfUnknownStates() {
      assertThrows(IllegalArgumentException.class, () -> machine.removeTransition("fall", MOVE));
    }
  }

  @Nested
  class StateChange {

    @Test
    void leavesTheOldStateBeforeEnteringTheNewOne() {
      List<String> calls = new ArrayList<>();
      idle.setOnExit(s -> calls.add("exit " + s.name));
      run.setOnEnter(s -> calls.add("enter " + s.name));
      machine.addTransition(idle, MOVE, run);

      machine.sendSignal(new Signal(MOVE));

      assertEquals(List.of("exit idle", "enter run"), calls);
    }

    @Test
    void sameStateOnlyUpdatesItsData() {
      List<String> calls = new ArrayList<>();
      machine.setState("run", "left");
      run.setOnEnter(s -> calls.add("enter"));
      run.setOnExit(s -> calls.add("exit"));
      machine.addTransition(run, MOVE, run);

      machine.sendSignal(new Signal(MOVE, "right"));

      assertTrue(calls.isEmpty());
      assertEquals("right", run.getData());
      verify(run.getAnimation()).frameCount(0);
    }

    @Test
    void restartsTheAnimationOfTheNewState() {
      machine.addTransition(idle, MOVE, run);

      machine.sendSignal(new Signal(MOVE));

      verify(run.getAnimation()).frameCount(0);
    }

    @Test
    void keepsTheAnimationFrameWhenResetIsTurnedOff() {
      StateMachine.setResetFrame(false);
      machine.addTransition(idle, MOVE, run);

      machine.sendSignal(new Signal(MOVE));

      assertSame(run, machine.getCurrentState());
      verify(run.getAnimation(), never()).frameCount(anyInt());
    }

    @Test
    void setStateSwitchesDirectlyWithData() {
      machine.setState("jump", "up");

      assertSame(jump, machine.getCurrentState());
      assertEquals("up", jump.getData());
    }

    @Test
    void setStateRejectsUnknownStates() {
      assertThrows(IllegalArgumentException.class, () -> machine.setState("fall", null));
    }

    @Test
    void resetGoesToTheFirstStateInTheList() {
      StateMachine sm = new StateMachine(new ArrayList<>(List.of(run, idle)), idle);
      sm.setState(StateMachine.IDLE_STATE, "left");

      sm.reset();

      assertSame(run, sm.getCurrentState());
      assertNull(run.getData());
    }
  }

  @Nested
  class EpsilonTransitions {

    @Test
    void switchesInUpdateOnceTheConditionHolds() {
      boolean[] finished = {false};
      machine.addEpsilonTransition(idle, s -> finished[0], run);

      machine.update();
      assertSame(idle, machine.getCurrentState());

      finished[0] = true;
      machine.update();
      assertSame(run, machine.getCurrentState());
    }

    @Test
    void updatesTheAnimationOfTheCurrentState() {
      machine.update();

      verify(idle.getAnimation()).update();
      verify(run.getAnimation(), never()).update();
    }

    @Test
    void conditionGetsTheCurrentState() {
      List<State> checked = new ArrayList<>();
      machine.addEpsilonTransition(idle, s -> checked.add(s) && false, run);

      machine.update();

      assertEquals(List.of(idle), checked);
    }

    @Test
    void takesTheDataFromTheSupplier() {
      machine.addEpsilonTransition(idle, s -> true, run, () -> "left");

      machine.update();

      assertEquals("left", run.getData());
    }

    @Test
    void withoutSupplierTheDataIsEmpty() {
      run.setData("left");
      machine.addEpsilonTransition(idle, s -> true, run);

      machine.update();

      assertNull(run.getData());
    }

    @Test
    void newConditionForTheSameTargetReplacesTheOldOne() {
      EpsilonTransition first = machine.addEpsilonTransition(idle, s -> true, run);
      EpsilonTransition replaced = machine.addEpsilonTransition(idle, s -> false, run);

      machine.update();

      assertNull(first);
      assertSame(run, replaced.targetState());
      assertSame(idle, machine.getCurrentState());
    }

    @Test
    void onlyConditionsOfTheCurrentStateAreChecked() {
      machine.addEpsilonTransition(run, s -> true, jump);

      machine.update();

      assertSame(idle, machine.getCurrentState());
    }
  }

  @Nested
  class States {

    @Test
    void findsStatesByName() {
      assertSame(run, machine.getState("run"));
      assertNull(machine.getState("fall"));
    }

    @Test
    void rejectsSearchingWithoutName() {
      assertThrows(IllegalArgumentException.class, () -> machine.getState(null));
    }

    @Test
    void removesStatesByName() {
      assertSame(run, machine.removeState("run"));
      assertNull(machine.removeState("run"));

      assertNull(machine.getState("run"));
      assertEquals(Set.of("idle", "jump"), names(machine.states()));
    }

    @Test
    void removedStateIsNoLongerReachable() {
      machine.addTransition(idle, MOVE, run);
      machine.addEpsilonTransition(idle, s -> true, run);

      assertTrue(machine.removeState(run));
      machine.sendSignal(new Signal(MOVE));
      machine.update();

      assertSame(idle, machine.getCurrentState());
    }
  }

  @Nested
  class CurrentAnimation {

    @Test
    void comesFromTheCurrentState() {
      Animation animation = idle.getAnimation();
      Sprite sprite = mock(Sprite.class);
      when(animation.getSprite()).thenReturn(sprite);
      when(animation.getWidth()).thenReturn(16f);
      when(animation.getHeight()).thenReturn(32f);
      when(animation.getSpriteWidth()).thenReturn(64f);
      when(animation.getSpriteHeight()).thenReturn(128f);
      when(animation.isFinished()).thenReturn(true);

      assertSame(sprite, machine.getSprite());
      assertEquals(16f, machine.getWidth());
      assertEquals(32f, machine.getHeight());
      assertEquals(64f, machine.getSpriteWidth());
      assertEquals(128f, machine.getSpriteHeight());
      assertTrue(machine.isAnimationFinished());
    }

    @Test
    void toStringShowsCurrentStateAndTransitions() {
      machine.addTransition(idle, MOVE, run);

      String text = machine.toString();

      assertTrue(text.contains("currentState=idle"));
      assertTrue(text.contains("-- \"move\" -> run"));
    }
  }
}
