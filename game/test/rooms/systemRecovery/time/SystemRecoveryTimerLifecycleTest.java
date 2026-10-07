package rooms.systemRecovery.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import engine.Entity;
import engine.Game;
import engine.components.InputComponent;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.game.PreRunConfiguration;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.systems.LevelSystem;
import engine.time.PlayClock;
import engine.utils.IVoidFunction;
import engine.utils.Point;
import engine.utils.Tuple;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.hints.HintSystem;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.petrinet.PetriNetSystem;
import feature.timer.WorldTimerComponent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.save.SystemRecoverySave;
import rooms.systemRecovery.story.SystemRecoveryDialogTriggers;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import rooms.systemRecovery.util.SystemRecoveryText;
import testingUtils.MockNetworkHandler;

/** Exercises the level callbacks with a virtual clock and no real save file or graphics. */
class SystemRecoveryTimerLifecycleTest {

  private final AtomicLong clock = new AtomicLong();
  private final PlayClock playClock = new PlayClock(clock::get);
  private final SystemRecoveryTimeLimit timer =
      new SystemRecoveryTimeLimit(Duration.ofMinutes(4), () -> playClock);
  private final UUID runId = UUID.randomUUID();
  private final SystemRecoveryTimerSystem countdown = new SystemRecoveryTimerSystem();
  private final AtomicReference<Runnable> finishCutscene = new AtomicReference<>();
  private final AtomicReference<List<Tuple<String, Integer>>> cutscenePages =
      new AtomicReference<>();
  private final AtomicReference<IVoidFunction> finishControls = new AtomicReference<>();
  private SystemRecoveryLevel level;
  private Entity player;
  private MockedStatic<SystemRecoverySave> saves;
  private MockedStatic<BlackFadeCutscene> cutscenes;
  private MockedStatic<DialogFactory> dialogs;

  @BeforeEach
  void setUp() throws ReflectiveOperationException {
    Game.removeAllEntities();
    Game.removeAllSystems();
    SystemRecovery.configureDebugMode();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
    MockNetworkHandler.useLocalNetworkHandler();
    Game.add(new LevelSystem());
    Game.currentLevel(null);
    Game.add(new HintSystem());
    Game.add(new PetriNetSystem());
    SystemRecoveryProgressNet.reset();
    SystemRecoveryProgressNet.initialize();
    SystemRecoveryQuestLogUtil.initializeQuestLog();

    Map<String, Point> points = Map.of("timer", new Point(1, 1));
    level =
        new SystemRecoveryLevel(
            new LevelElement[][] {{LevelElement.FLOOR}}, DesignLabel.DEFAULT, points);
    setField("resolvedPoints", points);
    setField("timeLimit", timer);
    setField("runId", runId);
    Game.currentLevel(level);
    player = new Entity("Ada");
    player.add(new PlayerComponent(true, "Ada"));
    player.add(new InputComponent());
    player.add(new PositionComponent(new Point(1, 1)));
    Game.add(player);
    saves = mockStatic(SystemRecoverySave.class);
    cutscenes = mockStatic(BlackFadeCutscene.class);
    dialogs = mockStatic(DialogFactory.class);
    cutscenes
        .when(
            () ->
                BlackFadeCutscene.show(
                    any(), anyBoolean(), anyBoolean(), anyBoolean(), any(), any(int[].class)))
        .thenAnswer(
            invocation -> {
              cutscenePages.set(invocation.getArgument(0));
              finishCutscene.set(invocation.getArgument(4));
              return null;
            });
    dialogs
        .when(
            () ->
                DialogFactory.showDialogDialog(
                    anyString(), any(IVoidFunction.class), any(int[].class)))
        .thenAnswer(
            invocation -> {
              finishControls.set(invocation.getArgument(1));
              return null;
            });
    invoke("setupTimer");
    Game.add(countdown);
  }

  @AfterEach
  void tearDown() {
    dialogs.close();
    cutscenes.close();
    saves.close();
    Game.currentLevel(null);
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    SystemRecovery.configureDebugMode();
  }

  @Test
  void timerIsSpawnedAtCustomPointAndHasAnInteraction() {
    Entity display = timerEntity();
    assertEquals(new Point(1, 1), display.fetch(PositionComponent.class).orElseThrow().position());
    assertTrue(display.isPresent(InteractionComponent.class));
    assertEquals(timer, display.fetch(SystemRecoveryTimeLimit.class).orElseThrow());
    display.fetch(InteractionComponent.class).orElseThrow().triggerInteraction(display, player);
    dialogs.verify(
        () ->
            DialogFactory.showOkDialog(
                eq(SystemRecoveryText.key("timer.remaining", 60)),
                eq(SystemRecoveryText.key("timer.title")),
                any(IVoidFunction.class),
                any(int[].class)));
    assertEquals(3600, display.fetch(WorldTimerComponent.class).orElseThrow().duration());
  }

