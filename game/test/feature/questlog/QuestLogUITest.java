package feature.questlog;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Entity;
import engine.components.PlayerComponent;
import feature.hud.dialogs.DialogContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests for quest log view filtering. */
public class QuestLogUITest {

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
