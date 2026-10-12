package engine.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GameStarterMenuConfigurationTest {

  @Test
  void defaultsPreserveExistingHostAndJoinMenu() {
    GameStarter starter = GameStarter.builder("Example", Object.class).build();

    assertTrue(starter.showJoinButton());
    assertTrue(starter.hostActionLabel().isEmpty());
    assertTrue(starter.creditsRoomId().isEmpty());
  }

  @Test
  void creditsRoomIdIsOptionalAndConfiguredByRoom() {
    GameStarter starter =
        GameStarter.builder("Example", Object.class).creditsRoomId("sample-room").build();

    assertEquals("sample-room", starter.creditsRoomId().orElseThrow());
  }

  @Test
  void creditsRoomIdCannotEscapeTheInternalAssetDirectory() {
    assertThrows(
        IllegalArgumentException.class,
        () -> GameStarter.builder("Example", Object.class).creditsRoomId("../outside"));
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
