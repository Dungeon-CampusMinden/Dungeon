package engine.input;

import java.util.Objects;

/**
 * Typed name of an input action, written as {@code namespace:name} (for example {@code core:move}).
 *
 * <p>The engine and features define their actions in their own namespaces, rooms in a namespace
 * named after the room.
 *
 * @param namespace owner of the action
 * @param name name of the action inside its namespace
 */
public record ActionId(String namespace, String name) {

  /**
   * Creates an action id.
   *
   * @param namespace owner of the action
   * @param name name of the action inside its namespace
   * @throws IllegalArgumentException if a part is blank or contains {@code :}
   */
  public ActionId {
    Objects.requireNonNull(namespace, "namespace");
    Objects.requireNonNull(name, "name");
    if (namespace.isBlank() || name.isBlank() || namespace.contains(":") || name.contains(":")) {
      throw new IllegalArgumentException("Invalid action id: " + namespace + ":" + name);
    }
  }

  /**
   * Parses the {@code namespace:name} form.
   *
   * @param id the written action id
   * @return the parsed id
   * @throws IllegalArgumentException if the id has no namespace
   */
  public static ActionId of(String id) {
    int separator = id.indexOf(':');
    if (separator < 0) throw new IllegalArgumentException("Action id without namespace: " + id);
    return new ActionId(id.substring(0, separator), id.substring(separator + 1));
  }

  @Override
  public String toString() {
    return namespace + ":" + name;
  }
}
