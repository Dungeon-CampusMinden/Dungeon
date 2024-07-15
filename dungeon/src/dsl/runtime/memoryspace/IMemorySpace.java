package dsl.runtime.memoryspace;

import dsl.runtime.value.Value;
import java.util.Map;
import java.util.Set;

/** Interface for a memory space in the runtime environment. */
public interface IMemorySpace {
  /**
   * Bind a new value in this memory space.
   *
   * @param name name of the value to bind
   * @param value value to bind
   * @return true on success, false otherwise
   */
  boolean bindValue(String name, Value value);

  /**
   * Resolve a value in this memory space.
   *
   * @param name the name to resolve
   * @return the value resolved by the name
   */
  Value resolve(String name);

  /**
   * Resolve a value in this memory space.
   *
   * @param name the name to resolve
   * @param resolveInParent should the name be resolved in the parent of this memory space?
   * @return the value resolved by the name
   */
  Value resolve(String name, boolean resolveInParent);

  /**
   * Delete a {@link Value} of given name from this IMemorySpace.
   *
   * @param name the name of the value to delete
   */
  void delete(String name);

  /**
   * Set the value specified by the name.
   *
   * @param name name of the value to set
   * @param value value, to which the named value should be set
   * @return true on success, false otherwise
   */
  boolean setValue(String name, Value value);

  /**
   * @return Set of all named values (as combination of name and value).
   */
  Set<Map.Entry<String, Value>> getValueSet();
}
