package contrib.editor.level.state;

import contrib.components.HealthComponent;
import contrib.editor.level.mode.LevelEditorMode;
import core.Entity;
import core.Game;
import core.components.InputComponent;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a session for the level editor in a game, allowing control over session activation,
 * player capture, and input handling for editor functionality.
 *
 * <p>This class provides methods to manage the lifecycle and behavior of the level editor session.
 *
 * <p>The session interacts with the game's player entity, allowing the editor to override player
 * inputs and toggle god mode.
 *
 * <p>The session operates in two states:
 * <ul>
 *   <li><strong>Active:</strong> The editor is running and has control over the player entity.
 *   <li><strong>Inactive:</strong> The editor is not running.
 * </ul>
 *
 * <p>The session can be temporarily stopped without being deactivated, which allows the mode to
 * change without executing while preserving the editor's state.
 */
public final class LevelEditorSession {

  private boolean active = false;
  private boolean stopped = false;

  private Entity capturedPlayer = null;
  private Map<Integer, InputComponent.InputData> capturedPlayerCallbacks = null;

  /**
   * Checks if the level editor session is currently active.
   *
   * @return {@code true} if the session is active, {@code false} otherwise
   */
  public boolean active() {
    return active;
  }

  /**
   * Changes the activation state of the level editor session.
   *
   * <p>When activated, this method captures the current player entity (if available) to allow the
   * editor to control it. When deactivated, the captured player is released and returned to its
   * normal state.
   *
   * @param active {@code true} to activate the session, {@code false} to deactivate the session
   * @return an {@link ActivationTransition} record containing information about the state change
   */
  public ActivationTransition changeActivation(boolean active) {
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

  /**
   * Stops the execution of the level editor session.
   *
   * <p>When stopped, the session will not execute its mode updates until {@link #run()} is called.
   * This is useful for pausing the editor without deactivating it, preserving the current state.
   */
  public void stop() {
    stopped = true;
  }

  /**
   * Resumes the execution of the level editor session after it has been stopped.
   *
   * <p>When running, the session will execute its mode updates normally.
   */
  public void run() {
    stopped = false;
  }

  /**
   * Determines whether the mode should be executed based on the current session state.
   *
   * <p>The mode will be executed if:
   * <ul>
   *   <li>The session is not stopped, OR
   *   <li>The mode has changed
   * </ul>
   *
   * @param modeChanged {@code true} if the mode has changed, {@code false} otherwise
   * @return {@code true} if the mode should be executed, {@code false} otherwise
   */
  public boolean shouldExecuteMode(boolean modeChanged) {
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

  /**
   * Represents the result of an activation state transition for the level editor session.
   *
   * @param changed {@code true} if the activation state actually changed, {@code false} if the
   *     state remained the same
   * @param hadCapturedPlayer {@code true} if the session had a captured player before the
   *     transition, {@code false} otherwise
   * @param hasCapturedPlayer {@code true} if the session has a captured player after the
   *     transition, {@code false} otherwise
   */
  public record ActivationTransition(
      boolean changed, boolean hadCapturedPlayer, boolean hasCapturedPlayer) {}
}
