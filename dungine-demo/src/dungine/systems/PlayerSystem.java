package dungine.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.input.Keyboard;
import de.fwatermann.dungine.input.Mouse;
import dungine.components.PlayerComponent;
import dungine.exception.MissingComponentException;
import java.util.Map;
import org.lwjgl.glfw.GLFW;

/**
 * The `PlayerSystem` is a System that executes the callback functions registered to keys when they
 * are pressed.
 */
public class PlayerSystem extends System<PlayerSystem> {

  /** Create a new `PlayerSystem` instance. */
  public PlayerSystem() {
    super(1, true);
  }

  @Override
  public void update(ECS ecs) {
    ecs.forEachEntity(this::execute, PlayerComponent.class);
  }

  private void execute(final Entity entity) {
    PlayerComponent pc =
        entity
            .component(PlayerComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PlayerComponent.class));
    this.execute(pc.callbacks(), entity);
  }

  /**
   * Execute the callback function registered to a key when it is pressed.
   *
   * <p>The callbacks are executed only if the game is not paused or if the callback is not
   * pauseable.
   *
   * @param callbacks WTF? .
   * @param entity associated entity of this component.
   */
  private void execute(
      final Map<Integer, PlayerComponent.InputData> callbacks, final Entity entity) {
    callbacks.forEach(
        (key, value) -> {
          this.execute(entity, key, value);
        });
  }

  private void execute(final Entity entity, int key, final PlayerComponent.InputData data) {
    boolean isMouseButton =
        key == GLFW.GLFW_MOUSE_BUTTON_LEFT
            || key == GLFW.GLFW_MOUSE_BUTTON_RIGHT
            || key == GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
    boolean isPressed = isMouseButton ? Mouse.buttonPressed(key) : Keyboard.keyPressed(key);
    boolean isJustPressed =
        isMouseButton ? Mouse.buttonJustPressed(key) : Keyboard.keyJustPressed(key);

    if ((isJustPressed && !data.repeat()) || (isPressed && data.repeat())) {
      data.callback().run(entity);
    }
  }
}
