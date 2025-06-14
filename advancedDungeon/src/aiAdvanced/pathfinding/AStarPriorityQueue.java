package aiAdvanced.pathfinding;

import core.level.utils.Coordinate;
import java.util.*;

/**
 * AStarPriorityQueue is a custom priority queue implementation for pathfinding algorithms. It uses
 * a priority map to manage elements based on their priority. The queue ensures that elements are
 * ordered by their priority (lower values indicate higher priority).
 */
public class AStarPriorityQueue extends ArrayDeque<Coordinate> {
  // A map to store the priority (f-score) of each coordinate
  private final Map<Coordinate, Double> priorityMap = new HashMap<>();

  /**
   * Adds a coordinate to the queue with a default priority of Double.MAX_VALUE. If the coordinate
   * already exists, its position is updated based on its priority.
   *
   * @param coord The coordinate to be added to the queue.
   */
  @Override
  public void push(Coordinate coord) {
    priorityMap.putIfAbsent(coord, Double.MAX_VALUE);
    addOrUpdatePosition(coord);
  }

  /**
   * Updates the priority of a coordinate in the queue. If the coordinate is already in the queue,
   * it is removed and reinserted at the correct position based on the updated priority.
   *
   * @param coord The coordinate whose priority is to be updated.
   * @param priority The new priority value for the coordinate.
   */
  public void updatePriority(Coordinate coord, double priority) {
    priorityMap.put(coord, priority);
    // Reorder the queue if the element is already in it
    if (contains(coord)) {
      remove(coord);
      addOrUpdatePosition(coord);
    }
  }

  /**
   * Adds a coordinate to the queue in the correct position based on its priority. If the queue is
   * empty, the coordinate is simply added. If the coordinate already exists, it is removed and
   * reinserted at the correct position.
   *
   * @param coord The coordinate to be added or updated in the queue.
   */
  private void addOrUpdatePosition(Coordinate coord) {
    // If queue is empty, just add it
    if (isEmpty()) {
      super.add(coord);
      return;
    }

    // Remove if already exists
    if (contains(coord)) {
      remove(coord);
    }

    double priority = priorityMap.get(coord);

    // Find the correct position and insert
    Iterator<Coordinate> it = iterator();
    int index = 0;

    while (it.hasNext()) {
      Coordinate current = it.next();
      if (priority < priorityMap.getOrDefault(current, Double.MAX_VALUE)) {
        break;
      }
      index++;
    }

    // Create a new list with the element inserted at the right position
    List<Coordinate> tempList = new ArrayList<>(this);
    tempList.add(index, coord);

    // Clear and repopulate the queue
    clear();
    addAll(tempList);
  }

  /**
   * Removes and returns the highest priority (lowest f-score) element from the queue. If the queue
   * is empty, returns null.
   *
   * @return The coordinate with the highest priority, or null if the queue is empty.
   */
  @Override
  public Coordinate pop() {
    if (isEmpty()) {
      return null;
    }
    return removeFirst();
  }
}
