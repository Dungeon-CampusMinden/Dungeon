package feature.prefabs;

import engine.Entity;
import java.util.Optional;

/**
 * Identifies a runtime event a prefab type can raise for its instances, such as a dialog having
 * been read.
 *
 * <p>Events are output ports: in the level editor they can be linked to {@link PrefabAction
 * actions} of other instances, which then run whenever the event is raised. Level handlers can
 * additionally register listeners per instance through the bound prefab view. Listeners are kept
 * while the instance is reset or updated. They are dropped when the instance is removed or the
 * level is reloaded.
 *
 * <p>To be linkable in the editor, an event has to be returned by {@link Prefab#declaredPorts()}.
 *
 * @param <T> payload passed to listeners
 */
public final class PrefabEvent<T> implements PrefabPort {

  private final String key;
  private final String displayName;
  private final boolean carriesPlayer;

  private PrefabEvent(String key, String displayName, boolean carriesPlayer) {
    this.key = PrefabPorts.requireKey(key);
    this.displayName = PrefabPorts.requireText(displayName, "displayName");
    this.carriesPlayer = carriesPlayer;
  }

  /**
   * Creates an event whose payload is the player that caused it.
   *
   * <p>Linked actions that need a player, such as showing a dialog, run for this player.
   *
   * @param key stable serialized key
   * @param displayName editor-facing name
   * @return new event identity
   */
  public static PrefabEvent<Entity> withPlayer(String key, String displayName) {
    return new PrefabEvent<>(key, displayName, true);
  }

  /**
   * Creates an event without a payload.
   *
   * @param key stable serialized key
   * @param displayName editor-facing name
   * @return new event identity
   */
  public static PrefabEvent<Void> signal(String key, String displayName) {
    return new PrefabEvent<>(key, displayName, false);
  }

  @Override
  public String key() {
    return key;
  }

  @Override
  public String displayName() {
    return displayName;
  }

  @Override
  public Kind kind() {
    return Kind.EVENT;
  }

  /**
   * Returns whether the payload of this event is the player that caused it.
   *
   * @return true if linked actions receive a player
   */
  public boolean carriesPlayer() {
    return carriesPlayer;
  }

  /**
   * Extracts the player that caused the event from a payload.
   *
   * @param payload raised payload
   * @return the causing player, if this event carries one
   */
  Optional<Entity> player(Object payload) {
    return carriesPlayer && payload instanceof Entity entity
        ? Optional.of(entity)
        : Optional.empty();
  }

  @Override
  public String toString() {
    return key;
  }
}
