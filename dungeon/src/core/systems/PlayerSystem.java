package core.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import core.Entity;
import core.System;
import core.components.PlayerComponent;
import core.utils.components.MissingComponentException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controls the Player.
 *
 * <p>Will work on Entities that implement the {@link PlayerComponent}.
 *
 * <p>This System will check for each registered callback in the {@link PlayerComponent} if the Key
 * is pressed, and if so, will execute the Callback.
 */
public final class PlayerSystem extends System {

  private boolean running = true;
  private List<Integer> pressed = new ArrayList();
  private List<Integer> lastPressed = new ArrayList();

  /** WTF? . */
  public PlayerSystem() {
    super(PlayerComponent.class);
  }

  @Override
  public void execute() {
    filteredEntityStream(PlayerComponent.class).forEach(this::execute);
  }

  private void execute(final Entity entity) {
    PlayerComponent pc =
        entity
            .fetch(PlayerComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PlayerComponent.class));

    checkPressedKeys();
    executeOnRelease(pc.releaseCallbacks(), entity, !this.running);
    execute(pc.callbacks(), entity, !this.running);
    lastPressed = new ArrayList<>(pressed);
  }

  private void checkPressedKeys() {
    pressed.clear();
    // Check keys (loop through all possible key codes)
    for (int keycode = 0; keycode < Input.Keys.MAX_KEYCODE; keycode++) {
      if (Gdx.input.isKeyPressed(keycode)) {
        pressed.add(keycode);
      }
    }

    // Check mouse buttons
    for (int button = Input.Buttons.LEFT; button <= Input.Buttons.BACK; button++) {
      if (Gdx.input.isButtonPressed(button)) {
        pressed.add(button);
      }
    }
  }

  @Override
  public void stop() {
    this.run = true; // This system can not be stopped.
    this.running = false;
  }

  @Override
  public void run() {
    this.run = true;
    this.running = true;
  }

  /**
   * Execute the callback function registered to a key when it is pressed.
   *
   * <p>The callbacks are executed only if the game is not paused or if the callback is not
   * pauseable.
   *
   * @param callbacks WTF? .
   * @param entity associated entity of this component.
   * @param paused if the game is paused or not.
   */
  private void execute(
      final Map<Integer, PlayerComponent.InputData> callbacks,
      final Entity entity,
      boolean paused) {
    callbacks.forEach(
        (key, value) -> {
          if (!paused || value.pauseable()) {
            execute(entity, key, value);
          }
        });
  }

  private void execute(final Entity entity, int key, final PlayerComponent.InputData data) {
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

  private void executeOnRelease(
      final Map<Integer, PlayerComponent.InputData> callbacks,
      final Entity entity,
      boolean paused) {
    callbacks.forEach(
        (key, value) -> {
          if (!paused || value.pauseable()) {
            executeOnRelease(entity, key, value);
          }
        });
  }

  private void executeOnRelease(
      final Entity entity, int key, final PlayerComponent.InputData data) {
    if ((lastPressed.contains(key) && !pressed.contains(key))) {
      java.lang.System.out.println(key + " was released");
      data.callback().accept(entity);
    }
  }
}
