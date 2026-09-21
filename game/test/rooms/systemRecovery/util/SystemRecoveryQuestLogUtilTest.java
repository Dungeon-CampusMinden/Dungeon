package rooms.systemRecovery.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Game;
import feature.hints.Hint;
import feature.questlog.QuestLogComponent;
import feature.questlog.QuestLogUtil;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.util.interpreter.TerminalStep;

/** Tests the localized story and hint entries written to the System Recovery quest log. */
class SystemRecoveryQuestLogUtilTest {

  @AfterEach
  void cleanupGameState() {
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  @Test
  void firstRiddleDialogEntriesContainTheOriginalSpeakersAndCompleteDialogs() {
    SystemRecoveryQuestLogUtil.initializeQuestLog();

    SystemRecoveryQuestLogUtil.addDialogEntry("riddle1", "opening-call", "echo", "opening-call");
    SystemRecoveryQuestLogUtil.addDialogEntry("riddle1", "energy-values", "axiom", "energy-values");
    SystemRecoveryQuestLogUtil.addDialogEntry(
        "riddle1", "energy-battery", "axiom", "energy-battery");

    assertEquals(
        List.of(
            "systemRecovery.story.echo\n" + "systemRecovery.story.opening-call",
            "systemRecovery.story.axiom\n" + "systemRecovery.story.energy-values",
            "systemRecovery.story.axiom\n" + "systemRecovery.story.energy-battery"),
        entriesFor("riddle1"));
  }

  @Test
  void acceptedHintEntryContainsOnlyTheHintText() {
    SystemRecoveryQuestLogUtil.initializeQuestLog();

    SystemRecoveryQuestLogUtil.addHintEntry(
        "riddle1",
        new Hint(
            "systemRecovery.hints.orientation-title",
            "systemRecovery.hints.steps.energy-array.orientation"));

    assertEquals(
        List.of("systemRecovery.hints.steps.energy-array.orientation"), entriesFor("riddle1"));
  }

  @Test
  void secondRiddleDialogEntriesUseTheCompleteStoryMessages() {
    SystemRecoveryQuestLogUtil.initializeQuestLog();

    SystemRecoveryQuestLogUtil.addDialogEntry("riddle2", "module-array", "axiom", "module-array");
    SystemRecoveryQuestLogUtil.addDialogEntry(
        "riddle2", "module-values", "axiom", "module-values");
    SystemRecoveryQuestLogUtil.addDialogEntry(
        "riddle2", "module-assignment", "axiom", "module-assignment");
    SystemRecoveryQuestLogUtil.addDialogEntry("riddle2", "gpu-fault", "axiom", "gpu-fault");
    SystemRecoveryQuestLogUtil.addDialogEntry(
        "riddle2", "module-length", "axiom", "module-length");
    SystemRecoveryQuestLogUtil.addDialogEntry(
        "riddle2", "open-scanner-door", "axiom", "open-scanner-door");

    assertEquals(
        List.of(
            "systemRecovery.story.axiom\nsystemRecovery.story.module-array",
            "systemRecovery.story.axiom\nsystemRecovery.story.module-values",
            "systemRecovery.story.axiom\nsystemRecovery.story.module-assignment",
            "systemRecovery.story.axiom\nsystemRecovery.story.gpu-fault",
            "systemRecovery.story.axiom\nsystemRecovery.story.module-length",
            "systemRecovery.story.axiom\nsystemRecovery.story.open-scanner-door"),
        entriesFor("riddle2"));
  }

  @Test
  void acceptedSolutionIsStoredVerbatimInTheMatchingRiddleTab() {
    SystemRecoveryQuestLogUtil.initializeQuestLog();
    String source = "int[] eigeneEnergie = new int[5];";

    SystemRecoveryQuestLogUtil.addTerminalSolutionEntry(
        new TerminalAttempt(TerminalStep.ENERGY_ARRAY.stateId(), source, 7));

    assertEquals(
        List.of(SystemRecoveryText.questKey("solution", source)), entriesFor("riddle1"));
  }

  @Test
  void identicalAcceptedSolutionIsNotDuplicated() {
    SystemRecoveryQuestLogUtil.initializeQuestLog();
    String source = "String[] meineModule = new String[5];";
    TerminalAttempt attempt =
        new TerminalAttempt(TerminalStep.MODULE_ARRAY.stateId(), source, 7);

    SystemRecoveryQuestLogUtil.addTerminalSolutionEntry(attempt);
    SystemRecoveryQuestLogUtil.addTerminalSolutionEntry(attempt);

    assertEquals(1, entriesFor("riddle2").size());
  }

  private static List<String> entriesFor(String riddleKey) {
    QuestLogComponent questLog = QuestLogUtil.getQuestLogComponent().orElseThrow();
    return questLog.get(SystemRecoveryText.questKey(riddleKey + ".tab")).stream()
        .map(entry -> entry.text())
        .toList();
  }
}
