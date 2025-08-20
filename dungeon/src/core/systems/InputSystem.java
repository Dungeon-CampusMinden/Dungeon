package core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import core.Entity;
import core.System;
import core.components.InputComponent;
import core.utils.components.MissingComponentException;
import java.util.Map;

/**
 * Processes input events for the player.
 *
 * <p>Will work on Entities that implement the {@link InputComponent}.
 *
 * <p>This System will check for each registered callback in the {@link InputComponent} if the Key
 * is pressed, and if so, will execute the Callback.
 */
public final class InputSystem extends System {

  private boolean paused = true;

  /** Creates a new InputSystem. */
  public InputSystem() {
    super(InputComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream(InputComponent.class).forEach(this::execute);
  }

  private void execute(final Entity entity) {
    InputComponent pc =
        entity
            .fetch(InputComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, InputComponent.class));
    if (pc.deactivateControls()) return;
    execute(pc.callbacks(), entity, !this.paused);
  }

  /** This method only marks the game as paused, it does not stop the system. */
  @Override
  public void stop() {
    this.run = true; // This system can not be stopped.
    this.paused = false;
  }

  /**
   * This method marks the game as running, but does not run the system, because the system cannot
   * be stopped.
   */
  @Override
  public void run() {
    this.run = true;
    this.paused = true;
  }

  /**
   * Execute the callback function registered to a key when it is pressed.
   *
   * <p>The callbacks are executed only if the game is not paused or if the callback is not
   * pauseable.
   *
   * @param callbacks the map of key callbacks to execute.
   * @param entity associated entity of this component.
   * @param paused if the game is paused or not.
   */
  private void execute(
      final Map<Integer, InputComponent.InputData> callbacks, final Entity entity, boolean paused) {
    callbacks.forEach(
        (key, value) -> {
          if (!paused || value.pauseable()) {
            execute(entity, key, value);
          }
        });
  }

  private void execute(final Entity entity, int key, final InputComponent.InputData data) {
    boolean isMouseButton =
        key == Input.Buttons.LEFT || key == Input.Buttons.RIGHT || key == Input.Buttons.MIDDLE;
    boolean isPressed =
        isMouseButton ? Gdx.input.isButtonPressed(key) : Gdx.input.isKeyPressed(key);
    boolean isJustPressed =
        isMouseButton ? Gdx.input.isButtonJustPressed(key) : Gdx.input.isKeyJustPressed(key);

    if ((isJustPressed && !data.repeat()) || (isPressed && data.repeat())) {
      data.callback().accept(entity);
    }
  }
}