  @Test
  void timerNamesTheLastStartedMinuteInTheSingular() {
    Entity display = timerEntity();
    InteractionComponent interaction = display.fetch(InteractionComponent.class).orElseThrow();
    playWithRemaining(61);
    interaction.triggerInteraction(display, player);
    playWithRemaining(60);
    interaction.triggerInteraction(display, player);

    for (String text :
        List.of(
            SystemRecoveryText.key("timer.remaining", 2),
            SystemRecoveryText.key("timer.remaining-one"))) {
      dialogs.verify(
          () ->
              DialogFactory.showOkDialog(
                  eq(text),
                  eq(SystemRecoveryText.key("timer.title")),
                  any(IVoidFunction.class),
                  any(int[].class)));
    }
  }

  @Test
  void zeroBudgetRejectsLateProgressAndShowsOneFailureCutscene() throws Exception {
    Entity module = new Entity("module");
    module.add(new InteractionComponent(new Interaction((_, _) -> {})));
    Game.add(module);
    setField("terminalsUnlocked", true);
    playWithRemaining(1);
    clock.set(1000);

    assertTrue(SystemRecoveryLevel.timeLimitExpired());
    assertFalse(SystemRecoveryLevel.terminalsUnlocked());
    assertFalse(SystemRecoveryProgressNet.complete(SystemRecoveryLearningStep.ENERGY_ARRAY));
    countdown.execute();
    countdown.execute();

    assertTrue(player.fetch(InputComponent.class).orElseThrow().deactivateControls());
    assertFalse(module.isPresent(InteractionComponent.class));
    assertEquals(
        SystemRecoveryLearningStep.ENERGY_ARRAY,
        SystemRecoveryProgressNet.activeStep().orElseThrow());
    assertEquals(SystemRecoveryText.timeoutEndingPages(), cutscenePages.get());
    cutscenes.verify(
        () ->
            BlackFadeCutscene.show(
                eq(SystemRecoveryText.timeoutEndingPages()),
                eq(true),
                eq(false),
                eq(false),
                any(),
                eq(new int[] {player.id()})),
        times(1));
    try (MockedStatic<Game> game = mockStatic(Game.class)) {
      finishCutscene.get().run();
      game.verify(() -> Game.exit("System Recovery time limit expired"));
      game.verify(Game::complete, never());
    }
    // The last checkpoint stays resumable with the budget it was saved with.
    saves.verifyNoInteractions();
  }

  @Test
  void autosaveStopsAsSoonAsTimeIsUpButContinuesDuringTheSuccessfulEnding() throws Exception {
    saves
        .when(() -> SystemRecoverySave.capture(any(), any(), any(), any(), any()))
        .thenCallRealMethod();
    setField("resolvedPoints", pointsWithFarAwayDialogTriggers());
    setField("introSuppressed", true);
    SystemRecoveryProgressNet.restoreActiveStep(SystemRecoveryLearningStep.MODULE_ARRAY);

    playWithRemaining(1);
    clock.addAndGet(1000);
    invoke("onTick");
    saves.verify(() -> SystemRecoverySave.write(any(SystemRecoverySave.SaveData.class)), never());

    playWithRemaining(100);
    setField("endingTriggered", true);
    invoke("onTick");
    saves.verify(() -> SystemRecoverySave.write(any(SystemRecoverySave.SaveData.class)), times(1));
  }

  @Test
  void completedRunKeepsItsRemainingTime() {
    playWithRemaining(10);
    SystemRecoveryProgressNet.restoreActiveStep(SystemRecoveryLearningStep.COMPLETE);
    countdown.execute();
    clock.set(Duration.ofHours(1).toMillis());
    countdown.execute();
    assertEquals(10, timer.remainingSeconds());
    assertFalse(SystemRecoveryLevel.timeLimitExpired());
    cutscenes.verifyNoInteractions();
  }

  /**
   * Runs play with the given budget left, as after loading a save.
   *
   * @param seconds remaining budget
   */
  private void playWithRemaining(int seconds) {
    playClock.configure(1);
    playClock.restore((SystemRecoveryTimeLimit.TOTAL_SECONDS - seconds) * 1000L);
    playClock.participantJoined((short) 0, player.id());
    playClock.ready();
  }

  private Entity timerEntity() {
    return Game.entities()
        .filter(entity -> entity.name().equals("system_recovery_timer"))
        .findFirst()
        .orElseThrow();
  }

  private static Map<String, Point> pointsWithFarAwayDialogTriggers() {
    Map<String, Point> points = new HashMap<>(Map.of("timer", new Point(1, 1)));
    SystemRecoveryDialogTriggers.ROOM_ENTRY.forEach(
        trigger -> points.put(trigger.pointName(), new Point(50, 50)));
    return points;
  }

  private void setField(String name, Object value) throws ReflectiveOperationException {
    Field field = SystemRecoveryLevel.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(level, value);
  }

  private void invoke(String name) throws ReflectiveOperationException {
    Method method = SystemRecoveryLevel.class.getDeclaredMethod(name);
    method.setAccessible(true);
    method.invoke(level);
  }
}
