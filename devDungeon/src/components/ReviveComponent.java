package components;

import core.Component;

/**
 * The ReviveComponent class implements the Component interface. This class is used to manage the
 * revive functionality of an entity in the game.
 */
public class ReviveComponent implements Component {

  // The number of times the entity can be revived
  private int reviveCount;

  /**
   * Constructor for the ReviveComponent class.
   *
   * @param reviveCount The initial number of times the entity can be revived.
   */
  public ReviveComponent(int reviveCount) {
    this.reviveCount = reviveCount;
  }

  /**
   * Getter for the reviveCount field.
   *
   * @return The number of times the entity can be revived.
   */
  public int reviveCount() {
    return this.reviveCount;
  }

  /**
   * Setter for the reviveCount field.
   *
   * @param reviveCount The new number of times the entity can be revived.
   */
  public void reviveCount(int reviveCount) {
    this.reviveCount = reviveCount;
  }

  /**
   * Overrides the toString method from the Object class.
   *
   * @return A string representation of the ReviveComponent object.
   */
  @Override
  public String toString() {
    return "ReviveComponent{" + "reviveCount=" + this.reviveCount + '}';
  }
}
