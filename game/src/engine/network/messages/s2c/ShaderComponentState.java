package engine.network.messages.s2c;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable network representation of a synchronized shader component.
 *
 * @param shaders ordered shader declarations
 */
public record ShaderComponentState(List<ShaderEntryState> shaders) {

  /**
   * Creates an immutable, order-normalized shader component state.
   *
   * @param shaders shader declarations
   */
  public ShaderComponentState {
    Objects.requireNonNull(shaders, "shaders");
    List<ShaderEntryState> normalized = new ArrayList<>(shaders);
    Set<String> identifiers = new HashSet<>();
    Set<Integer> orders = new HashSet<>();
    for (ShaderEntryState shader : normalized) {
      Objects.requireNonNull(shader, "shader");
      if (!identifiers.add(shader.identifier())) {
        throw new IllegalArgumentException("Duplicate shader identifier: " + shader.identifier());
      }
      if (!orders.add(shader.order())) {
        throw new IllegalArgumentException("Duplicate shader order: " + shader.order());
      }
    }
    normalized.sort(Comparator.comparingInt(ShaderEntryState::order));
    shaders = List.copyOf(normalized);
  }

  /** Creates an empty shader component state. */
  public ShaderComponentState() {
    this(List.of());
  }

  /**
   * One immutable network shader declaration.
   *
   * @param identifier stable component-local identifier
   * @param order render order
   * @param type stable shader type identifier
   * @param enabled whether the shader is enabled
   * @param upscaling minimum render-target upscaling factor
   * @param properties type-specific configuration
   */
  public record ShaderEntryState(
      String identifier,
      int order,
      String type,
      boolean enabled,
      int upscaling,
      Map<String, String> properties) {

    /**
     * Creates a validated shader declaration.
     *
     * @param identifier stable component-local identifier
     * @param order render order
     * @param type stable shader type identifier
     * @param enabled whether the shader is enabled
     * @param upscaling minimum render-target upscaling factor
     * @param properties type-specific configuration
     */
    public ShaderEntryState {
      if (identifier == null || identifier.isBlank()) {
        throw new IllegalArgumentException("Shader identifier must not be blank");
      }
      if (type == null || type.isBlank()) {
        throw new IllegalArgumentException("Shader type must not be blank");
      }
      if (upscaling < 1) {
        throw new IllegalArgumentException("Shader upscaling must be at least 1");
      }
      properties = Map.copyOf(Objects.requireNonNull(properties, "properties"));
    }
  }

  /**
   * Creates state from a collection of entries.
   *
   * @param shaders shader declarations
   * @return immutable state
   */
  public static ShaderComponentState of(Collection<ShaderEntryState> shaders) {
    return new ShaderComponentState(shaders.stream().toList());
  }
}
