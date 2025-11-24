package core.components;

import core.Component;
import core.sound.SoundSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Component for entities that emit audio. Contains a list of {@link SoundSpec} instances. Clients
 * compute spatialization locally based on entity position.
 */
public class SoundComponent implements Component {
  private final List<SoundSpec> sounds = new ArrayList<>();

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
   * <p>The returned list cannot be modified directly. Use {@link #add(SoundSpec)}, {@link
   * #removeByInstance(long)}, {@link #clear()}, or {@link #replaceAll(List)} to modify the sounds.
   *
   * @return an unmodifiable list of sound specifications; never null
   */
  public List<SoundSpec> sounds() {
    return Collections.unmodifiableList(sounds);
  }

  /**
   * Replaces all audio specifications with the provided list.
   *
   * <p>This method clears all existing sounds and adds all sounds from the provided list. A
   * defensive copy is made, so subsequent modifications to the input list do not affect this
   * component.
   *
   * @param specs the new list of sound specifications, or null to clear all sounds
   * @return a list of sound specifications that were removed; never null
   */
  public List<SoundSpec> replaceAll(List<SoundSpec> specs) {
    List<SoundSpec> removed = new ArrayList<>(sounds);
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
   * <p>The sound will be added to the end of the internal list. Null values are silently ignored.
   *
   * @param spec the sound specification to add, or null (which is ignored)
   */
  public void add(SoundSpec spec) {
    if (spec != null) sounds.add(spec);
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
   * <p>After calling this method, {@link #sounds()} will return an empty list. This does not stop
   * any currently playing sounds; use {@link core.sound.AudioApi} to stop active audio instances.
   */
  public void clear() {
    sounds.clear();
  }
}
