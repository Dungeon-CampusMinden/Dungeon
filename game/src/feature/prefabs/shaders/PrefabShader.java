package feature.prefabs.shaders;

import engine.utils.Rectangle;
import engine.utils.components.draw.shader.AbstractShader;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabPropertyType;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Describes how a shader class is configured and created by prefabs.
 *
 * <p>A definition lists the editable parameters of the shader and knows how to turn their values
 * into a configured shader instance. Parameters use regular {@link PrefabProperty} descriptors, so
 * the level editor renders them with the same controls as prefab properties. World-space properties
 * (points and regions) and nested shader parameters are not supported as shader parameters.
 *
 * @param <S> shader type
 */
public final class PrefabShader<S extends AbstractShader> {

  private static final Set<PrefabPropertyType> UNSUPPORTED_PARAMETER_TYPES =
      Set.of(
          PrefabPropertyType.POINT,
          PrefabPropertyType.REGION,
          PrefabPropertyType.SHADER_PARAMETERS);

  private final String id;
  private final String displayName;
  private final Class<S> shaderClass;
  private final List<PrefabProperty<?>> parameters;
  private final Map<String, PrefabProperty<?>> parametersByKey;
  private final Function<ShaderParameters, S> factory;
  private final BiConsumer<S, Rectangle> regionApplier;
  private ShaderParameters defaults;

  /**
   * Creates a shader definition.
   *
   * @param id stable serialized shader ID
   * @param displayName editor-facing shader name
   * @param shaderClass created shader class
   * @param parameters ordered editable shader parameters
   * @param factory creates a shader configured from parameter values
   * @param regionApplier restricts a created shader to a world region, where {@code null} means the
   *     whole screen; {@code null} if the shader cannot be restricted to a region
   */
  public PrefabShader(
      String id,
      String displayName,
      Class<S> shaderClass,
      List<PrefabProperty<?>> parameters,
      Function<ShaderParameters, S> factory,
      BiConsumer<S, Rectangle> regionApplier) {
    this.id = requireText(id, "id");
    this.displayName = requireText(displayName, "displayName");
    this.shaderClass = Objects.requireNonNull(shaderClass, "shaderClass");
    this.parameters = List.copyOf(parameters);
    this.factory = Objects.requireNonNull(factory, "factory");
    this.regionApplier = regionApplier;
    Map<String, PrefabProperty<?>> byKey = new LinkedHashMap<>();
    for (PrefabProperty<?> parameter : this.parameters) {
      if (UNSUPPORTED_PARAMETER_TYPES.contains(parameter.type())) {
        throw new IllegalArgumentException(
            "Shader parameter '" + parameter.key() + "' has unsupported type " + parameter.type());
      }
      if (byKey.putIfAbsent(parameter.key(), parameter) != null) {
        throw new IllegalArgumentException(
            "Duplicate parameter '" + parameter.key() + "' in shader " + id);
      }
    }
    this.parametersByKey = Map.copyOf(byKey);
  }

  /**
   * Returns the stable serialized shader ID.
   *
   * @return shader ID
   */
  public String id() {
    return id;
  }

  /**
   * Returns the editor-facing shader name.
   *
   * @return display name
   */
  public String displayName() {
    return displayName;
  }

  /**
   * Returns the shader class created by this definition.
   *
   * @return shader class
   */
  public Class<S> shaderClass() {
    return shaderClass;
  }

  /**
   * Returns the ordered editable parameters of this shader.
   *
   * @return parameter descriptors
   */
  public List<PrefabProperty<?>> parameters() {
    return parameters;
  }

  /**
   * Looks up a parameter by its serialized key.
   *
   * @param key parameter key
   * @return matching parameter, if this shader has it
   */
  public Optional<PrefabProperty<?>> parameter(String key) {
    return Optional.ofNullable(parametersByKey.get(key));
  }

  /**
   * Returns whether created shaders can be restricted to a world region.
   *
   * @return true if {@link #create(ShaderParameters, Rectangle)} accepts a region
   */
  public boolean supportsRegion() {
    return regionApplier != null;
  }

  /**
   * Returns parameter values populated with every parameter default.
   *
   * @return default parameter values
   */
  public ShaderParameters defaults() {
    if (defaults == null) defaults = new ShaderParameters(this, Map.of());
    return defaults;
  }

  /**
   * Creates a configured shader.
   *
   * @param parameters parameter values for this shader
   * @param region world region to restrict the shader to, or {@code null} for the whole screen
   * @return new shader instance
   * @throws IllegalArgumentException if the parameters belong to another shader or a region is
   *     given for a shader that does not support regions
   */
  public S create(ShaderParameters parameters, Rectangle region) {
    Objects.requireNonNull(parameters, "parameters");
    if (parameters.shader() != this) {
      throw new IllegalArgumentException(
          "Shader parameters for '" + parameters.shader().id() + "' cannot create '" + id + "'");
    }
    S shader = factory.apply(parameters);
    if (regionApplier != null) {
      regionApplier.accept(shader, region);
    } else if (region != null) {
      throw new IllegalArgumentException("Shader '" + id + "' does not support regions");
    }
    return shader;
  }

  @Override
  public String toString() {
    return displayName;
  }

  private static String requireText(String value, String field) {
    Objects.requireNonNull(value, field);
    if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
    return value;
  }
}
