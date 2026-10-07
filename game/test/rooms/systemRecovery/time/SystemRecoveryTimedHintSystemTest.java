package rooms.systemRecovery.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.PlayerComponent;
import engine.game.PreRunConfiguration;
import engine.systems.LevelSystem;
import feature.hints.HintSystem;
import feature.petrinet.PetriNetSystem;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import testingUtils.MockNetworkHandler;

/** Exercises hint scheduling against real ECS components and hints, without level reflection. */
class SystemRecoveryTimedHintSystemTest {

  private final AtomicLong clock = new AtomicLong();
  private final AtomicBoolean initialInput = new AtomicBoolean();
  private final SystemRecoveryTimeLimit timer =
      new SystemRecoveryTimeLimit(Duration.ofMinutes(4), clock::get);
  private final SystemRecoveryLevel level = mock(SystemRecoveryLevel.class);
  private final SystemRecoveryTimedHintSystem timedHints = new SystemRecoveryTimedHintSystem(true);
  private final HintSystem hints = new HintSystem();
  private Entity timerEntity;

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    SystemRecovery.configureDebugMode();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
    MockNetworkHandler.useLocalNetworkHandler();
    Game.add(new LevelSystem());
    Game.currentLevel(level);
    Game.add(hints);
    Game.add(new PetriNetSystem());
    SystemRecoveryProgressNet.reset();
    SystemRecoveryProgressNet.initialize();
    SystemRecoveryQuestLogUtil.initializeQuestLog();
    when(level.initialTerminalAttemptRecorded()).thenAnswer(_ -> initialInput.get());
    doAnswer(
            _ -> {
              timer.postponeHint();
              return null;
            })
        .when(level)
        .resetTimedHintDelay();
    timerEntity = new Entity("timer");
    timerEntity.add(timer);
    Game.add(timerEntity);
    Entity player = new Entity("Ada");
    player.add(new PlayerComponent(true, "Ada"));
    Game.add(player);
    Game.add(timedHints);
  }

  @AfterEach
  void tearDown() {
    Game.currentLevel(null);
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    SystemRecovery.configureDebugMode();
    MockNetworkHandler.useLocalNetworkHandler();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void initialInactivityRingsInBothModesWithoutConsumingAHint(boolean forceHints) {
    when(level.triggerIdleOpeningCall()).thenReturn(true, false);
    SystemRecoveryTimedHintSystem assistance = new SystemRecoveryTimedHintSystem(forceHints);
    timer.start();
    assistance.execute();
    advanceToMinute(4);
    clock.decrementAndGet();
    assistance.execute();
    verify(level, never()).triggerIdleOpeningCall();
    clock.incrementAndGet();
    assistance.execute();
    assistance.execute();
    verify(level, times(1)).triggerIdleOpeningCall();
    assertEquals(0, acceptedHints());
    advanceToMinute(8);
    assistance.execute();
    assertEquals(0, acceptedHints());
    verify(level, never()).triggerTimedHintReminder();
    verify(level, never()).resetTimedHintDelay();
  }

  @Test
  void firstObservationDoesNotResetAnAlreadyDueOpeningCall() {
    timer.start();
    advanceToMinute(4);
    timedHints.execute();
    verify(level).triggerIdleOpeningCall();
  }

  @Test
  void initialCallDoesNotDependOnTheHintSystem() {
    Game.remove(HintSystem.class);
    timer.start();
    advanceToMinute(4);
    timedHints.execute();
    verify(level).triggerIdleOpeningCall();
  }

  @Test
  void firstInputReenablesTheConfiguredAutomaticHints() {
    when(level.triggerIdleOpeningCall()).thenReturn(true);
    timer.start();
    timedHints.execute();
    advanceToMinute(4);
    timedHints.execute();
    assertEquals(0, acceptedHints());
    initialInput.set(true);
    advanceToMinute(8);
    timedHints.execute();
    assertEquals(1, acceptedHints());
    verify(level, times(1)).triggerIdleOpeningCall();
  }

  @Test
  void acceptedPartialProgressAndManualHintsPostponeAutomaticHelp() {
    initialInput.set(true);
    timer.start();
    timedHints.execute();
    advanceToMinute(3);
    level.resetTimedHintDelay();
    advanceToMinute(6);
    timedHints.execute();
    assertEquals(0, acceptedHints());
    advanceToMinute(7);
    timedHints.execute();
    assertEquals(1, acceptedHints());
    // Close the automatic dialog before accepting a subsequent telephone hint.
    Game.entities()
        .filter(entity -> entity.isPresent(feature.components.UIComponent.class))
        .toList()
        .forEach(Game::remove);
    hints.acceptSharedHint();
    timedHints.execute();
    advanceToMinute(10);
    timedHints.execute();
    assertEquals(2, acceptedHints());
    advanceToMinute(11);
    timedHints.execute();
    assertEquals(3, acceptedHints());
  }

  @Test
  void reminderDoesNotConsumeHintsOrGetCancelledByTheFollowingTick() {
    initialInput.set(true);
    when(level.triggerTimedHintReminder()).thenReturn(true);
    SystemRecoveryTimedHintSystem reminders = new SystemRecoveryTimedHintSystem(false);
    timer.start();
    reminders.execute();
    advanceToMinute(4);
    reminders.execute();
    reminders.execute();
    verify(level, times(1)).triggerTimedHintReminder();
    verify(level, never()).resetTimedHintDelay();
    assertEquals(0, acceptedHints());
    advanceToMinute(8);
    reminders.execute();
    verify(level, times(2)).triggerTimedHintReminder();
  }

  @Test
  void stepChangesRestartTheHintInterval() {
    timer.start();
    timedHints.execute();
    advanceToMinute(3);
    SystemRecoveryProgressNet.restoreActiveStep(SystemRecoveryLearningStep.MODULE_ARRAY);
    timedHints.execute();
    advanceToMinute(4);
    timedHints.execute();
    assertEquals(0, acceptedHints());
    advanceToMinute(7);
    timedHints.execute();
    assertEquals(1, acceptedHints());
    verify(level).resetTimedHintDelay();
  }

  @Test
  void introductionExpiryAndCompletionDoNotRingThePhone() {
    advanceToMinute(4);
    timedHints.execute();
    timer.restore(0);
    timedHints.execute();
    timer.restore(3600);
    SystemRecoveryProgressNet.restoreActiveStep(SystemRecoveryLearningStep.COMPLETE);
    timedHints.execute();
    advanceToMinute(8);
    timedHints.execute();
    verify(level, never()).triggerIdleOpeningCall();
    verify(level, never()).triggerTimedHintReminder();
    assertFalse(hints.peekSharedHint().isPresent());
  }

  @Test
  void exhaustedHintsDoNotTriggerOptionalReminders() {
    initialInput.set(true);
    SystemRecoveryTimedHintSystem reminders = new SystemRecoveryTimedHintSystem(false);
    timer.start();
    reminders.execute();
    int accepted = 0;
    while (hints.acceptSharedHint().isPresent()) accepted++;
    assertEquals(accepted, acceptedHints());
    reminders.execute();
    advanceToMinute(4);
    reminders.execute();
    verify(level, never()).triggerTimedHintReminder();
  }

  @Test
  void systemsRequireTheRuntimeComponentAndRemainAuthoritativeDuringPause() {
    SystemRecoveryTimerSystem countdown = new SystemRecoveryTimerSystem();
    countdown.stop();
    timedHints.stop();
    assertTrue(countdown.isRunning());
    assertTrue(timedHints.isRunning());
    assertEquals(System.AuthoritativeSide.SERVER, countdown.authoritativeSide());
    assertEquals(System.AuthoritativeSide.SERVER, timedHints.authoritativeSide());
    assertTrue(countdown.filterRules().contains(SystemRecoveryTimeLimit.class));
    assertTrue(timedHints.filterRules().contains(SystemRecoveryTimeLimit.class));
    Game.remove(timerEntity);
    timer.start();
    advanceToMinute(4);
    timedHints.execute();
    verify(level, never()).triggerIdleOpeningCall();
  }

  @Test
  void levelEditorCannotTriggerCalls() {
    timer.start();
    advanceToMinute(4);
    SystemRecovery.configureDebugMode("--leveleditor");
    timedHints.execute();
    SystemRecovery.configureDebugMode();
    verify(level, never()).triggerIdleOpeningCall();
    verify(level, never()).triggerTimedHintReminder();
  }

  private void advanceToMinute(int minute) {
    clock.set(Duration.ofMinutes(minute).toMillis());
  }

  private int acceptedHints() {
    int id =
        SystemRecoveryProgressNet.hintEntityId(SystemRecoveryProgressNet.activeStep().orElseThrow())
            .orElseThrow();
    return hints.sharedProgress(Game.findEntityById(id).orElseThrow()).acceptedCount();
  }
}
