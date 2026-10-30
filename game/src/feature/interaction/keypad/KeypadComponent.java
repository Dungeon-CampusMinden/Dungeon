package feature.interaction.keypad;

import engine.Component;
import java.util.Objects;
import java.util.function.IntSupplier;

/**
 * Input and display state of a numeric or text keypad: the entered text and whether the keypad is
 * unlocked.
 *
 * <p>Answers are checked by the {@link feature.tasks.TaskComponent} of the keypad entity, see
 * {@link KeypadFactory}. On clients, the component holds the state synchronized by {@link
 * KeypadSync}.
 */
public class KeypadComponent implements Component {

  private final IntSupplier maxLength;
  private final boolean showLength;
  private String enteredText = "";
  private boolean unlocked = false;
  private boolean clearOnWrong = true;

  /**
   * Creates a keypad component.
   *
   * @param maxLength supplies the maximum number of characters that can be entered
   * @param showLength whether unentered characters up to {@code maxLength} are shown as {@code *}
   */
  public KeypadComponent(IntSupplier maxLength, boolean showLength) {
    this.maxLength = Objects.requireNonNull(maxLength, "maxLength");
    this.showLength = showLength;
  }

  /**
   * Appends input to the entered text, unless the keypad is unlocked or the input would exceed
   * {@link #maxLength()}.
   *
   * @param input characters to append
   */
  public void add(String input) {
    if (unlocked || enteredText.length() + input.length() > maxLength()) return;
    enteredText += input;
  }

  /** Removes the last entered character, unless the keypad is unlocked. */
  public void backspace() {
    if (enteredText.isEmpty() || unlocked) return;
    enteredText = enteredText.substring(0, enteredText.length() - 1);
  }

  /** Removes the entered text. */
  public void clear() {
    enteredText = "";
  }

  /**
   * Returns the entered text.
   *
   * @return the entered text
   */
  public String enteredText() {
    return enteredText;
  }

  /**
   * Replaces the entered text, e.g. with synchronized state.
   *
   * @param enteredText new entered text
   */
  public void enteredText(String enteredText) {
    this.enteredText = Objects.requireNonNull(enteredText, "enteredText");
  }

  /**
   * Returns the text shown on the keypad: the entered text, padded with {@code *} up to {@link
   * #maxLength()} if {@link #showLength()} is set.
   *
   * @return the displayed text
   */
  public String display() {
    if (!showLength) return enteredText;
    return enteredText + "*".repeat(Math.max(0, maxLength() - enteredText.length()));
  }

  /**
   * Returns the maximum number of characters that can be entered.
   *
   * @return the current maximum length
   */
  public int maxLength() {
    return maxLength.getAsInt();
  }

  /**
   * Returns whether unentered characters up to {@link #maxLength()} are shown as {@code *}.
   *
   * @return true if the length is shown
   */
  public boolean showLength() {
    return showLength;
  }

  /**
   * Returns whether the keypad has been unlocked.
   *
   * @return true if unlocked
   */
  public boolean unlocked() {
    return unlocked;
  }

  /**
   * Sets the unlocked state of the keypad.
   *
   * @param unlocked true if unlocked
   */
  public void unlocked(boolean unlocked) {
    this.unlocked = unlocked;
  }

  /**
   * Returns whether a wrong answer clears the entered text. Defaults to true.
   *
   * @return true if a wrong answer clears the entered text
   */
  public boolean clearOnWrong() {
    return clearOnWrong;
  }

  /**
   * Sets whether a wrong answer clears the entered text.
   *
   * @param clearOnWrong true to clear the entered text after a wrong answer
   */
  public void clearOnWrong(boolean clearOnWrong) {
    this.clearOnWrong = clearOnWrong;
  }
}
