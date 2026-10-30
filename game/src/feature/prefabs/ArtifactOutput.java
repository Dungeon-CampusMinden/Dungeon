package feature.prefabs;

import java.util.Objects;

/**
 * Value a prefab type produces for its instances, e.g. a generated code.
 *
 * <p>Artifact outputs are output ports: they can be linked to any number of {@link ArtifactSlot
 * slots} of other instances. Instances publish their values with {@link Prefab#publish} while they
 * are spawned.
 *
 * @param <T> value type
 */
public final class ArtifactOutput<T> implements PrefabPort {

  private final String key;
  private final String displayName;
  private final ArtifactType<T> type;

  private ArtifactOutput(String key, String displayName, ArtifactType<T> type) {
    this.key = PrefabPorts.requireKey(key);
    this.displayName = PrefabPorts.requireText(displayName, "displayName");
    this.type = Objects.requireNonNull(type, "type");
  }

  /**
   * Creates an artifact output.
   *
   * @param key stable serialized key
   * @param displayName editor-facing name
   * @param type type of the produced values
   * @param <T> value type
   * @return new artifact output
   */
  public static <T> ArtifactOutput<T> of(String key, String displayName, ArtifactType<T> type) {
    return new ArtifactOutput<>(key, displayName, type);
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
    return Kind.ARTIFACT_OUTPUT;
  }

  /**
   * Returns the type of the produced values.
   *
   * @return artifact type
   */
  public ArtifactType<T> type() {
    return type;
  }

  @Override
  public String toString() {
    return key;
  }
}
