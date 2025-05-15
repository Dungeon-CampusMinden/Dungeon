package produsAdvanced.abstraction;

/**
 * Abstract class for sorting monsters.
 *
 * <p>This class defines a method to sort monsters, typically represented by their health values.
 * Subclasses must override the {@code sortMonsters} method to implement the desired sorting
 * behavior.
 */
public abstract class MonsterSort {

  /**
   * Sorts the monsters based on their health values.
   *
   * <p>The method returns an array of sorted health values. If a subclass does not implement this
   * method, an {@link UnsupportedOperationException} is thrown.
   *
   * @return an array with the sorted health values of the monsters
   * @throws UnsupportedOperationException if the method is not implemented by the subclass
   */
  public int[] sortMonsters() {
    throw new UnsupportedOperationException("This method must be implemented by the student");
  }
}
