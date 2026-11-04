package feature.interaction.combinationlock;

import engine.Component;

/**
 * Wheels of a combination lock. It holds one digit per wheel and the wheel that turns next.
 *
 * <p>The server turns the wheels, see {@link CombinationLock}. On clients, the component holds the
 * state synchronized by {@link CombinationLockSync}, without {@link #dialing()}.
 */
public class CombinationLockComponent implements Component {

  private final int[] digits;
  private int selected;
  private boolean dialing;

  /**
   * Creates a lock whose wheels all show 0, with the first wheel selected.
   *
   * @param wheels number of wheels, at least one
   * @throws IllegalArgumentException if {@code wheels} is less than one
   */
  public CombinationLockComponent(int wheels) {
    if (wheels < 1) throw new IllegalArgumentException("A lock needs a wheel: " + wheels);
    digits = new int[wheels];
  }

  /**
   * Turns the selected wheel. Turning past 9 gives 0 and the other way round.
   *
   * @param steps digits to turn by, positive to count up
   */
  public void turn(int steps) {
    digits[selected] = Math.floorMod(digits[selected] + steps, 10);
  }

  /**
   * Selects another wheel. Past the last wheel, the first one follows and the other way round.
   *
   * @param steps wheels to move by, positive to the right
   */
  public void select(int steps) {
    selected = Math.floorMod(selected + steps, digits.length);
  }

  /**
   * Returns the digits the wheels show.
   *
   * @return one digit per wheel from left to right, such as {@code "07"}
   */
  public String digits() {
    StringBuilder text = new StringBuilder();
    for (int digit : digits) text.append(digit);
    return text.toString();
  }

  /**
   * Replaces the digits, for example with synchronized state.
   *
   * @param digits one digit per wheel from left to right
   * @throws IllegalArgumentException if the text has another length or a character that is no digit
   */
  public void digits(String digits) {
    if (!digits.matches("[0-9]{" + this.digits.length + "}")) {
      throw new IllegalArgumentException(
          "Expected " + this.digits.length + " digits, got: " + digits);
    }
    for (int i = 0; i < this.digits.length; i++) this.digits[i] = digits.charAt(i) - '0';
  }

  /**
   * Returns the selected wheel.
   *
   * @return index of the wheel that turns next, from 0 on the left
   */
  public int selected() {
    return selected;
  }

  /**
   * Selects a wheel, for example with synchronized state.
   *
   * @param selected index of the wheel, from 0 on the left
   * @throws IllegalArgumentException if there is no such wheel
   */
  public void selected(int selected) {
    if (selected < 0 || selected >= digits.length) {
      throw new IllegalArgumentException("No wheel " + selected + " of " + digits.length);
    }
    this.selected = selected;
  }

  /**
   * Returns whether the lock is turning its wheels to a dialed code. Meanwhile it ignores further
   * actions.
   *
   * @return true from a dialed code until the lock tried to open, on the server only
   */
  public boolean dialing() {
    return dialing;
  }

  /**
   * Marks whether the lock is turning its wheels to a dialed code.
   *
   * @param dialing whether the lock is dialing
   */
  void dialing(boolean dialing) {
    this.dialing = dialing;
  }

  /**
   * Returns the number of wheels.
   *
   * @return the number of wheels
   */
  public int wheels() {
    return digits.length;
  }
}
