package contrib.components;

import core.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A component that allows entities to store and manage arbitrary state values identified by String
 * keys.
 *
 * <p>States are stored as key-value pairs where the key is a String identifier and the value is any
 * Object.
 *
 * <p><b>Usage example:</b>
 *
 * <pre>{@code
 * StateComponent stateComponent = new StateComponent();
 *
 * // Add an Integer state with key "score"
 * stateComponent.add("score", 10);
 *
 * // Update the "score" state
 * stateComponent.add("score", 20);
 *
 * // Retrieve the "score" state
 * stateComponent.value("score").ifPresent(score -> System.out.println("Score: " + score));
 *
 * // Remove the "score" state
 * stateComponent.remove("score");
 * }</pre>
 */
public class StateComponent implements Component {
  Map<String, Object> states = new HashMap<>();

  /**
   * Adds or updates the state value for the given id.
   *
   * @param id the identifier of the state
   * @param obj the state value to store
   */
  public void add(String id, Object obj) {
    states.put(id, obj);
  }

  /**
   * Removes the state value associated with the given id.
   *
   * @param id the identifier of the state to remove
   */
  public void remove(String id) {
    states.remove(id);
  }

  /**
   * Checks if a state with the given id exists.
   *
   * @param id the identifier of the state
   * @return true if the state exists, false otherwise
   */
  public boolean contains(String id) {
    return states.containsKey(id);
  }

  /**
   * Retrieves the state value associated with the given id.
   *
   * @param id the identifier of the state
   * @return an Optional containing the state value if present, or empty if not found
   */
  public Optional<Object> value(String id) {
    return Optional.ofNullable(states.get(id));
  }
}
