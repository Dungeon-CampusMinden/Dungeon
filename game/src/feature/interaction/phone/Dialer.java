package feature.interaction.phone;

import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.network.messages.s2c.DialogFeedbackMessage;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFeedbackReceiver;
import feature.interaction.keypad.DigitEntry;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * The client side of a phone dialog: how many digits fit, the lines on the note of important
 * extensions, the dialing itself and what the lines answer.
 *
 * <p>A dialog of a phone types into a {@link DigitEntry} with {@link #maxDigits()} and hands the
 * confirmed number to {@link #dial(String)}, which sends it to the server and tells right away
 * whether a line answers. The dialog receives the line's answer as {@link DialogFeedbackReceiver}
 * and reads it with {@link #answer(DialogFeedbackMessage)}; a yes to a question goes back with
 * {@link #yes()}. {@link PhoneUI} is the engine's look; a room replaces it by registering a builder
 * of its own for {@link feature.hud.dialogs.DialogType.DefaultTypes#PHONE} with {@link
 * feature.hud.dialogs.DialogFactory#register}.
 */
public final class Dialer {

  private final int maxDigits;
  private final Set<String> numbers;
  private final List<PhoneLine> note;
  private final Consumer<DialogResponseMessage.Payload> server;
  private final Consumer<DialogResponseMessage.Payload> yes;

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
    yes = DialogCallbackResolver.createButtonCallback(context.dialogId(), PhoneFactory.YES);
  }

  /**
   * Reads what a line answered, in the language of this client.
   *
   * @param feedback the feedback the server sent to the dialog
   * @return the answer of the line
   */
  public static Answer answer(DialogFeedbackMessage feedback) {
    String text = Game.localization().getCurrentTranslator().translate(feedback.messageKey());
    return new Answer(text, feedback.targetTabKey().equals(PhoneFactory.ASK));
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
   * Dials a number: sends it to the server, which lets the line answer in the dialog.
   *
   * @param number the confirmed digits
   * @return whether a line answers under the number; otherwise there is no connection
   */
  public boolean dial(String number) {
    server.accept(new DialogResponseMessage.StringValue(number));
    return numbers.contains(number);
  }

  /** Answers yes to the question a line asked; a no needs no answer. */
  public void yes() {
    yes.accept(null);
  }

  /**
   * What a line answered.
   *
   * @param text the announcement or the question
   * @param question whether the line waits for a yes
   */
  public record Answer(String text, boolean question) {}
}
