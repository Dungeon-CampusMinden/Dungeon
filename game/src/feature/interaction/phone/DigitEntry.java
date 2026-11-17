package feature.interaction.phone;

import java.util.function.Consumer;

/**
 * The digits typed on a keypad that a dialog draws itself, e.g. to dial a phone number or to enter
 * a code: at most a given number of digits, with keys to clear, to delete the last digit and to
 * confirm.
 *
 * <p>Confirming hands the digits to the owner, which answers with {@link #answer()}, e.g. by
 * showing "no connection" on its display; the next key clears the answer and starts a new entry.
 * Confirming again or without digits does nothing. A dialog draws the keys and the display and
 * passes the key presses on to this class.
 */
final class DigitEntry {

  private final int maxDigits;
  private final Consumer<String> onConfirm;
  private String digits = "";
  private boolean answered;

  /**
   * Creates an empty entry.
   *
   * @param maxDigits digits that fit
   * @param onConfirm receives the typed digits when they are confirmed
   */
  DigitEntry(int maxDigits, Consumer<String> onConfirm) {
    this.maxDigits = maxDigits;
    this.onConfirm = onConfirm;
  }

  /**
   * Types a digit, unless the entry is full.
   *
   * @param digit the digit, '0' to '9'
   * @throws IllegalArgumentException if the character is no digit from '0' to '9'
   */
  void digit(char digit) {
    if (digit < '0' || digit > '9') throw new IllegalArgumentException("No digit: " + digit);
    startOver();
    if (digits.length() < maxDigits) digits += digit;
  }

  /** Clears the digits. */
  void clear() {
    startOver();
    digits = "";
  }

  /** Deletes the last digit. */
  void backspace() {
    startOver();
    digits = digits.substring(0, Math.max(0, digits.length() - 1));
  }

  /** Hands the digits to the owner, unless there are none or the owner already answered them. */
  void confirm() {
    if (!answered && !digits.isEmpty()) onConfirm.accept(digits);
  }

  /** Marks the digits as answered; the owner shows its answer until the next key. */
  void answer() {
    answered = true;
  }

  /**
   * Returns whether the owner shows an answer.
   *
   * @return true from {@link #answer()} until the next key
   */
  boolean answered() {
    return answered;
  }

  /**
   * Returns the typed digits.
   *
   * @return the digits, empty if none were typed
   */
  String digits() {
    return digits;
  }

  // The first key after an answer clears it.
  private void startOver() {
    if (!answered) return;
    answered = false;
    digits = "";
  }
}
