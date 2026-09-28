package rooms.systemRecovery.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

import engine.Game;
import engine.utils.IVoidFunction;
import feature.hud.dialogs.DialogFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import rooms.systemRecovery.SystemRecovery;
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
                  SystemRecoveryQuestLogUtil.addDialogEntry(
                      anyString(), anyString(), anyString()))
          .thenAnswer(
              invocation -> {
                questLogDialogIds.add(invocation.getArgument(1));
                return null;
              });
      dialogs
          .when(
              () ->
                  DialogFactory.showDialogDialog(
                      anyString(), any(IVoidFunction.class), eq(42)))
          .thenAnswer(
              invocation -> {
                shownDialogs.add(invocation.getArgument(0));
                finishCallbacks.add(invocation.getArgument(1));
                return null;
              });

      SystemRecoveryStoryDialogs storyDialogs = new SystemRecoveryStoryDialogs(clock::get);
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
      assertEquals(
          List.of("search-robot-complete", "access-module-found"), questLogDialogIds);
      assertTrue(shownDialogs.indexOf(SystemRecoveryStoryDialogs.SEARCH_ROBOT_COMPLETE.script())
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
      SystemRecoveryStoryDialogs storyDialogs = new SystemRecoveryStoryDialogs(clock::get);
      storyDialogs.announceForPlayer(SystemRecoveryStoryDialogs.MODULE_ARRAY, 42);

      storyDialogs.tick();
      dialogs.verifyNoInteractions();

      clock.addAndGet(900L);
      storyDialogs.tick();

      questLog.verify(
          () ->
              SystemRecoveryQuestLogUtil.addDialogEntry(
                  "riddle2", "module-array", expectedScript));
      dialogs.verify(
          () -> DialogFactory.showDialogDialog(eq(expectedScript), any(IVoidFunction.class), eq(42)));
    } finally {
      SystemRecovery.configureDebugMode();
    }
  }
}
