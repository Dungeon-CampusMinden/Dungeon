package contrib.configuration;

/** Static configuration values for controller/joystick input mapping. */
public class JoystickConfig {

  /** Utility class; do not instantiate. */
  private JoystickConfig() {}

  /** Left trigger axis index. */
  public static final int LT_AXIS = 4;
  /** Right trigger axis index. */
  public static final int RT_AXIS = 5;

  /** Button index used for interacting with the world. */
  public static final int BUTTON_INTERACT = 2;
  /** Button index used to select the previous skill. */
  public static final int BUTTON_PREV_SKILL = 9;
  /** Button index used to select the next skill. */
  public static final int BUTTON_NEXT_SKILL = 10;
  /** Button index used to toggle inventory. */
  public static final int BUTTON_INVENTAR = 1;

  /** D-pad up button index. */
  public static final int BUTTON_DPAD_UP = 11;
  /** D-pad down button index. */
  public static final int BUTTON_DPAD_DOWN = 12;
  /** D-pad left button index. */
  public static final int BUTTON_DPAD_LEFT = 13;
  /** D-pad right button index. */
  public static final int BUTTON_DPAD_RIGHT = 14;

  /** Left stick X axis index. */
  public static final int AXIS_LEFT_X = 0;
  /** Left stick Y axis index. */
  public static final int AXIS_LEFT_Y = 1;
  /** Right stick X axis index. */
  public static final int AXIS_RIGHT_X = 2;
  /** Right stick Y axis index. */
  public static final int AXIS_RIGHT_Y = 3;

  /** Deadzone threshold for trigger axes. */
  public static final float TRIGGER_DEADZONE = 0.2f;
  /** Deadzone threshold for analog sticks. */
  public static final float STICK_DEADZONE = 0.2f;
  /** Cursor speed multiplier for right stick aiming. */
  public static final float CURSOR_SPEED = 800f;
}
