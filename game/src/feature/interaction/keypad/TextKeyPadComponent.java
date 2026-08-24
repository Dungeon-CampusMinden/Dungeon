package feature.interaction.keypad;

import engine.Component;
import engine.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Component that represents a keypad with a text that can be entered. */
public class TextKeyPadComponent implements Component {

  private final List<String> correctText;
  private final List<String> enteredText;
  private boolean isUIOpen = false;
  private boolean isUnlocked = false;
  private boolean showCharacterCount;
  private Runnable action;
  private Consumer<Entity> onCorrectCode = caller -> {};
  private Consumer<Entity> onWrongCode = caller -> {};
  private int wrongCodeAttempts = 0;
  private Entity overlay;

  /**
   * Creates a TextKeyPadComponent.
   *
   * @param correctText The correct text for the keypad
   * @param action The action to execute when the correct text is entered
   * @param showCharacterCount Whether to show the number of characters to be entered
   */
  public TextKeyPadComponent(
      List<String> correctText, Runnable action, boolean showCharacterCount) {
    this.correctText = correctText;
    this.enteredText = new ArrayList<>();
    this.action = action;
    this.showCharacterCount = showCharacterCount;
  }

  /**
   * Creates a TextKeyPadComponent with showCharacterCount set to true.
   *
   * @param correctText The correct text for the keypad
   * @param action The action to execute when the correct text is entered
   */
  public TextKeyPadComponent(List<String> correctText, Runnable action) {
    this(correctText, action, true);
  }

  /**
   * Creates a TextKeyPadComponent.
   *
   * @param correctText the correct text to enter.
   * @param enteredText the current entered text.
   * @param isUnlocked if the keypad is already unlocked.
   * @param showCharacterCount Whether to show the number of characters to be entered
   */
  public TextKeyPadComponent(
      List<String> correctText,
      List<String> enteredText,
      boolean isUnlocked,
      boolean showCharacterCount) {
    this.correctText = correctText;
    this.enteredText = enteredText;
    this.isUnlocked = isUnlocked;
    this.showCharacterCount = showCharacterCount;
  }

  /**
   * Returns the entered characters as a string, with asterisks for unentered characters if
   * showCharacterCount is true.
   *
   * @return The entered text as a string
   */
  public String enteredString() {
    StringBuilder s =
        new StringBuilder(
            enteredText.stream().map(Object::toString).collect(Collectors.joining("")));
    if (showCharacterCount) {
      while (s.length() < correctText.size()) {
        s.append("*");
      }
    }
    return s.toString();
  }

  /**
   * Returns the correct text as a string.
   *
   * @return The correct text as a string
   */
  public String correctString() {
    return correctText.stream().map(Object::toString).collect(Collectors.joining(""));
  }

  /** Removes the last entered character. */
  public void backspace() {
    if (enteredText.isEmpty() || isUnlocked) return;
    enteredText.removeLast();
  }

  /**
   * Adds a character to the entered text.
   *
   * @param character The character to add
   */
  public void addCharacter(String character) {
    if (enteredText.size() >= 8 || isUnlocked) return;
    else if (enteredText.size() >= correctText.size() && showCharacterCount) return;
    enteredText.add(character);
  }

  /**
   * Checks if the entered characters match the correct characters and unlocks if they do.
   *
   * @param caller entity that submitted the code
   * @throws NullPointerException if caller is null
   */
  public void checkUnlock(Entity caller) {
    Objects.requireNonNull(caller, "caller");
    boolean completeCodeEntered = enteredText.size() == correctText.size();
    boolean isCorrect = completeCodeEntered;
    if (completeCodeEntered) {
      for (int i = 0; i < enteredText.size(); i++) {
        if (!Objects.equals(enteredText.get(i), correctText.get(i))) {
          isCorrect = false;
          break;
        }
      }
    }

    if (isCorrect) {
      isUnlocked = true;
      if (action != null) action.run();
      onCorrectCode.accept(caller);
    } else if (completeCodeEntered) {
      wrongCodeAttempts++;
      onWrongCode.accept(caller);
    }
  }

  /**
   * Gets the list of correct characters for the keypad.
   *
   * @return The correct characters.
   */
  public List<String> correctText() {
    return correctText;
  }

  /**
   * Gets the list of characters currently entered by the user.
   *
   * @return The entered characters.
   */
  public List<String> enteredText() {
    return enteredText;
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
   * Checks if the number of required characters should be displayed (e.g., using asterisks).
   *
   * @return True if character count is shown, false otherwise.
   */
  public boolean showCharacterCount() {
    return showCharacterCount;
  }

  /**
   * Sets whether the number of required characters should be displayed.
   *
   * @param showCharacterCount True to show the character count, false otherwise.
   */
  public void showCharacterCount(boolean showCharacterCount) {
    this.showCharacterCount = showCharacterCount;
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
   * Registers a callback executed after the keypad is unlocked with the correct code.
   *
   * @param onCorrectCode callback to run
   * @throws NullPointerException if the callback is null
   */
  public void onCorrectCode(Runnable onCorrectCode) {
    Objects.requireNonNull(onCorrectCode, "onCorrectCode");
    this.onCorrectCode = caller -> onCorrectCode.run();
  }

  /**
   * Registers a callback executed after the keypad is unlocked with the correct code.
   *
   * @param onCorrectCode callback receiving the submitting entity
   * @throws NullPointerException if the callback is null
   */
  public void onCorrectCode(Consumer<Entity> onCorrectCode) {
    this.onCorrectCode = Objects.requireNonNull(onCorrectCode, "onCorrectCode");
  }

  /**
   * Registers a callback executed after each failed submit.
   *
   * @param onWrongCode callback to run
   * @throws NullPointerException if the callback is null
   */
  public void onWrongCode(Runnable onWrongCode) {
    Objects.requireNonNull(onWrongCode, "onWrongCode");
    this.onWrongCode = caller -> onWrongCode.run();
  }

  /**
   * Registers a callback executed after each complete failed submit.
   *
   * @param onWrongCode callback receiving the submitting entity
   * @throws NullPointerException if the callback is null
   */
  public void onWrongCode(Consumer<Entity> onWrongCode) {
    this.onWrongCode = Objects.requireNonNull(onWrongCode, "onWrongCode");
  }

  /**
   * Returns the number of failed submit attempts.
   *
   * @return failed submit count
   */
  public int wrongCodeAttempts() {
    return wrongCodeAttempts;
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
