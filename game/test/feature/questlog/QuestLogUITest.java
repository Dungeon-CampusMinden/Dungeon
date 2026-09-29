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
    questLog.add("Tasks", new QuestLogEntry("Current task", 0, false, "System", false));
    assertEquals("Notes", QuestLogUI.selectionFor(questLog, null).selectedTab().orElseThrow());
    questLog.overview("Tasks", questLog.get("Notes"));
    Entity ada = player("Ada");
    Entity bob = player("Bob");

    assertEquals("Tasks", QuestLogUI.selectionFor(questLog, null, bob).selectedTab().orElseThrow());
    assertEquals(List.of("Tasks", "Notes"), QuestLogUI.selectionFor(questLog, null, bob).tabs());

    assertEquals(
        List.of("Public", "Private"),
        QuestLogUI.selectionFor(questLog, "Notes", ada).selectedEntries().stream()
            .map(QuestLogEntry::text)
            .toList());
    questLog.overview("Missing", List.of());
    assertEquals("Notes", QuestLogUI.selectionFor(questLog, null, bob).selectedTab().orElseThrow());
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
    questLog.add("Tasks", new QuestLogEntry("Current task", 0, false, "System", false));
    questLog.overview(
        "Tasks",
        List.of(
            questLog.get("Notes").get(1),
            questLog.get("Notes").getFirst(),
            new QuestLogEntry("Missing entry", 3, false, "System", false)));

    DialogContext context = createDialogContext(questLog, "Notes", player("Bob"));

    assertArrayEquals(
        new String[] {"Current task", "Public"},
        context.find("questlog.entryTexts", String[].class).orElseThrow());
    assertEquals("Notes", context.find("questlog.selectedTab", String.class).orElseThrow());
    assertEquals("Tasks", context.find("questlog.overviewTab", String.class).orElseThrow());
    assertArrayEquals(
        new int[] {1}, context.find("questlog.overviewReferences", int[].class).orElseThrow());

    DialogContext ownerContext = createDialogContext(questLog, null, player("Ada"));
    assertEquals("Tasks", ownerContext.find("questlog.selectedTab", String.class).orElseThrow());
    assertArrayEquals(
        new int[] {2, 1},
        ownerContext.find("questlog.overviewReferences", int[].class).orElseThrow());
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
