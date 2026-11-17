package feature.interaction.phone;

import static engine.input.Binding.key;

import com.badlogic.gdx.Input.Keys;
import engine.input.ActionId;
import engine.input.ContextSpec;
import engine.input.UiActions;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The client side of a phone dialog: how many digits fit, the lines on the note of important
 * extensions and the dialing itself.
 *
 * <p>A dialog of a phone takes up to {@link #maxDigits()} digits and hands the confirmed number to
 * {@link #dial(String)}, which sends it to the server and tells right away whether a line answers.
 * {@link PhoneUI} is the engine's look; a room replaces it by registering a builder of its own for
 * {@link feature.hud.dialogs.DialogType.DefaultTypes#PHONE} with {@link
 * feature.hud.dialogs.DialogFactory#register}.
 *
 * <p>Every phone dialog takes its keys through the input context {@link #INPUT}; a look reacts to
 * them with {@link feature.hud.UIUtils#onUiAction}.
 */
public final class Dialer {

  /** Types a digit, the one of its index: the digit keys and the number pad. */
  public static final List<ActionId> DIGITS =
      IntStream.rangeClosed(0, 9)
          .mapToObj(
              digit ->
                  new ActionId(
                      "phone",
                      "digit_" + digit,
                      key(Keys.NUM_0 + digit),
                      key(Keys.NUMPAD_0 + digit)))
          .toList();

  /** Deletes the last digit. */
  public static final ActionId BACKSPACE = new ActionId("phone", "backspace", key(Keys.BACKSPACE));

  /** Dials the typed number. */
  public static final ActionId DIAL =
      new ActionId("phone", "dial", key(Keys.ENTER), key(Keys.NUMPAD_ENTER));

  /** Input context of a phone dialog: its keys, and the pad presses the keys through its cursor. */
  public static final ContextSpec INPUT =
      UiActions.modal(
          "phone",
          Stream.concat(DIGITS.stream(), Stream.of(BACKSPACE, DIAL, UiActions.CLICK))
              .toArray(ActionId[]::new));

  private final int maxDigits;
  private final Set<String> numbers;
  private final List<PhoneLine> note;
  private final Consumer<DialogResponseMessage.Payload> server;

  /**
   * Creates the dialer of a phone dialog that {@link PhoneFactory#use(engine.Entity,
   * engine.Entity)} opened.
   *
   * @param context the dialog context with the numbers and the note of the phone
   */
  public Dialer(DialogContext context) {
    maxDigits = context.require(PhoneFactory.DIGITS, Integer.class);
    numbers = Set.of(context.require(PhoneFactory.NUMBERS, String[].class));
    String[] names = context.require(PhoneFactory.NOTE_NAMES, String[].class);
    String[] noteNumbers = context.require(PhoneFactory.NOTE_NUMBERS, String[].class);
    note =
        IntStream.range(0, names.length)
            .mapToObj(index -> new PhoneLine(noteNumbers[index], names[index], true))
            .toList();
    server = DialogCallbackResolver.createButtonCallback(context.dialogId(), PhoneFactory.DIAL);
  }

  /**
   * Returns how many digits a number can have.
   *
   * @return the most digits the keypad takes
   */
  public int maxDigits() {
    return maxDigits;
  }

  /**
   * Returns the lines the note of important extensions lists.
   *
   * @return the listed lines in the order of the phone
   */
  public List<PhoneLine> note() {
    return note;
  }

  /**
   * Dials a number: sends it to the server, which lets the line answer and closes the dialog.
   *
   * @param number the confirmed digits
   * @return whether a line answers under the number; otherwise there is no connection
   */
  public boolean dial(String number) {
    server.accept(new DialogResponseMessage.StringValue(number));
    return numbers.contains(number);
  }
}
