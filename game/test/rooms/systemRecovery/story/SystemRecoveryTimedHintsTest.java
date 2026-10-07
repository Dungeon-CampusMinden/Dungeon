package rooms.systemRecovery.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.game.PreRunConfiguration;
import feature.components.UIComponent;
import feature.hints.Hint;
import feature.hints.HintSystem;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogType;
import feature.petrinet.PetriNetSystem;
import feature.questlog.QuestLogUtil;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import rooms.systemRecovery.modules.computer.SystemRecoveryDialogTypes;
import rooms.systemRecovery.petrinet.SystemRecoveryHintCatalog;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEvents;
import testingUtils.MockNetworkHandler;

/** Tests timed assistance against the real shared hint sequence and dialog ownership. */
class SystemRecoveryTimedHintsTest {

  private HintSystem hints;
  private MockedStatic<SystemRecoveryPuzzleEvents> tracking;

  @BeforeEach
  void setUp() {
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    MockNetworkHandler.useLocalNetworkHandler();
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
    tracking = mockStatic(SystemRecoveryPuzzleEvents.class);
    hints = new HintSystem();
    Game.add(hints);
    Game.add(new PetriNetSystem());
    SystemRecoveryProgressNet.initialize();
    SystemRecoveryQuestLogUtil.initializeQuestLog();
    addPlayer("Ada");
  }

  @AfterEach
  void tearDown() {
    tracking.close();
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    MockNetworkHandler.useLocalNetworkHandler();
  }

  @Test
  void automaticHintIsTheNextPhoneHintAndIsWrittenToTheMatchingQuestLog() {
    var first = hints.peekSharedHint().orElseThrow();
    assertTrue(SystemRecoveryHintPhone.deliverAutomaticHint());

    assertEquals(1, acceptedHintCount());
    var dialog = hintDialogs().getFirst();
    assertEquals(
        Game.localization()
            .getCurrentTranslator()
            .translate(SystemRecoveryText.echoCall("hint-delivery", first.text())),
        dialog.dialogContext().require(DialogContextKeys.DIALOG, String.class));
    var log = QuestLogUtil.getQuestLogComponent().orElseThrow().getEntries();
    assertEquals(
        first.text(), log.get(SystemRecoveryText.questKey("riddle1.tab")).getFirst().text());
    UIUtils.closeDialog(dialog);

    var next = hints.acceptSharedHint().orElseThrow();
    assertEquals(SystemRecoveryHintCatalog.hints(SystemRecoveryLearningStep.ENERGY_ARRAY)[1], next);
    assertEquals(2, acceptedHintCount());
  }

  @Test
  void automaticHintIsTrackedAsAutomaticInsteadOfRequested() {
    assertTrue(SystemRecoveryHintPhone.deliverAutomaticHint());

    tracking.verify(
        () ->
            SystemRecoveryPuzzleEvents.automaticHintUsed(
                eq(SystemRecoveryPuzzle.ENERGY), anyString(), any(Entity.class)));
    tracking.verify(
        () -> SystemRecoveryPuzzleEvents.hintUsed(any(), anyString(), any(Entity.class)), never());
  }

  @Test
  void multiplayerHintConsumesOneStageButEachPlayerHasAnIndependentDialog() {
    addPlayer("Grace");

    assertTrue(SystemRecoveryHintPhone.deliverAutomaticHint());

    List<UIComponent> dialogs = hintDialogs();
    assertEquals(1, acceptedHintCount());
    assertEquals(2, dialogs.size());
    assertEquals(1, dialogs.getFirst().targetEntityIds().length);
    assertEquals(1, dialogs.getLast().targetEntityIds().length);
    assertTrue(dialogs.getFirst().targetEntityIds()[0] != dialogs.getLast().targetEntityIds()[0]);
    dialogs.getFirst().callbacks().get(DialogContextKeys.ON_CONFIRM).accept(null);
    assertEquals(1, hintDialogs().size());
  }

  @Test
  void openComputerDoesNotBlockHelpOrDiscardItsComponent() {
    Entity computer = new Entity("computer");
    UIComponent ui =
        new UIComponent(
            DialogContext.builder().type(SystemRecoveryDialogTypes.COMPUTER).build(), true);
    computer.add(ui);
    Game.add(computer);

    assertTrue(SystemRecoveryHintPhone.deliverAutomaticHint());

    assertTrue(computer.isPresent(UIComponent.class));
    assertEquals(1, hintDialogs().size());
    assertEquals(1, acceptedHintCount());
  }

  @Test
  void currentStoryOrChoiceIsNotInterruptedAndHintRemainsAvailable() {
    Entity dialog = new Entity("story");
    dialog.add(
        new UIComponent(
            DialogContext.builder().type(DialogType.DefaultTypes.YES_NO).build(), true));
    Game.add(dialog);

    assertFalse(SystemRecoveryHintPhone.deliverAutomaticHint());
    assertEquals(0, acceptedHintCount());
    Game.remove(dialog);

    assertTrue(SystemRecoveryHintPhone.deliverAutomaticHint());
    assertEquals(1, acceptedHintCount());
  }

  @Test
  void exhaustedHintsOrNoPlayersDoNotConsumeAnything() {
    for (Hint ignored : SystemRecoveryHintCatalog.hints(SystemRecoveryLearningStep.ENERGY_ARRAY)) {
      assertTrue(hints.acceptSharedHint().isPresent());
    }
    assertFalse(SystemRecoveryHintPhone.deliverAutomaticHint());
    SystemRecoveryProgressNet.restoreActiveStep(SystemRecoveryLearningStep.MODULE_ARRAY);
    Game.allPlayers().toList().forEach(Game::remove);

    assertFalse(SystemRecoveryHintPhone.deliverAutomaticHint());
    assertEquals(
        0,
        hints
            .sharedProgress(
                Game.findEntityById(
                        SystemRecoveryProgressNet.hintEntityId(
                                SystemRecoveryLearningStep.MODULE_ARRAY)
                            .orElseThrow())
                    .orElseThrow())
            .acceptedCount());
  }

  private void addPlayer(String name) {
    Entity player = new Entity(name);
    player.add(new PlayerComponent(true, name));
    Game.add(player);
  }

  private int acceptedHintCount() {
    int id =
        SystemRecoveryProgressNet.hintEntityId(SystemRecoveryLearningStep.ENERGY_ARRAY)
            .orElseThrow();
    return hints.sharedProgress(Game.findEntityById(id).orElseThrow()).acceptedCount();
  }

  private List<UIComponent> hintDialogs() {
    return Game.entities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType() == DialogType.DefaultTypes.DIALOG_DIALOG)
        .toList();
  }
}
