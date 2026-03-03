package core.systems.input;

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

  /**
   * Processes joystick input for a specific player entity.
   *
   * <p>Updates mouse cursor, handles mouse simulation, and processes movement/actions if controls
   * are enabled. Inventory toggle is always available regardless of control state.
   *
   * @param player the player entity to process joystick input for
   */
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

  private boolean isControllerStillConnected(Controller controller) {
    if (controller == null) {
      return false;
    }
    var controllers = Controllers.getControllers();
    for (int i = 0; i < controllers.size; i++) {
      if (controllers.get(i) == controller) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the currently active controller, lazily resolving the first connected controller if
   * none is cached yet.
   *
   * @return the active controller, or null if no controller is connected
   */
  private Optional<Controller> getActiveController() {
    if (activeController == null || !isControllerStillConnected(activeController)) {
      var controllers = Controllers.getControllers();
      activeController = controllers.size == 0 ? null : controllers.first();
    }
    return Optional.ofNullable(activeController);
  }

  /**
   * Checks if controls are disabled for the given player entity.
   *
   * @param player the player entity to check
   * @return true if the player has an InputComponent and controls are deactivated, false otherwise
   */
  private boolean isControlsDisabled(Entity player) {
    return player.fetch(InputComponent.class).map(InputComponent::deactivateControls).orElse(false);
  }

  /**
   * Processes movement input from the controller and sends movement commands to the network.
   *
   * <p>Reads input from the left stick first, falling back to D-Pad if the stick is neutral. Sends
   * movement commands for UP, DOWN, LEFT, and RIGHT directions based on the input vector.
   *
   * @param controller the controller to read movement input from
   */
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

  /**
   * Processes action input from the controller and sends action commands to the network.
   *
   * <p>Handles right trigger for skill casting, interact button, and skill selection buttons
   * (next/prev). All actions use the current cursor position as the target point.
   *
   * @param controller the controller to read action input from
   * @param target the target point for actions (typically the cursor position)
   */
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

  /**
   * Handles inventory toggle input from the controller.
   *
   * <p>Checks if the inventory button was just pressed and toggles the inventory for the player
   * entity if so.
   *
   * @param controller the controller to read inventory button input from
   * @param player the player entity to toggle inventory for
   */
  private void handleInventoryToggle(Controller controller, Entity player) {
    if (isJustPressed(controller, JoystickConfig.BUTTON_INVENTAR)) {
      HeroController.toggleInventory(player);
    }
  }

  /**
   * Checks if a button was just pressed (pressed in this frame but not in the previous frame).
   *
   * <p>Returns true only on the first frame the button is detected as pressed, preventing repeated
   * triggers while the button is held down.
   *
   * @param controller the controller to check the button state from
   * @param button the button index to check
   * @return true if the button was just pressed this frame, false otherwise
   */
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

  /**
   * Reads D-Pad input from the controller and returns a normalized direction vector.
   *
   * <p>Returns a vector with components in the range [-1, 1] for each axis, or a zero-vector if no
   * D-Pad buttons are pressed.
   *
   * @param controller the controller to read D-Pad input from
   * @return a direction vector representing D-Pad input, or ZERO if no input
   */
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

  /**
   * Reads left stick input from the controller and returns a direction vector.
   *
   * <p>Applies deadzone filtering and inverts the Y-axis. Returns zero-vector if input is within
   * the deadzone threshold.
   *
   * @param controller the controller to read left stick input from
   * @return a direction vector from the left stick, or ZERO if within deadzone
   */
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

  /**
   * Reads right stick input from the controller and returns a direction vector.
   *
   * <p>Applies deadzone filtering and inverts the Y-axis. Returns {@link Vector2#ZERO} if input is
   * within the deadzone threshold.
   *
   * @param controller the controller to read right stick input from
   * @return a direction vector from the right stick, or ZERO if within deadzone
   */
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

  /**
   * Checks if the rt-button was just pressed (crossed the deadzone threshold this frame).
   *
   * <p>Returns true only on the first frame the trigger exceeds the deadzone, preventing repeated
   * triggers while the trigger is held down.
   *
   * @param controller the controller to check the right trigger state from
   * @return true if the rt-button was just pressed this frame, false otherwise
   */
  private boolean isRtTriggerJustPressed(Controller controller) {
    float value = controller.getAxis(JoystickConfig.RT_AXIS);
    boolean pressedNow = Math.abs(value) > JoystickConfig.TRIGGER_DEADZONE;
    boolean justPressed = pressedNow && !rtAxisButtonPressed;
    rtAxisButtonPressed = pressedNow;
    return justPressed;
  }

  /**
   * Updates the mouse cursor position based on right stick input.
   *
   * <p>Moves the cursor smoothly using the right stick direction, initializing from the current
   * mouse position if needed. The cursor is constrained to screen boundaries.
   *
   * @param controller the controller to read right stick input from
   */
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
    float maxX = Game.windowWidth() - 1f;
    float maxY = Game.windowHeight() - 1f;

    // keep inside screen
    if (cursorX < 0f) cursorX = 0f;
    if (cursorY < 0f) cursorY = 0f;
    if (cursorX > maxX) cursorX = maxX;
    if (cursorY > maxY) cursorY = maxY;

    // new value for mouse cursor
    Gdx.input.setCursorPosition(Math.round(cursorX), Math.round(cursorY));
  }

  /**
   * Simulates mouse button events using the lt-button.
   *
   * <p>Maps the lt-button to mouse button actions: touchDown when pressed, touchDragged while held,
   * and touchUp when released. Uses the current cursor position for all events.
   *
   * @param controller the controller to read left trigger input from
   */
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
