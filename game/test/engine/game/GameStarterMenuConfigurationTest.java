package engine.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameStarterMenuConfigurationTest {

  @Test
  void defaultsPreserveExistingHostAndJoinMenu() {
    GameStarter starter = GameStarter.builder("Example", Object.class).build();

    assertTrue(starter.showJoinButton());
    assertTrue(starter.hostActionLabel().isEmpty());
  }

  @Test
  void projectCanOverrideHostLabelAndHideJoinWithoutChangingDefaults() {
    GameStarter starter =
        GameStarter.builder("Example", Object.class)
            .hostActionLabel(() -> "Start Game")
            .showJoinButton(false)
            .build();

    assertEquals("Start Game", starter.hostActionLabel().orElseThrow());
    assertFalse(starter.showJoinButton());
  }

  @Test
  void editorHookRunsOnlyWhenTheMenuRequestsIt() {
    boolean[] started = {false};
    GameStarter starter =
        GameStarter.builder("Example", Object.class)
            .beforeLevelEditorStart(() -> started[0] = true)
            .build();

    assertFalse(started[0]);
    starter.beforeLevelEditorStart();
    assertTrue(started[0]);
  }
}
