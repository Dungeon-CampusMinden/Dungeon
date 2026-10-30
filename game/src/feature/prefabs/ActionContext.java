package feature.prefabs;

import engine.Entity;
import engine.Game;
import engine.level.elements.ILevel;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Context passed to a running {@link PrefabAction}.
 *
 * @param level level owning the receiving instance
 * @param prefab definition of the receiving instance
 * @param instance normalized receiving instance
 * @param player player that caused the triggering event, if it carries one
 */
public record ActionContext(
    ILevel level, Prefab prefab, PrefabInstance instance, Optional<Entity> player) {

  /** Validates the context members. */
  public ActionContext {
    Objects.requireNonNull(level, "level");
    Objects.requireNonNull(prefab, "prefab");
    Objects.requireNonNull(instance, "instance");
    Objects.requireNonNull(player, "player");
  }

  /**
   * Reads a typed property value of the receiving instance.
   *
   * @param property property descriptor of the receiving prefab type
   * @param <T> property value type
   * @return typed property value
   */
  public <T> T value(PrefabProperty<T> property) {
    return property.get(instance);
  }

  /**
   * Returns the entities currently spawned for the receiving instance.
   *
   * @return live entities, in creation order
   */
  public List<Entity> liveEntities() {
    return PrefabSpawner.liveEntities(level, prefab.side(), instance.name(), instance.type());
  }

  /**
   * Returns the players the action acts on: the player that caused the event, or every player if
   * the event carries none.
   *
   * @return affected players
   */
  public List<Entity> players() {
    return player.map(List::of).orElseGet(() -> Game.allPlayers().toList());
  }

  /**
   * Creates the context used when spawning the receiving instance, e.g. to raise events.
   *
   * @return creation context on the side of the receiving prefab
   */
  public PrefabCreationContext creationContext() {
    return new PrefabCreationContext(level, prefab.side());
  }
}
