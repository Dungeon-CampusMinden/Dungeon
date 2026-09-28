package rooms.systemRecovery.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.utils.IVoidFunction;
import feature.components.UIComponent;
import feature.hud.dialogs.DialogFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;

/** Verifies that every System Recovery story step uses its intended in-world speaker. */
class SystemRecoveryStoryDialogsTest {

  /** System instructions for riddles two and three are attributed to AXIOM. */
  @Test
  void attributesSystemInstructionsToAxiom() {
    assertEquals("axiom", SystemRecoveryStoryDialogs.MODULE_ARRAY.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.MODULE_VALUES.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.MODULE_ASSIGNMENT.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.GPU_FAULT.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.READ_MODULE_LENGTH.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.OPEN_SCANNER_DOOR.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.SCANNER_CODE.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.SCANNER_LEVER.speakerKey());
  }

  /** ECHO and the search robot keep their own distinct quest-log speaker labels. */
  @Test
  void preservesOtherStorySpeakers() {
    assertEquals("echo", SystemRecoveryStoryDialogs.MANUAL_SORTING.speakerKey());
    assertEquals("echo", SystemRecoveryStoryDialogs.CENTRAL_META.speakerKey());
    assertEquals("search-robot", SystemRecoveryStoryDialogs.SEARCH_ROBOT_START.speakerKey());
    assertEquals("search-robot", SystemRecoveryStoryDialogs.SEARCH_ROBOT_COMPLETE.speakerKey());
  }

  /** AXIOM's reaction waits until the player closes the search robot's completion dialog. */
  @Test
  void announcesSearchRobotBeforeAxiomWithoutStackingDialogs() {
    AtomicLong clock = new AtomicLong(1_000L);
    List<String> shownDialogs = new ArrayList<>();
    List<String> questLogDialogIds = new ArrayList<>();
    List<IVoidFunction> finishCallbacks = new ArrayList<>();

    try (MockedStatic<Game> game = mockStatic(Game.class);
        MockedStatic<SystemRecoveryQuestLogUtil> questLog =
            mockStatic(SystemRecoveryQuestLogUtil.class);
        MockedStatic<DialogFactory> dialogs = mockStatic(DialogFactory.class)) {
      game.when(Game::levelEntities).thenAnswer(_ -> Stream.empty());
      questLog
          .when(
              () ->
                  SystemRecoveryQuestLogUtil.addDialogEntry(anyString(), anyString(), anyString()))
          .thenAnswer(
              invocation -> {
                questLogDialogIds.add(invocation.getArgument(1));
                return null;
              });
      dialogs
          .when(() -> DialogFactory.showDialogDialog(anyString(), any(IVoidFunction.class), eq(42)))
          .thenAnswer(
              invocation -> {
                shownDialogs.add(invocation.getArgument(0));
                finishCallbacks.add(invocation.getArgument(1));
                return null;
              });

      SystemRecoveryStoryDialogs storyDialogs =
          new SystemRecoveryStoryDialogs(clock::get, _ -> true);
      storyDialogs.announceSequenceForPlayer(
          SystemRecoveryStoryDialogs.SEARCH_ROBOT_COMPLETE,
          SystemRecoveryStoryDialogs.ACCESS_MODULE_FOUND,
          42);
      clock.addAndGet(900L);
      storyDialogs.tick();

      assertEquals(
          List.of(SystemRecoveryStoryDialogs.SEARCH_ROBOT_COMPLETE.script()), shownDialogs);
      assertEquals(List.of("search-robot-complete"), questLogDialogIds);

      finishCallbacks.getFirst().execute();
      storyDialogs.tick();
      assertFalse(shownDialogs.contains(SystemRecoveryStoryDialogs.ACCESS_MODULE_FOUND.script()));

      clock.addAndGet(900L);
      storyDialogs.tick();

      assertEquals(
          List.of(
              SystemRecoveryStoryDialogs.SEARCH_ROBOT_COMPLETE.script(),
              SystemRecoveryStoryDialogs.ACCESS_MODULE_FOUND.script()),
          shownDialogs);
      assertEquals(List.of("search-robot-complete", "access-module-found"), questLogDialogIds);
      assertTrue(
          shownDialogs.indexOf(SystemRecoveryStoryDialogs.SEARCH_ROBOT_COMPLETE.script())
              < shownDialogs.indexOf(SystemRecoveryStoryDialogs.ACCESS_MODULE_FOUND.script()));
    }
  }

