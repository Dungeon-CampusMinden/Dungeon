package feature.interaction.combinationlock;

import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.interaction.keypad.KeypadFactory;
import feature.systems.EventScheduler;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;

/**
 * Shows the dialog of a combination lock and applies its actions on the server.
 *
 * <p>The lock belongs to an entity with a {@link CombinationLockComponent}, such as a chest. The
 * dialog sends each action as an {@link DialogContextKeys#ON_CONFIRM} response. With its controls,
 * the arrows send {@link #UP} and {@link #DOWN}, which turn the selected wheel, and {@link #LEFT}
 * and {@link #RIGHT}, which select another wheel; the button sends {@link #OPEN}. Without controls,
 * a whole code arrives at once through {@link #dial(String)}. The lock then turns one wheel after
 * the other to its digit and tries to open. The wheels keep their digits after the dialog closes.
 */
public final class CombinationLock {

  /** Action that turns the selected wheel one digit up. */
  public static final String UP = "up";

  /** Action that turns the selected wheel one digit down. */
  public static final String DOWN = "down";

  /** Action that selects the wheel to the left. */
  public static final String LEFT = "left";

  /** Action that selects the wheel to the right. */
  public static final String RIGHT = "right";

  /** Action that tries to open the lock with the digits the wheels show. */
  public static final String OPEN = "open";

  /**
   * Dialog attribute (a {@link Boolean}) whether the dialog shows arrows and an open button;
   * without them it only shows the wheels, for a code that arrives through {@link #dial(String)}.
   */
  public static final String CONTROLS = "combinationLockControls";

  // Prefix of the action that dials a whole code.
  private static final String DIAL = "dial:";
  // Time for one wheel to roll to its digit, and the pause before the lock tries to open.
  private static final long WHEEL_MS = 800;
  private static final long SETTLE_MS = 400;

  private CombinationLock() {}

  /**
   * Returns the action that dials a whole code. The lock turns the wheels one after the other to
   * the digits and then tries to open. An action with a code of the wrong length does nothing.
   *
   * @param digits one digit per wheel from left to right
   * @return the action
   */
  public static String dial(String digits) {
    return DIAL + digits;
  }

  /**
   * Shows the dialog of a lock. Once the user tries to open it, the dialog closes and the caller
   * checks the digits, for example to open a chest or to tell that the lock stays shut.
   *
   * @param lock entity with a {@link CombinationLockComponent}
   * @param user the player using the lock
   * @param title title of the dialog
   * @param question what the dialog asks for, such as a riddle that gives the code
   * @param hint how to work the lock, below the question; empty for none
   * @param controls whether the dialog shows arrows and an open button; without them, the code
   *     arrives through {@link #dial(String)}, for example from the audience
   * @param onOpen receives the digits the wheels show when the user tries to open the lock
   * @return the dialog
   */
  public static UIComponent show(
      Entity lock,
      Entity user,
      String title,
      String question,
      String hint,
      boolean controls,
      Consumer<String> onOpen) {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.COMBINATION_LOCK)
            .put(DialogContextKeys.ENTITY, lock.id())
            .put(DialogContextKeys.TITLE, title)
            .put(DialogContextKeys.QUESTION, question)
            .put(DialogContextKeys.DESCRIPTION, hint)
            .put(CONTROLS, controls)
            .build();
    UIComponent ui = DialogFactory.show(context, true, true, user.id());
    CombinationLockComponent component = lock.fetch(CombinationLockComponent.class).orElseThrow();
    // The dialog may close while the wheels turn, for example when the game ends.
    BooleanSupplier stillOpen =
        () ->
            ui.dialogContext()
                .find(DialogContextKeys.OWNER_ENTITY, Integer.class)
                .flatMap(Game::findEntityById)
                .flatMap(owner -> owner.fetch(UIComponent.class))
                .filter(current -> current == ui)
                .isPresent();
    Runnable open =
        () -> {
          if (!stillOpen.getAsBoolean()) return;
          UIUtils.closeDialog(ui);
          onOpen.accept(component.digits());
        };
    ui.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue(String action))
              || component.dialing()) {
            return;
          }
          if (action.startsWith(DIAL)) {
            dial(
                lock,
                user,
                action.substring(DIAL.length()),
                stillOpen,
                open,
                (step, delay) -> EventScheduler.schedulePlayAction(step::run, delay));
          } else if (press(lock, user, action)) {
            open.run();
          }
        });
    return ui;
  }

  /**
   * Applies an arrow or the button of the lock dialog, except {@link #OPEN}, which the caller
   * handles. Unknown actions are ignored.
   *
   * @param lock entity with a {@link CombinationLockComponent}
   * @param user the player who pressed the button
   * @param action the action of the button
   * @return true if the user tries to open the lock
   */
  static boolean press(Entity lock, Entity user, String action) {
    CombinationLockComponent component = lock.fetch(CombinationLockComponent.class).orElseThrow();
    switch (action) {
      case UP -> component.turn(1);
      case DOWN -> component.turn(-1);
      case LEFT -> component.select(-1);
      case RIGHT -> component.select(1);
      case OPEN -> {
        return true;
      }
      default -> {
        return false;
      }
    }
    click(component, user, action.equals(UP) || action.equals(DOWN));
    return false;
  }

  /**
   * Turns the wheels one after the other to the digits of a code, then runs {@code then}. The first
   * wheel turns at once, each further one after the previous one had time to roll. Until the last
   * step, the lock is {@link CombinationLockComponent#dialing() dialing}. Once {@code active} turns
   * false, the remaining wheels stay where they are and {@code then} does not run.
   *
   * @param lock entity with a {@link CombinationLockComponent}
   * @param user the player who dialed
   * @param digits one digit per wheel from left to right
   * @param active whether the dial may go on, for example while its dialog is open
   * @param then runs once the last wheel stopped, for example to try to open the lock
   * @param schedule runs a step after a delay in milliseconds
   * @return true if the code fits the wheels and the lock dials it
   */
  static boolean dial(
      Entity lock,
      Entity user,
      String digits,
      BooleanSupplier active,
      Runnable then,
      ObjLongConsumer<Runnable> schedule) {
    CombinationLockComponent component = lock.fetch(CombinationLockComponent.class).orElseThrow();
    if (!digits.matches("[0-9]{" + component.wheels() + "}")) return false;
    component.dialing(true);
    for (int wheel = 0; wheel < digits.length(); wheel++) {
      int index = wheel;
      Runnable turn =
          () -> {
            if (!active.getAsBoolean()) return;
            component.selected(index);
            component.turn(digits.charAt(index) - component.digits().charAt(index));
            click(component, user, true);
          };
      if (wheel == 0) turn.run();
      else schedule.accept(turn, wheel * WHEEL_MS);
    }
    schedule.accept(
        () -> {
          component.dialing(false);
          if (active.getAsBoolean()) then.run();
        },
        digits.length() * WHEEL_MS + SETTLE_MS);
    return true;
  }

  // A turned wheel clicks like the digit key of a keypad, selecting a wheel clicks lower.
  private static void click(CombinationLockComponent component, Entity user, boolean turned) {
    int digit = component.digits().charAt(component.selected()) - '0';
    KeypadFactory.beep(user, turned ? KeypadFactory.digitPitch(digit) : 0.8f);
  }
}
