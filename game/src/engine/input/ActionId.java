package engine.input;

import java.util.List;
import java.util.Objects;

/**
 * Typed name of an input action, written as {@code namespace:name} (for example {@code core:move}).
 * It carries the default bindings of the action; only the name decides equality.
 *
 * <p>The engine and features define their actions in their own namespaces, rooms in a namespace
 * named after the room. An action becomes known to the {@link BindingTable} through a context that
 * declares it.
 */
public final class ActionId {
  private final String namespace;
  private final String name;
  private final List<Binding> defaults;

  /**
   * Creates an action id.
   *
   * @param namespace owner of the action
   * @param name name of the action inside its namespace
   * @param defaults the default bindings of the action
   * @throws IllegalArgumentException if a name part is blank or contains {@code :}
   */
  public ActionId(String namespace, String name, Binding... defaults) {
    Objects.requireNonNull(namespace, "namespace");
    Objects.requireNonNull(name, "name");
    if (namespace.isBlank() || name.isBlank() || namespace.contains(":") || name.contains(":")) {
      throw new IllegalArgumentException("Invalid action id: " + namespace + ":" + name);
    }
    this.namespace = namespace;
    this.name = name;
    this.defaults = List.of(defaults);
  }

  /**
   * Parses the {@code namespace:name} form, which refers to an action without defining it.
   *
   * @param id the written action id
   * @return the parsed id, without defaults
   * @throws IllegalArgumentException if the id has no namespace
   */
  public static ActionId of(String id) {
    int separator = id.indexOf(':');
    if (separator < 0) throw new IllegalArgumentException("Action id without namespace: " + id);
    return new ActionId(id.substring(0, separator), id.substring(separator + 1));
  }

  /**
   * Gets the owner of the action.
   *
   * @return the namespace
   */
  public String namespace() {
    return namespace;
  }

  /**
   * Gets the name inside the namespace.
   *
   * @return the name
   */
  public String name() {
    return name;
  }

  /**
   * Gets the default bindings.
   *
   * @return the bindings the action has unless the player overrides them
   */
  public List<Binding> defaults() {
    return defaults;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof ActionId id && namespace.equals(id.namespace) && name.equals(id.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(namespace, name);
  }

  @Override
  public String toString() {
    return namespace + ":" + name;
  }
}
