package contrib.modules.keypad;

import contrib.modules.interaction.InteractionComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;
import java.util.Arrays;
import java.util.List;

/** Factory class for creating keypad entities. */
public class KeypadFactory {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(KeypadFactory.class);

  private static final float DEFAULT_INTERACTION_RADIUS = 1.5f;
  private static final IPath TEXTURE_ON = new SimpleIPath("objects/keypad/on.png");
  private static final IPath TEXTURE_OFF = new SimpleIPath("objects/keypad/off.png");

  /**
   * Creates a keypad at the designated position.
   *
   * @param pos The position where the lever will be created.
   * @param correctDigits The correct digits that will start the action if entered
   * @param action The action to execute when the correct digits are entered
   * @param showDigitCount Whether to show the number of digits to be entered
   * @return The created keypad entity.
   */
  public static Entity createKeypad(
      Point pos, List<Integer> correctDigits, Runnable action, boolean showDigitCount) {
    Entity entity = new Entity("keypad");

    entity.add(new PositionComponent(pos));

    State stClosed = new State("closed", TEXTURE_OFF);
    State stOpen = new State("open", TEXTURE_ON);
    StateMachine sm = new StateMachine(Arrays.asList(stClosed, stOpen));
    sm.addTransition(stClosed, "open", stOpen);
    sm.addTransition(stOpen, "close", stClosed);
    DrawComponent dc = new DrawComponent(sm);
    entity.add(dc);

    KeypadComponent kc = new KeypadComponent(correctDigits, action, showDigitCount);
    entity.add(kc);

    entity.add(
        new InteractionComponent(
            DEFAULT_INTERACTION_RADIUS,
            true,
            (e, who) -> {
              kc.isUIOpen(true);
              LOGGER.info("Interacted with keypad sprite");
            }));
    return entity;
  }
}
