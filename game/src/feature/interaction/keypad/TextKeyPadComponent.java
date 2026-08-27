package feature.interaction.keypad;

import engine.Component;
import engine.Entity;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/** Component that represents a keypad with a text that can be entered. */
public class TextKeyPadComponent implements Component {

  private final List<String> correctTexts;
  private String enteredText;
  private boolean isUIOpen = false;
  private boolean isUnlocked = false;
  private Runnable action;
  private Consumer<Entity> onCorrectCode = caller -> {};
  private Consumer<Entity> onWrongCode = caller -> {};
  private int wrongCodeAttempts = 0;
  private Entity overlay;

  /**
   * Creates a TextKeyPadComponent.
   *
   * @param correctTexts The correct texts for the keypad
   * @param action The action to execute when the correct text is entered
   */
  public TextKeyPadComponent(
    List<String> correctTexts, Runnable action) {
    this(correctTexts, "", action,false);
  }

  /**
   * Creates a TextKeyPadComponent.
   *
   * @param correctTexts The correct texts for the keypad
   * @param enteredText the current entered text.
   * @param isUnlocked if the keypad is already unlocked.
   */
  public TextKeyPadComponent(
    List<String> correctTexts, String enteredText, boolean isUnlocked) {
    this(correctTexts, enteredText, ()->{}, isUnlocked);
  }

  /**
   * Creates a TextKeyPadComponent.
   *
   * @param correctTexts the correct texts to enter.
   * @param enteredText the current entered text.
   * @param action the action that runs after unlocking the keypad.
   * @param isUnlocked if the keypad is already unlocked.
   */
  public TextKeyPadComponent(
      List<String> correctTexts,
      String enteredText,
      Runnable action,
      boolean isUnlocked) {
    this.correctTexts = correctTexts;
    this.enteredText = enteredText;
    this.action = action;
    this.isUnlocked = isUnlocked;
  }

  /**
   * Returns the correct texts as a string.
   *
   * @return The correct texts as a string
   */
  public String correctString() {
    return correctTexts.stream().map(Object::toString).collect(Collectors.joining(";"));
  }

  /** Removes the last entered character. */
  public void backspace() {
    if (enteredText.isEmpty() || isUnlocked) return;
    enteredText = enteredText.substring(0, enteredText.length() - 1);
  }

  /**
   * Adds a character to the entered text.
   *
   * @param character The character to add
   */
  public void addCharacter(String character) {
    if (isUnlocked) return;
    enteredText += character;
  }

  /**
   * Checks if the entered characters match the correct characters and unlocks if they do.
   *
   * @param caller entity that submitted the code
   * @throws NullPointerException if caller is null
   */
  public void checkUnlock(Entity caller) {
    Objects.requireNonNull(caller, "caller");
    boolean isCorrect = false;
    for (String validText : correctTexts) {
      if (validText.equals(enteredText)) {
        isCorrect=true;
        break;
      }
    }

    if (isCorrect) {
      isUnlocked = true;
      if (action != null) action.run();
      onCorrectCode.accept(caller);
    } else {
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
    return correctTexts;
  }

  /**
   * Gets the list of characters currently entered by the user.
   *
   * @return The entered characters.
   */
  public String enteredText() {
    return enteredText;
  }

  /**
   * Sets the enteredText to the given parameter.
   * @param text new enteredText value.
   */
  public void setEnteredText(String text) {
    enteredText = text;
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
