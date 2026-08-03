package contrib.questlog;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for quest log utility entry creation. */
public class QuestLogUtilTest {

  @AfterEach
  void cleanupGameState() {
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  /** Verifies normal gameplay entries are not marked as user-created. */
  @Test
  void addCreatesGameEntry() {
    QuestLogComponent questLog =
        QuestLogUtil.initServerQuestLog().fetch(QuestLogComponent.class).orElseThrow();

    assertTrue(QuestLogUtil.add("Main", "Find the exit."));

    assertFalse(questLog.get("Main").get(0).userCreated());
  }

  /** Verifies submitted player notes are marked as user-created. */
  @Test
  void addPlayerNoteCreatesUserEntry() {
    QuestLogComponent questLog =
        QuestLogUtil.initServerQuestLog().fetch(QuestLogComponent.class).orElseThrow();
    Entity player = new Entity("hero");
    player.add(new PlayerComponent(true, "Ada"));

    assertTrue(QuestLogUtil.addPlayerNote(player, "Notes", "Remember the code.", false));

    assertTrue(questLog.get("Notes").get(0).userCreated());
  }
}
