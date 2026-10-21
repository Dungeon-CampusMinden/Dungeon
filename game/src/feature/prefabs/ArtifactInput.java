package feature.prefabs;

import engine.level.elements.ILevel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads the artifact values linked to one slot of a spawned instance.
 *
 * <p>Values are resolved on every call, so an input can be kept by spawned entities and always
 * returns the values currently published by the linked producers. Values that are not published
 * yet, cannot be converted, or are rejected by the slot are missing.
 *
 * @param <T> value type
 */
public final class ArtifactInput<T> {

  private final ILevel level;
  private final String consumer;
  private final ArtifactSlot<T> slot;
  private final List<PrefabLink> links;

  ArtifactInput(ILevel level, String consumer, ArtifactSlot<T> slot, List<PrefabLink> links) {
    this.level = level;
    this.consumer = consumer;
    this.slot = slot;
    this.links = List.copyOf(links);
  }

  /**
   * Returns whether any producer is linked to the slot.
   *
   * @return true if linked
   */
  public boolean isLinked() {
    return !links.isEmpty();
  }

  /**
   * Returns the value of the first linked producer.
   *
   * @return the value, or empty if it is not available
   */
  public Optional<T> value() {
    return links.isEmpty() ? Optional.empty() : resolve(links.getFirst());
  }

  /**
   * Returns the available values of all linked producers by alias, in link order.
   *
   * @return available values
   */
  public Map<String, T> values() {
    Map<String, T> result = new LinkedHashMap<>();
    for (PrefabLink link : links) {
      resolve(link).ifPresent(value -> result.put(alias(link), value));
    }
    return result;
  }

  /**
   * Returns the aliases of all linked producers, including those without an available value.
   *
   * @return aliases in link order
   */
  public List<String> aliases() {
    List<String> result = new ArrayList<>(links.size());
    for (PrefabLink link : links) result.add(alias(link));
    return result;
  }

  /**
   * Registers a listener called whenever a linked producer publishes or drops its value.
   *
   * <p>The listener is dropped when the consuming instance is despawned.
   *
   * @param listener change listener
   */
  public void onChange(Runnable listener) {
    PrefabRuntime.listenToArtifacts(level, consumer, listener);
  }

  private Optional<T> resolve(PrefabLink link) {
    return PrefabRuntime.artifactValue(level, link, slot);
  }

  private static String alias(PrefabLink link) {
    return link.alias() == null ? PrefabWiring.defaultAlias(link.source()) : link.alias();
  }
}