  /** Story calls remain available in editor mode, where only the intro is suppressed. */
  @Test
  void dispatchesStoryDialogInLevelEditorMode() {
    SystemRecovery.configureDebugMode("--leveleditor");
    AtomicLong clock = new AtomicLong(1_000L);
    String expectedScript = SystemRecoveryStoryDialogs.MODULE_ARRAY.script();

    try (MockedStatic<Game> game = mockStatic(Game.class);
        MockedStatic<SystemRecoveryQuestLogUtil> questLog =
            mockStatic(SystemRecoveryQuestLogUtil.class);
        MockedStatic<DialogFactory> dialogs = mockStatic(DialogFactory.class)) {
      game.when(() -> Game.levelEntities()).thenAnswer(_ -> Stream.empty());
      SystemRecoveryStoryDialogs storyDialogs =
          new SystemRecoveryStoryDialogs(clock::get, _ -> true);
      storyDialogs.announceForPlayer(SystemRecoveryStoryDialogs.MODULE_ARRAY, 42);

      storyDialogs.tick();
      dialogs.verifyNoInteractions();

      clock.addAndGet(900L);
      storyDialogs.tick();

      questLog.verify(
          () ->
              SystemRecoveryQuestLogUtil.addDialogEntry("riddle2", "module-array", expectedScript));
      dialogs.verify(
          () ->
              DialogFactory.showDialogDialog(eq(expectedScript), any(IVoidFunction.class), eq(42)));
    } finally {
      SystemRecovery.configureDebugMode();
    }
  }

  @Test
  void discardsAnInstructionAfterProgressChangesButStillRunsItsCompletionAction() {
    AtomicLong clock = new AtomicLong(1_000L);
    AtomicReference<SystemRecoveryLearningStep> active =
        new AtomicReference<>(SystemRecoveryLearningStep.ENERGY_VALUES);
    AtomicInteger completionActions = new AtomicInteger();

    try (MockedStatic<Game> game = mockStatic(Game.class);
        MockedStatic<SystemRecoveryQuestLogUtil> questLog =
            mockStatic(SystemRecoveryQuestLogUtil.class);
        MockedStatic<DialogFactory> dialogs = mockStatic(DialogFactory.class)) {
      SystemRecoveryStoryDialogs storyDialogs =
          new SystemRecoveryStoryDialogs(clock::get, step -> active.get() == step.validAt());
      storyDialogs.announceForPlayer(
          SystemRecoveryStoryDialogs.ENERGY_VALUES, 42, completionActions::incrementAndGet);

      active.set(SystemRecoveryLearningStep.ENERGY_INSERT_BATTERY);
      clock.addAndGet(900L);
      storyDialogs.tick();

      dialogs.verifyNoInteractions();
      questLog.verifyNoInteractions();
      assertEquals(1, completionActions.get());
    }
  }

  @Test
  void waitsForAnExistingDialogAndDeliversTheNextInstructionToTheSamePlayerLater() {
    AtomicLong clock = new AtomicLong(1_000L);
    AtomicBoolean blockingDialogOpen = new AtomicBoolean();
    List<String> shown = new ArrayList<>();
    Entity dialogEntity = mock(Entity.class);
    UIComponent blockingUi = mock(UIComponent.class);
    when(dialogEntity.fetch(UIComponent.class)).thenReturn(Optional.of(blockingUi));
    when(blockingUi.willPauseGame()).thenReturn(true);
    when(blockingUi.targetEntityIds()).thenReturn(new int[] {42});

    try (MockedStatic<Game> game = mockStatic(Game.class);
        MockedStatic<SystemRecoveryQuestLogUtil> questLog =
            mockStatic(SystemRecoveryQuestLogUtil.class);
        MockedStatic<DialogFactory> dialogs = mockStatic(DialogFactory.class)) {
      game.when(Game::levelEntities)
          .thenAnswer(_ -> blockingDialogOpen.get() ? Stream.of(dialogEntity) : Stream.empty());
      dialogs
          .when(() -> DialogFactory.showDialogDialog(anyString(), any(IVoidFunction.class), eq(42)))
          .thenAnswer(
              invocation -> {
                shown.add(invocation.getArgument(0));
                return null;
              });

      SystemRecoveryStoryDialogs storyDialogs =
          new SystemRecoveryStoryDialogs(clock::get, _ -> true);
      storyDialogs.announceForPlayer(SystemRecoveryStoryDialogs.STORAGE_UNLOCKED, 42);
      storyDialogs.announceForPlayer(SystemRecoveryStoryDialogs.STORAGE_ARRAY, 42);
      clock.addAndGet(900L);
      storyDialogs.tick();
      assertEquals(List.of(SystemRecoveryStoryDialogs.STORAGE_UNLOCKED.script()), shown);

      blockingDialogOpen.set(true);
      storyDialogs.tick();
      assertEquals(1, shown.size());

      blockingDialogOpen.set(false);
      storyDialogs.tick();
      assertEquals(
          List.of(
              SystemRecoveryStoryDialogs.STORAGE_UNLOCKED.script(),
              SystemRecoveryStoryDialogs.STORAGE_ARRAY.script()),
          shown);
    }
  }

