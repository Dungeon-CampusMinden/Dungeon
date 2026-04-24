package contrib.editor.level;

import contrib.components.HealthComponent;
import contrib.editor.level.mode.LevelEditorMode;
import core.Entity;
import core.Game;
import core.components.InputComponent;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a session for the level editor in a game, allowing control over
 * session activation, player capture, and input handling for editor functionality.
 *
 * <p>This class provides methods to manage the lifecycle and behavior of the level editor session.
 *
 * <p>The session interacts with the game's player entity, allowing the editor to
 * override player inputs and toggle god mode.
 */
final class LevelEditorSession {

  private boolean active = false;
  private boolean stopped = false;

  private Entity capturedPlayer = null;
  private Map<Integer, InputComponent.InputData> capturedPlayerCallbacks = null;

  boolean active() {
    return active;
  }

  ActivationTransition changeActivation(boolean active) {
    boolean hadCapturedPlayer = hasCapturedPlayer();
    if (this.active == active) {
      return new ActivationTransition(false, hadCapturedPlayer, hadCapturedPlayer);
    }

    this.active = active;

    if (active) {
      Game.player().ifPresent(this::capturePlayer);
    } else {
      releaseCapturedPlayer();
    }

    return new ActivationTransition(true, hadCapturedPlayer, hasCapturedPlayer());
  }

  void stop() {
    stopped = true;
  }

  void run() {
    stopped = false;
  }

  boolean shouldExecuteMode(boolean modeChanged) {
    return !stopped || modeChanged;
  }

  private boolean hasCapturedPlayer() {
    return capturedPlayer != null;
  }

  private void capturePlayer(Entity player) {
    capturedPlayer = player;

    player.fetch(InputComponent.class).ifPresent(this::capturePlayerEditorInputs);
    player.fetch(HealthComponent.class).ifPresent(healthComponent -> healthComponent.godMode(true));
  }

  private void releaseCapturedPlayer() {
    if (capturedPlayer == null) {
      return;
    }

    if (capturedPlayerCallbacks != null) {
      capturedPlayer.fetch(InputComponent.class).ifPresent(this::restorePlayerEditorInputs);
      capturedPlayerCallbacks = null;
    }

    capturedPlayer
      .fetch(HealthComponent.class)
      .ifPresent(healthComponent -> healthComponent.godMode(false));

    capturedPlayer = null;
  }

  private void capturePlayerEditorInputs(InputComponent inputComponent) {
    Map<Integer, InputComponent.InputData> callbacks = inputComponent.callbacks();
    capturedPlayerCallbacks = new HashMap<>();

    LevelEditorMode.editorInputs()
      .forEach(
        input -> {
          InputComponent.InputData callback = callbacks.get(input);
          if (callback != null) {
            capturedPlayerCallbacks.put(input, callback);
          }

          inputComponent.removeCallback(input);
        });
  }

  private void restorePlayerEditorInputs(InputComponent inputComponent) {
    capturedPlayerCallbacks.forEach(
      (key, value) ->
        inputComponent.registerCallback(
          key, value.callback(), value.repeat(), value.pauseable()));
  }

  record ActivationTransition(boolean changed, boolean hadCapturedPlayer, boolean hasCapturedPlayer) {}
}
