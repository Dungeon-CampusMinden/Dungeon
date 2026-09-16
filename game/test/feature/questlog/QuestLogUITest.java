package feature.questlog;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.network.handler.INetworkHandler;
import engine.network.messages.NetworkMessage;
import engine.network.messages.c2s.InputMessage;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.systems.HudSystem;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/** Tests for quest log view filtering. */
public class QuestLogUITest {

  @BeforeEach
  void clearWorld() {
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  @AfterEach
  void cleanWorld() {
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  /** Verifies personal entries are visible only to their creator. */
  @Test
  void selectionForHidesPersonalEntriesFromOtherPlayers() {
    QuestLogComponent questLog = new QuestLogComponent();
    questLog.add("Notes", new QuestLogEntry("Public", 1, true, "Ada", false));
    questLog.add("Notes", new QuestLogEntry("Private", 2, true, "Ada", true));
    Entity ada = player("Ada");
    Entity bob = player("Bob");

    assertEquals(
        List.of("Public", "Private"),
        QuestLogUI.selectionFor(questLog, "Notes", ada).selectedEntries().stream()
            .map(QuestLogEntry::text)
            .toList());
    assertEquals(
        List.of("Public"),
        QuestLogUI.selectionFor(questLog, "Notes", bob).selectedEntries().stream()
            .map(QuestLogEntry::text)
            .toList());
  }

  /** Verifies personal entries are removed from the dialog data sent to other players. */
  @Test
  void dialogContextHidesPersonalEntriesFromOtherPlayers() {
    QuestLogComponent questLog = new QuestLogComponent();
    questLog.add("Notes", new QuestLogEntry("Public", 1, true, "Ada", false));
    questLog.add("Notes", new QuestLogEntry("Private", 2, true, "Ada", true));

    DialogContext context = createDialogContext(questLog, "Notes", player("Bob"));

    assertArrayEquals(
        new String[] {"Public"}, context.find("questlog.entryTexts", String[].class).orElseThrow());
  }

  /** A server response carries only the selected tab's entry payload, not the whole log. */
  @Test
  void dialogContextIncludesEntriesOnlyForSelectedTab() {
    QuestLogComponent questLog = new QuestLogComponent();
    questLog.add("Riddle 1", new QuestLogEntry("First task", 1, false, "AI", false));
    questLog.add("Riddle 2", new QuestLogEntry("Second task", 2, false, "AI", false));

    DialogContext context = createDialogContext(questLog, "Riddle 2", player("Bob"));

    assertArrayEquals(
        new String[] {"Riddle 2", "Riddle 1"},
        context.find("questlog.tabs", String[].class).orElseThrow());
    assertArrayEquals(
        new String[] {"Riddle 2"},
        context.find("questlog.entryTabs", String[].class).orElseThrow());
    assertArrayEquals(
        new String[] {"Second task"},
        context.find("questlog.entryTexts", String[].class).orElseThrow());
  }

  /** Quest log open and tab changes use custom requests that can be sent reliably over TCP. */
  @Test
  void requestMessageCarriesTheSelectedTabAsUtf8() {
    InputMessage request = QuestLogUI.createQuestLogRequest("Rätsel 2");
    InputMessage.Custom custom = request.payloadAs(InputMessage.Custom.class);

    assertEquals(InputMessage.Action.CUSTOM, request.action());
    assertEquals(QuestLogUI.COMMAND_SELECT_QUESTLOG_TAB, custom.commandId());
    assertEquals("Rätsel 2", new String(custom.payload(), StandardCharsets.UTF_8));
  }

  /** Tab requests use the reliable transport instead of the lossy gameplay-input channel. */
  @Test
  void requestMessageIsSentReliably() {
    INetworkHandler network = Mockito.mock(INetworkHandler.class);

    QuestLogUI.sendReliableRequest(network, "Riddle 2");

    Mockito.verify(network)
        .send(
            Mockito.eq((short) 0),
            Mockito.argThat(
                (NetworkMessage message) ->
                    message instanceof InputMessage input
                        && input
                            .payloadAs(InputMessage.Custom.class)
                            .commandId()
                            .equals(QuestLogUI.COMMAND_SELECT_QUESTLOG_TAB)),
            Mockito.eq(true));
  }

  /** An existing targeted dialog makes the server decline a second QuestLog overlay. */
  @Test
  void questLogRequestDoesNotStackOverAnAlreadyOpenDialog() {
    QuestLogUtil.initServerQuestLog();
    Entity requester = player("Requester");
    Game.add(requester);
    HudSystem hud = Mockito.mock(HudSystem.class);
    Mockito.when(hud.hasOpenUI(requester)).thenReturn(true);

    try (MockedStatic<HudSystem> hudSystem = Mockito.mockStatic(HudSystem.class);
        MockedStatic<DialogFactory> dialogs = Mockito.mockStatic(DialogFactory.class)) {
      hudSystem.when(HudSystem::getInstance).thenReturn(hud);

      assertFalse(QuestLogUI.showQuestLogForPlayers(requester.id()));

      dialogs.verify(
          () -> DialogFactory.show(Mockito.any(DialogContext.class), Mockito.any(int[].class)),
          never());
    }
  }

  /** Shared entries are sent in separate dialogs addressed to each requesting player. */
  @Test
  void simultaneousPlayerRequestsRemainIndividuallyTargeted() {
    QuestLogComponent questLog =
        QuestLogUtil.initServerQuestLog().fetch(QuestLogComponent.class).orElseThrow();
    questLog.add("Riddle 1", new QuestLogEntry("questlog.riddle1.entry", 1, false, "AI", false));
    Entity host = player("Host");
    Entity joiner = player("Joiner");
    Game.add(host);
    Game.add(joiner);
    HudSystem hud = Mockito.mock(HudSystem.class);
    Mockito.when(hud.hasOpenUI(host)).thenReturn(false);
    Mockito.when(hud.hasOpenUI(joiner)).thenReturn(false);
    feature.components.UIComponent ui = Mockito.mock(feature.components.UIComponent.class);
    List<Integer> targetedRecipients = new ArrayList<>();

    try (MockedStatic<HudSystem> hudSystem = Mockito.mockStatic(HudSystem.class);
        MockedStatic<DialogFactory> dialogs = Mockito.mockStatic(DialogFactory.class)) {
      hudSystem.when(HudSystem::getInstance).thenReturn(hud);
      dialogs
          .when(
              () -> DialogFactory.show(Mockito.any(DialogContext.class), Mockito.any(int[].class)))
          .thenAnswer(
              invocation -> {
                targetedRecipients.add(invocation.getArgument(1));
                return ui;
              });

      assertTrue(QuestLogUI.showQuestLogForPlayers(host.id()));
      assertTrue(QuestLogUI.showQuestLogForPlayers(joiner.id()));

      assertEquals(2, targetedRecipients.size());
      assertTrue(targetedRecipients.contains(host.id()));
      assertTrue(targetedRecipients.contains(joiner.id()));
    }
  }

  private static Entity player(String name) {
    Entity player = new Entity("hero_" + name);
    player.add(new PlayerComponent(true, name));
    return player;
  }

  private static DialogContext createDialogContext(
      QuestLogComponent questLog, String selectedTab, Entity viewer) {
    try {
      Method method =
          QuestLogUI.class.getDeclaredMethod(
              "createDialogContext", QuestLogComponent.class, String.class, Entity.class);
      method.setAccessible(true);
      return (DialogContext) method.invoke(null, questLog, selectedTab, viewer);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
      throw new AssertionError("Could not invoke QuestLogUI.createDialogContext", ex);
    }
  }
}