  @Test
  void aBlockingDialogForOnePlayerDoesNotDelayAnotherPlayersStory() {
    AtomicLong clock = new AtomicLong(1_000L);
    List<Integer> shownTo = new ArrayList<>();
    Entity dialogEntity = mock(Entity.class);
    UIComponent blockingUi = mock(UIComponent.class);
    when(dialogEntity.fetch(UIComponent.class)).thenReturn(Optional.of(blockingUi));
    when(blockingUi.willPauseGame()).thenReturn(true);
    when(blockingUi.targetEntityIds()).thenReturn(new int[] {42});

    try (MockedStatic<Game> game = mockStatic(Game.class);
        MockedStatic<SystemRecoveryQuestLogUtil> questLog =
            mockStatic(SystemRecoveryQuestLogUtil.class);
        MockedStatic<DialogFactory> dialogs = mockStatic(DialogFactory.class)) {
      game.when(Game::levelEntities).thenAnswer(_ -> Stream.of(dialogEntity));
      dialogs
          .when(
              () ->
                  DialogFactory.showDialogDialog(
                      anyString(), any(IVoidFunction.class), any(int[].class)))
          .thenAnswer(
              invocation -> {
                shownTo.add(invocation.getArgument(2));
                return null;
              });

      SystemRecoveryStoryDialogs storyDialogs =
          new SystemRecoveryStoryDialogs(clock::get, _ -> true);
      storyDialogs.announceForPlayer(SystemRecoveryStoryDialogs.STORAGE_ARRAY, 42);
      storyDialogs.announceForPlayer(SystemRecoveryStoryDialogs.STORAGE_ARRAY, 43);
      clock.addAndGet(900L);
      storyDialogs.tick();

      assertEquals(List.of(43), shownTo);
    }
  }

  @Test
  void startsTheFinalCallOnlyAfterTheFirstPlayerClosesAxiomsResponse() {
    AtomicLong clock = new AtomicLong(1_000L);
    AtomicInteger calls = new AtomicInteger();
    List<IVoidFunction> closeCallbacks = new ArrayList<>();
    Entity first = mock(Entity.class);
    Entity second = mock(Entity.class);
    when(first.id()).thenReturn(42);
    when(second.id()).thenReturn(43);

    try (MockedStatic<Game> game = mockStatic(Game.class);
        MockedStatic<SystemRecoveryQuestLogUtil> questLog =
            mockStatic(SystemRecoveryQuestLogUtil.class);
        MockedStatic<DialogFactory> dialogs = mockStatic(DialogFactory.class)) {
      game.when(() -> Game.levelEntities(Set.of(PlayerComponent.class)))
          .thenAnswer(_ -> Stream.of(first, second));
      game.when(Game::levelEntities).thenAnswer(_ -> Stream.empty());
      dialogs
          .when(
              () ->
                  DialogFactory.showDialogDialog(
                      anyString(), any(IVoidFunction.class), any(int[].class)))
          .thenAnswer(
              invocation -> {
                closeCallbacks.add(invocation.getArgument(1));
                return null;
              });

      SystemRecoveryStoryDialogs storyDialogs =
          new SystemRecoveryStoryDialogs(clock::get, _ -> true);
      storyDialogs.announceCompletionToAllPlayers(calls::incrementAndGet);
      assertEquals(0, calls.get());
      clock.addAndGet(900L);
      storyDialogs.tick();
      assertEquals(0, calls.get());
      assertEquals(2, closeCallbacks.size());

      closeCallbacks.forEach(IVoidFunction::execute);
      assertEquals(1, calls.get());
    }
  }
}
