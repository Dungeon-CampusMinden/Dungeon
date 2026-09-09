package feature.shader;

import engine.Component;
import engine.utils.components.draw.shader.AbstractShader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Describes the shaders that should be applied to an entity.
 *
 * <p>The component owns the ordered shader declarations. {@link ShaderSystem} projects these
 * declarations into the entity's runtime {@code DrawComponent}.
 */
public final class ShaderComponent implements Component {

  /**
   * One named shader declaration and its render order.
   *
   * @param identifier shader identifier
   * @param order render order
   * @param shader runtime shader
   */
  public record ShaderEntry(String identifier, int order, AbstractShader shader) {
    /**
     * Creates a shader declaration.
     *
     * @param identifier shader identifier
     * @param order render order
     * @param shader runtime shader
     */
    public ShaderEntry(String identifier, int order, AbstractShader shader) {
      if (identifier == null || identifier.isBlank()) {
        throw new IllegalArgumentException("Shader identifier must not be blank");
      }
      Objects.requireNonNull(shader, "shader");
      this.identifier = identifier;
      this.order = order;
      this.shader = shader;
    }
  }

  private final List<ShaderEntry> shaders;

  /**
   * Creates a shader component.
   *
   * @param shaders shader declarations, in their desired order
   */
  public ShaderComponent(Collection<ShaderEntry> shaders) {
    Objects.requireNonNull(shaders, "shaders");

    List<ShaderEntry> normalized = new ArrayList<>(shaders);
    Set<String> identifiers = new HashSet<>();
    Set<Integer> orders = new HashSet<>();
    for (ShaderEntry shader : normalized) {
      Objects.requireNonNull(shader, "shader");
      if (!identifiers.add(shader.identifier())) {
        throw new IllegalArgumentException("Duplicate shader identifier: " + shader.identifier());
      }
      if (!orders.add(shader.order())) {
        throw new IllegalArgumentException("Duplicate shader order: " + shader.order());
      }
    }

    normalized.sort(Comparator.comparingInt(ShaderEntry::order));
    this.shaders = List.copyOf(normalized);
  }

  /**
   * Creates a shader component from declarations.
   *
   * @param shaders shader declarations
   */
  public ShaderComponent(ShaderEntry... shaders) {
    this(List.of(shaders));
  }

  /**
   * Creates a shader component with one shader declaration.
   *
   * @param identifier shader identifier
   * @param order render order
   * @param shader runtime shader
   */
  public ShaderComponent(String identifier, int order, AbstractShader shader) {
    this(new ShaderEntry(identifier, order, shader));
  }

  /**
   * Returns the ordered shader declarations.
   *
   * @return immutable shader declarations
   */
  public List<ShaderEntry> shaders() {
    return shaders;
  }

  /**
   * Returns a copy with the supplied shader declarations.
   *
   * @param values replacement shader declarations
   * @return updated component
   */
  public ShaderComponent withShaders(Collection<ShaderEntry> values) {
    return new ShaderComponent(values);
  }

  /**
   * Returns a copy with one shader declaration added or replaced.
   *
   * @param identifier shader identifier
   * @param order render order
   * @param shader runtime shader
   * @return updated component
   */
  public ShaderComponent withShader(String identifier, int order, AbstractShader shader) {
    List<ShaderEntry> values = new ArrayList<>(shaders);
    values.removeIf(existing -> existing.identifier().equals(identifier));
    values.add(new ShaderEntry(identifier, order, shader));
    return new ShaderComponent(values);
  }

  /**
   * Returns a copy without the shader with the supplied identifier.
   *
   * @param identifier shader identifier
   * @return updated component
   */
  public ShaderComponent withoutShader(String identifier) {
    return new ShaderComponent(
        shaders.stream().filter(shader -> !shader.identifier().equals(identifier)).toList());
  }
}
