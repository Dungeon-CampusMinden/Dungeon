package utils.pathfinding;

/**
 * A generic interface and its implementations for managing graph search data structures. These data
 * structures are used in pathfinding algorithms to manage the open set of nodes.
 *
 * @param <T> The type of elements stored in the data structure.
 */
public interface GraphSearchDataStructure<T> {
  /**
   * Adds an element to the data structure.
   *
   * @param element The element to add.
   */
  void push(T element);

  /**
   * Removes and returns the next element from the data structure.
   *
   * @return The next element, or null if the data structure is empty.
   */
  T pop();

  /**
   * Checks if the data structure is empty.
   *
   * @return True if the data structure is empty, false otherwise.
   */
  boolean isEmpty();
}
