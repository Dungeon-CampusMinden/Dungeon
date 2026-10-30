package feature.interaction.keypad;

import engine.Component;

/** Component that represents a keypad with a text that can be entered. */
public class TextKeyPadComponent implements Component {

  private String enteredText;
  private boolean isUnlocked = false;
  private Runnable action;

  /**
   * Creates a TextKeyPadComponent.
   *
   * @param action The action to execute when the correct text is entered
   */
  public TextKeyPadComponent(Runnable action) {
    this("", action, false);
  }

  /**
   * Creates a TextKeyPadComponent.
   *
   * @param enteredText the current entered text.
   * @param isUnlocked if the keypad is already unlocked.
   */
  public TextKeyPadComponent(String enteredText, boolean isUnlocked) {
    this(enteredText, () -> {}, isUnlocked);
  }

  /**
   * Creates a TextKeyPadComponent.
   *
   * @param enteredText the current entered text.
   * @param action the action that runs after unlocking the keypad.
   * @param isUnlocked if the keypad is already unlocked.
   */
  public TextKeyPadComponent(String enteredText, Runnable action, boolean isUnlocked) {
    this.enteredText = enteredText;
    this.action = action;
    this.isUnlocked = isUnlocked;
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
   * Gets the text currently entered by the user.
   *
   * @return The entered text.
   */
  public String enteredText() {
    return enteredText;
  }

  /**
   * Sets the enteredText to the given parameter.
   *
   * @param text new enteredText value.
   */
  public void setEnteredText(String text) {
    enteredText = text;
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
}
