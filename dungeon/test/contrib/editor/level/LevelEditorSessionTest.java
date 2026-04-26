package contrib.editor.level;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.components.HealthComponent;
import contrib.editor.level.mode.LevelEditorMode;
import core.Entity;
import core.Game;
import core.components.InputComponent;
import core.components.PlayerComponent;
import core.input.Keys;
import core.level.elements.ILevel;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for {@link LevelEditorSession}. */
public class LevelEditorSessionTest {

  @AfterEach
  void cleanup() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.currentLevel((ILevel) null);
  }

  /** Ensures activation captures editor inputs and deactivation restores them. */
  @Test
  void activationCapturesAndRestoresPlayerEditorState() {
    LevelEditorSession session = new LevelEditorSession();
    Entity player = new Entity();
    InputComponent inputComponent = new InputComponent();
    HealthComponent healthComponent = new HealthComponent();
    Consumer<Entity> editorCallback = _ -> {};
    Consumer<Entity> movementCallback = _ -> {};

    inputComponent.registerCallback(LevelEditorMode.PRIMARY_UP, editorCallback, false, true);
    inputComponent.registerCallback(Keys.W, movementCallback, true, false);
    player.add(new PlayerComponent());
    player.add(inputComponent);
    player.add(healthComponent);
    Game.add(player);

    LevelEditorSession.ActivationTransition activated = session.changeActivation(true);

    assertTrue(session.active());
    assertTrue(activated.changed());
    assertFalse(activated.hadCapturedPlayer());
    assertTrue(activated.hasCapturedPlayer());
    assertFalse(inputComponent.callbacks().containsKey(LevelEditorMode.PRIMARY_UP));
    assertTrue(inputComponent.callbacks().containsKey(Keys.W));
    assertTrue(healthComponent.godMode());

    LevelEditorSession.ActivationTransition deactivated = session.changeActivation(false);

    Map<Integer, InputComponent.InputData> callbacks = inputComponent.callbacks();
    assertFalse(session.active());
    assertTrue(deactivated.changed());
    assertTrue(deactivated.hadCapturedPlayer());
    assertFalse(deactivated.hasCapturedPlayer());
    assertTrue(callbacks.containsKey(LevelEditorMode.PRIMARY_UP));
    assertSame(editorCallback, callbacks.get(LevelEditorMode.PRIMARY_UP).callback());
    assertTrue(callbacks.get(LevelEditorMode.PRIMARY_UP).pauseable());
    assertTrue(callbacks.containsKey(Keys.W));
    assertSame(movementCallback, callbacks.get(Keys.W).callback());
    assertFalse(healthComponent.godMode());
  }

  /** Ensures captured player state is restored even when no current game player is registered. */
  @Test
  void deactivationUsesCapturedPlayerReference() {
    LevelEditorSession session = new LevelEditorSession();
    Entity player = new Entity();
    InputComponent inputComponent = new InputComponent();
    HealthComponent healthComponent = new HealthComponent();
    Consumer<Entity> editorCallback = _ -> {};

    inputComponent.registerCallback(LevelEditorMode.PRIMARY_DOWN, editorCallback, true, false);
    player.add(new PlayerComponent());
    player.add(inputComponent);
    player.add(healthComponent);
    Game.add(player);

    session.changeActivation(true);
    Game.remove(player);

    LevelEditorSession.ActivationTransition deactivated = session.changeActivation(false);

    assertTrue(deactivated.hadCapturedPlayer());
    assertFalse(deactivated.hasCapturedPlayer());
    assertTrue(inputComponent.callbacks().containsKey(LevelEditorMode.PRIMARY_DOWN));
    assertSame(
        editorCallback, inputComponent.callbacks().get(LevelEditorMode.PRIMARY_DOWN).callback());
    assertFalse(healthComponent.godMode());
  }

  /** Ensures stop and run control whether the active mode should execute. */
  @Test
  void stopAndRunGateModeExecution() {
    LevelEditorSession session = new LevelEditorSession();

    assertTrue(session.shouldExecuteMode(false));

    session.stop();

    assertFalse(session.shouldExecuteMode(false));
    assertTrue(session.shouldExecuteMode(true));

    session.run();

    assertTrue(session.shouldExecuteMode(false));
  }
}
