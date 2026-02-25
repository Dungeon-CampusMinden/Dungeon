package core.components;

import core.Component;
import core.sound.SoundSpec;
import java.util.*;

/**
 * Component for entities that emit audio. Contains a set of {@link SoundSpec} instances. Clients
 * compute spatialization locally based on entity position.
 */
public class SoundComponent implements Component {
  private final Set<SoundSpec> sounds = new HashSet<>();

  /** Creates an empty SoundComponent with no initial sounds. */
  public SoundComponent() {}

  /**
   * Creates a SoundComponent with an initial sound specification.
   *
   * @param initial the initial sound spec to add, or null to create an empty component
   */
  public SoundComponent(SoundSpec initial) {
    if (initial != null) sounds.add(initial);
  }

  /**
   * Returns an immutable view of all audio specifications in this component.
   *
   * <p>The returned set cannot be modified directly. Use {@link #add(SoundSpec)}, {@link
   * #removeByInstance(long)}, {@link #clear()}, or {@link #replaceAll(Set)} to modify the sounds.
   *
   * @return an unmodifiable set of sound specifications; never null
   */
  public Set<SoundSpec> sounds() {
    return Collections.unmodifiableSet(sounds);
  }

  /**
   * Replaces all audio specifications with the provided set.
   *
   * <p>This method clears all existing sounds and adds all sounds from the provided set. A
   * defensive copy is made, so subsequent modifications to the input set do not affect this
   * component.
   *
   * @param specs the new set of sound specifications, or null to clear all sounds
   * @return a set of sound specifications that were removed; never null
   */
  public Set<SoundSpec> replaceAll(Set<SoundSpec> specs) {
    Set<SoundSpec> removed = new HashSet<>(sounds);
    sounds.clear();
    if (specs != null) {
      removed.removeAll(specs);
      sounds.addAll(specs);
    }
    return removed;
  }

  /**
   * Appends a sound specification to this component.
   *
   * <p>If a sound with the same instance ID already exists, this method will overwrite it by adding
   * the new spec.
   *
   * @param spec the sound specification to add, must not be null
   * @throws NullPointerException if spec is null
   */
  public void add(SoundSpec spec) {
    Objects.requireNonNull(spec);

    sounds.add(spec);
  }

  /**
   * Removes a sound specification by its unique instance ID.
   *
   * <p>If multiple sounds have the same instance ID (which should not occur in practice), all
   * matching sounds will be removed. If no sound with the given ID exists, this method has no
   * effect.
   *
   * @param instanceId the unique instance identifier of the sound to remove
   */
  public void removeByInstance(long instanceId) {
    sounds.removeIf(s -> s.instanceId() == instanceId);
  }

  /**
   * Removes all audio specifications from this component.
   *
   * <p>After calling this method, {@link #sounds()} will return an empty set. This does not stop
   * any currently playing sounds; use {@link core.sound.AudioApi} to stop active audio instances.
   */
  public void clear() {
    sounds.clear();
  }
}
