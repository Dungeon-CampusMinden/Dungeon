package contrib.modules.keypad;

import core.Component;
import core.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Component that represents a keypad with a code that can be entered. */
public class KeypadComponent implements Component {

  private final List<Integer> correctDigits;
  private final List<Integer> enteredDigits;
  private boolean isUIOpen = false;
  private boolean isUnlocked = false;
  private boolean showDigitCount;
  private Runnable action;
  private Entity overlay;

  /**
   * Creates a KeypadComponent.
   *
   * @param correctDigits The correct digits for the keypad
   * @param action The action to execute when the correct digits are entered
   * @param showDigitCount Whether to show the number of digits to be entered
   */
  public KeypadComponent(List<Integer> correctDigits, Runnable action, boolean showDigitCount) {
    this.correctDigits = correctDigits;
    this.enteredDigits = new ArrayList<>();
    this.action = action;
    this.showDigitCount = showDigitCount;
  }

  /**
   * Creates a KeypadComponent with showDigitCount set to true.
   *
   * @param correctDigits The correct digits for the keypad
   * @param action The action to execute when the correct digits are entered
   */
  public KeypadComponent(List<Integer> correctDigits, Runnable action) {
    this(correctDigits, action, true);
  }

  /**
   * Returns the entered digits as a string, with asterisks for unentered digits if showDigitCount
   * is true.
   *
   * @return The entered digits as a string
   */
  public String enteredString() {
    StringBuilder s =
        new StringBuilder(
            enteredDigits.stream().map(Object::toString).collect(Collectors.joining("")));
    if (showDigitCount) {
      while (s.length() < correctDigits.size()) {
        s.append("*");
      }
    }
    return s.toString();
  }

  /**
   * Returns the correct digits as a string.
   *
   * @return The correct digits as a string
   */
  public String correctString() {
    return correctDigits.stream().map(Object::toString).collect(Collectors.joining(""));
  }

  /** Removes the last entered digit. */
  public void backspace() {
    if (enteredDigits.isEmpty() || isUnlocked) return;
    enteredDigits.removeLast();
  }

  /**
   * Adds a digit to the entered digits.
   *
   * @param digit The digit to add
   */
  public void addDigit(int digit) {
    if (enteredDigits.size() >= 8 || isUnlocked) return;
    else if (enteredDigits.size() >= correctDigits.size() && showDigitCount) return;
    enteredDigits.add(digit);
  }

  /** Checks if the entered digits match the correct digits and unlocks if they do. */
  public void checkUnlock() {
    boolean isCorrect = true;
    if (enteredDigits.size() == correctDigits.size()) {
      for (int i = 0; i < enteredDigits.size(); i++) {
        if (!Objects.equals(enteredDigits.get(i), correctDigits.get(i))) {
          isCorrect = false;
          break;
        }
      }
    } else {
      isCorrect = false;
    }

    if (isCorrect) {
      isUnlocked = true;
      action.run();
    }
  }

  /**
   * Gets the list of correct digits for the keypad.
   *
   * @return The correct digits.
   */
  public List<Integer> correctDigits() {
    return correctDigits;
  }

  /**
   * Gets the list of digits currently entered by the user.
   *
   * @return The entered digits.
   */
  public List<Integer> enteredDigits() {
    return enteredDigits;
  }

  /**
   * Checks if the keypad UI is currently open.
   *
   * @return True if the UI is open, false otherwise.
   */
  public boolean isUIOpen() {
    return isUIOpen;
  }

  /**
   * Sets whether the keypad UI is open.
   *
   * @param isUIOpen True to open the UI, false to close it.
   */
  public void isUIOpen(boolean isUIOpen) {
    this.isUIOpen = isUIOpen;
  }

  /**
   * Checks if the keypad has been successfully unlocked.
   *
   * @return True if unlocked, false otherwise.
   */
  public boolean isUnlocked() {
    return isUnlocked;
  }

  /**
   * Sets the unlocked state of the keypad.
   *
   * @param isUnlocked True if unlocked, false otherwise.
   */
  public void isUnlocked(boolean isUnlocked) {
    this.isUnlocked = isUnlocked;
  }

  /**
   * Checks if the number of required digits should be displayed (e.g., using asterisks).
   *
   * @return True if digit count is shown, false otherwise.
   */
  public boolean showDigitCount() {
    return showDigitCount;
  }

  /**
   * Sets whether the number of required digits should be displayed.
   *
   * @param showDigitCount True to show the digit count, false otherwise.
   */
  public void showDigitCount(boolean showDigitCount) {
    this.showDigitCount = showDigitCount;
  }

  /**
   * Gets the action to be executed upon successful unlocking.
   *
   * @return The unlock action.
   */
  public Runnable action() {
    return action;
  }

  /**
   * Sets the action to be executed upon successful unlocking.
   *
   * @param action The new unlock action.
   */
  public void action(Runnable action) {
    this.action = action;
  }

  /**
   * Gets the entity associated with the keypad overlay.
   *
   * @return The overlay entity.
   */
  public Entity overlay() {
    return overlay;
  }

  /**
   * Sets the entity associated with the keypad overlay.
   *
   * @param overlay The new overlay entity.
   */
  public void overlay(Entity overlay) {
    this.overlay = overlay;
  }
}
