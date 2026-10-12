package feature.prefabs.shaders;

import engine.utils.Rectangle;
import engine.utils.components.draw.shader.AbstractShader;
import feature.prefabs.PrefabProperty;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Immutable selection of a {@link PrefabShader} together with values for all of its parameters.
 *
 * <p>This is the value type of {@link PrefabProperty#shaderParameters} properties.
 */
public final class ShaderParameters {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final PrefabShader<?> shader;
  private final Map<String, JsonNode> values;

  /**
   * Creates validated, normalized parameter values for a shader.
   *
   * @param shader selected shader
   * @param values serialized parameter values; omitted parameters use their defaults
   * @throws IllegalArgumentException if a value is invalid or a key is not a parameter of the
   *     shader
   */
  public ShaderParameters(PrefabShader<?> shader, Map<String, JsonNode> values) {
    this.shader = Objects.requireNonNull(shader, "shader");
    Objects.requireNonNull(values, "values");
    for (String key : values.keySet()) {
      if (shader.parameter(key).isEmpty()) {
        throw new IllegalArgumentException(
            "Unknown parameter '" + key + "' for shader '" + shader.id() + "'");
      }
    }
    Map<String, JsonNode> normalized = new LinkedHashMap<>();
    for (PrefabProperty<?> parameter : shader.parameters()) {
      normalized.put(parameter.key(), parameter.normalize(MAPPER, values.get(parameter.key())));
    }
    this.values = Collections.unmodifiableMap(normalized);
  }

  /**
   * Returns the selected shader.
   *
   * @return shader definition
   */
  public PrefabShader<?> shader() {
    return shader;
  }

  /**
   * Returns the normalized serialized values of every shader parameter, in parameter order.
   *
   * @return immutable parameter values
   */
  public Map<String, JsonNode> values() {
    return values;
  }

  /**
   * Reads a typed parameter value.
   *
   * @param parameter parameter of the selected shader
   * @param <T> parameter value type
   * @return typed parameter value
   */
  public <T> T get(PrefabProperty<T> parameter) {
    requireParameter(parameter);
    return parameter.get(values);
  }

  /**
   * Creates a copy with one parameter replaced.
   *
   * @param parameter parameter of the selected shader
   * @param value new typed value
   * @param <T> parameter value type
   * @return updated parameter values
   */
  public <T> ShaderParameters with(PrefabProperty<T> parameter, T value) {
    requireParameter(parameter);
    Map<String, JsonNode> updated = new LinkedHashMap<>(values);
    updated.put(parameter.key(), parameter.encode(MAPPER, value));
    return new ShaderParameters(shader, updated);
  }

  /**
   * Creates the configured shader.
   *
   * @param region world region to restrict the shader to, or {@code null} for the whole screen
   * @return new shader instance
   */
  public AbstractShader createShader(Rectangle region) {
    return shader.create(this, region);
  }

  private void requireParameter(PrefabProperty<?> parameter) {
    if (shader.parameter(parameter.key()).orElse(null) != parameter) {
      throw new IllegalArgumentException(
          "'" + parameter.key() + "' is not a parameter of shader '" + shader.id() + "'");
    }
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof ShaderParameters that
        && shader == that.shader
        && values.equals(that.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(shader.id(), values);
  }

  @Override
  public String toString() {
    return shader.id() + values;
  }
}
