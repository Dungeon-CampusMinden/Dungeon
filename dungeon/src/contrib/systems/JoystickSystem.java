package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.Controllers;
import contrib.configuration.JoystickConfig;
import contrib.entities.HeroController;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.System;
import core.components.InputComponent;
import core.components.PlayerComponent;
import core.network.messages.c2s.InputMessage;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * System responsible for handling joystick/controller input on the client side.
 *
 * <p>This system reads input from a connected game controller and translates it into game actions
 * such as movement, skill casting, interaction, inventory toggling, and mouse cursor control.
 *
 * <p>Main responsibilities:
 *
 * <ul>
 *   <li>Read left stick and D-Pad input to generate movement commands.
 *   <li>Read right stick input to control a virtual mouse cursor.
 *   <li>Map controller buttons and triggers to gameplay actions.
 *   <li>Simulate mouse events (touchDown, touchDragged, touchUp) using the left trigger.
 *   <li>Send translated input actions to the game network layer.
 * </ul>
 *
 * <p>The system is always active and cannot be stopped, even when the game is paused by UI dialogs,
 * ensuring consistent controller behavior.
 *
 * <p>Only local player entities ({@link PlayerComponent#isLocal()}) are affected by this system.
 *
 * <p>Dead zones are applied to analog inputs to prevent unintended movements.
 */
public class JoystickSystem extends System {

  private boolean rtAxisButtonPressed = false;
  private boolean ltMouseDown = false;

  private Controller activeController;

  private final Set<Integer> pressedButtons = new HashSet<>();

  private float cursorX = -1f;
  private float cursorY = -1f;

  /**
   * Creates the joystick system and binds it to the first available controller.
   *
   * <p>If no controller is available at construction time, the system will attempt to acquire one
   * lazily during execution.
   */
  public JoystickSystem() {
    super(AuthoritativeSide.CLIENT, PlayerComponent.class, InputComponent.class);
    var controllers = Controllers.getControllers();
    this.activeController = controllers.size == 0 ? null : controllers.first();
  }

  /** Keep joystick input active even when the game is paused by UI dialogs. */
  @Override
  public void stop() {
    this.run = true; // This system can not be stopped.
  }

  /** This system is always running, even when the game is unpaused. */
  @Override
  public void run() {
    this.run = true;
  }

  @Override
  public void execute() {
    filteredEntityStream()
        .filter(
            entity ->
                entity.fetch(PlayerComponent.class).map(PlayerComponent::isLocal).orElse(false))
        .forEach(this::executeJoystick);
  }

  private void executeJoystick(Entity player) {

    boolean controlsDisabled = isControlsDisabled(player);
    getActiveController()
        .ifPresent(
            controller -> {
              updateMouseCursor(controller);
              handleMouse(controller);
              if (!controlsDisabled) {
                handleMovement(controller);
                handleActions(controller, SkillTools.cursorPositionAsPoint());
              }
              handleInventoryToggle(controller, player);
            });
  }

  /**
   * Returns the currently active controller, lazily resolving the first connected controller if
   * none is cached yet.
   *
   * @return the active controller, or null if no controller is connected
   */
  private Optional<Controller> getActiveController() {
    if (activeController == null) {
      var controllers = Controllers.getControllers();
      activeController = controllers.size == 0 ? null : controllers.first();
    }
    return Optional.ofNullable(activeController);
  }

  private boolean isControlsDisabled(Entity player) {
    InputComponent inputComponent = player.fetch(InputComponent.class).orElse(null);
    return inputComponent != null && inputComponent.deactivateControls();
  }

  private void handleMovement(Controller controller) {
    Vector2 moveLeftVector = readLeftStick(controller);
    if (moveLeftVector.isZero()) {
      moveLeftVector = readDpad(controller);
    }
    if (moveLeftVector.isZero()) {
      return;
    }
    if (moveLeftVector.y() > 0) {
      Game.network().sendInput(new InputMessage(InputMessage.Action.MOVE, Direction.UP));
    } else if (moveLeftVector.y() < 0) {
      Game.network().sendInput(new InputMessage(InputMessage.Action.MOVE, Direction.DOWN));
    }
    if (moveLeftVector.x() > 0) {
      Game.network().sendInput(new InputMessage(InputMessage.Action.MOVE, Direction.RIGHT));
    } else if (moveLeftVector.x() < 0) {
      Game.network().sendInput(new InputMessage(InputMessage.Action.MOVE, Direction.LEFT));
    }
  }

  private void handleActions(Controller controller, Point target) {
    if (isRtTriggerJustPressed(controller)) {
      Game.network().sendInput(new InputMessage(InputMessage.Action.CAST_SKILL, target));
    }
    if (isJustPressed(controller, JoystickConfig.BUTTON_INTERACT)) {
      Game.network().sendInput(new InputMessage(InputMessage.Action.INTERACT, target));
    }
    if (isJustPressed(controller, JoystickConfig.BUTTON_NEXT_SKILL)) {
      Game.network().sendInput(new InputMessage(InputMessage.Action.NEXT_SKILL, Vector2.ZERO));
    }
    if (isJustPressed(controller, JoystickConfig.BUTTON_PREV_SKILL)) {
      Game.network().sendInput(new InputMessage(InputMessage.Action.PREV_SKILL, Vector2.ZERO));
    }
  }

  private void handleInventoryToggle(Controller controller, Entity player) {
    if (isJustPressed(controller, JoystickConfig.BUTTON_INVENTAR)) {
      HeroController.toggleInventory(player);
    }
  }

  // buttons
  private boolean isJustPressed(Controller controller, int button) {
    boolean isPressed = controller.getButton(button);
    if (isPressed) {
      if (!pressedButtons.contains(button)) {
        pressedButtons.add(button);
        return true;
      }
    } else {
      pressedButtons.remove(button);
    }
    return false;
  }

  // give me back something like sin/cos coordinates( between -1 - 1 for both axes)
  private Vector2 readDpad(Controller controller) {
    int x = 0;
    int y = 0;
    if (controller.getButton(JoystickConfig.BUTTON_DPAD_UP)) y += 1;
    if (controller.getButton(JoystickConfig.BUTTON_DPAD_DOWN)) y -= 1;
    if (controller.getButton(JoystickConfig.BUTTON_DPAD_RIGHT)) x += 1;
    if (controller.getButton(JoystickConfig.BUTTON_DPAD_LEFT)) x -= 1;
    if (x == 0 && y == 0) {
      return Vector2.ZERO;
    }
    return Vector2.of(x, y);
  }

  private Vector2 readLeftStick(Controller controller) {
    float x = controller.getAxis(JoystickConfig.AXIS_LEFT_X);
    float y = controller.getAxis(JoystickConfig.AXIS_LEFT_Y);

    if (Math.abs(x) < JoystickConfig.STICK_DEADZONE) x = 0f;
    if (Math.abs(y) < JoystickConfig.STICK_DEADZONE) y = 0f;

    if (x == 0f && y == 0f) {
      return Vector2.ZERO;
    }
    return Vector2.of(x, -y);
  }

  private Vector2 readRightStick(Controller controller) {

    float x = controller.getAxis(JoystickConfig.AXIS_RIGHT_X);
    float y = controller.getAxis(JoystickConfig.AXIS_RIGHT_Y);

    if (Math.abs(x) < JoystickConfig.STICK_DEADZONE) x = 0f;
    if (Math.abs(y) < JoystickConfig.STICK_DEADZONE) y = 0f;

    if (x == 0f && y == 0f) {
      return Vector2.ZERO;
    }
    return Vector2.of(x, -y);
  }

  private boolean isRtTriggerJustPressed(Controller controller) {
    float value = controller.getAxis(JoystickConfig.RT_AXIS);
    boolean pressedNow = Math.abs(value) > JoystickConfig.TRIGGER_DEADZONE;
    boolean justPressed = pressedNow && !rtAxisButtonPressed;
    rtAxisButtonPressed = pressedNow;
    return justPressed;
  }

  private void updateMouseCursor(Controller controller) {
    Vector2 aim = readRightStick(controller);
    // avoid jump to position 0,0.
    if (aim.isZero()) {
      return;
    }

    if (cursorX < 0f || cursorY < 0f) {
      cursorX = Gdx.input.getX();
      cursorY = Gdx.input.getY();
    }

    // smooth movement
    float dt = Gdx.graphics.getDeltaTime();
    cursorX += aim.x() * JoystickConfig.CURSOR_SPEED * dt;
    cursorY -= aim.y() * JoystickConfig.CURSOR_SPEED * dt;

    // screen limits
    float maxX = Gdx.graphics.getWidth() - 1f;
    float maxY = Gdx.graphics.getHeight() - 1f;

    // keep inside screen
    if (cursorX < 0f) cursorX = 0f;
    if (cursorY < 0f) cursorY = 0f;
    if (cursorX > maxX) cursorX = maxX;
    if (cursorY > maxY) cursorY = maxY;

    // new value for mouse cursor
    Gdx.input.setCursorPosition(Math.round(cursorX), Math.round(cursorY));
  }

  private void handleMouse(Controller controller) {
    InputProcessor processor = Gdx.input.getInputProcessor();
    if (processor == null) {
      return;
    }

    boolean ltDown =
        Math.abs(controller.getAxis(JoystickConfig.LT_AXIS)) > JoystickConfig.TRIGGER_DEADZONE;
    int x = Gdx.input.getX();
    int y = Gdx.input.getY();

    if (ltDown && !ltMouseDown) {
      processor.touchDown(x, y, 0, Input.Buttons.LEFT);
      ltMouseDown = true;
    } else if (ltDown) {
      processor.touchDragged(x, y, 0);
    } else if (ltMouseDown) {
      processor.touchUp(x, y, 0, Input.Buttons.LEFT);
      ltMouseDown = false;
    }
  }
}
