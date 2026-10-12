package feature.prefabs.shaders;

import engine.utils.components.draw.shader.AbstractShader;
import engine.utils.components.draw.shader.ColorGradeShader;
import engine.utils.components.draw.shader.HueRemapShader;
import feature.prefabs.PrefabProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry translating shader classes into their prefab definitions.
 *
 * <p>Prefabs choose which of the registered shaders they allow via {@link
 * PrefabProperty#shaderParameters}; this registry decides which parameters are shown and how the
 * shaders are created.
 */
public final class PrefabShaders {

  private static final Map<Class<? extends AbstractShader>, PrefabShader<?>> BY_CLASS =
      new LinkedHashMap<>();
  private static final Map<String, PrefabShader<?>> BY_ID = new LinkedHashMap<>();

  private static final PrefabProperty<Float> COLOR_GRADE_HUE =
      PrefabProperty.numberSlider("hue", "Hue (<0 = keep)", -1f, -1f, 1f, 0.01f);
  private static final PrefabProperty<Float> COLOR_GRADE_SATURATION =
      PrefabProperty.numberSlider(
          "saturationMultiplier", "Saturation Multiplier", 1f, 0f, 10f, 0.1f);
  private static final PrefabProperty<Float> COLOR_GRADE_VALUE =
      PrefabProperty.numberSlider("valueMultiplier", "Value Multiplier", 1f, 0f, 10f, 0.1f);
  private static final PrefabProperty<Float> COLOR_GRADE_TRANSITION =
      PrefabProperty.numberSlider("transitionSize", "Transition Size", 2f, 0f, 20f, 0.1f);
  private static final PrefabProperty<Boolean> COLOR_GRADE_INVERT =
      PrefabProperty.bool("invert", "Invert", false);

  /** Color grading by overriding the hue and scaling saturation and value. */
  public static final PrefabShader<ColorGradeShader> COLOR_GRADE =
      register(
          new PrefabShader<>(
              "color-grade",
              "Color Grade",
              ColorGradeShader.class,
              List.of(
                  COLOR_GRADE_HUE,
                  COLOR_GRADE_SATURATION,
                  COLOR_GRADE_VALUE,
                  COLOR_GRADE_TRANSITION,
                  COLOR_GRADE_INVERT),
              parameters ->
                  new ColorGradeShader()
                      .hue(parameters.get(COLOR_GRADE_HUE))
                      .saturationMultiplier(parameters.get(COLOR_GRADE_SATURATION))
                      .valueMultiplier(parameters.get(COLOR_GRADE_VALUE))
                      .transitionSize(parameters.get(COLOR_GRADE_TRANSITION))
                      .invert(parameters.get(COLOR_GRADE_INVERT)),
              ColorGradeShader::region));

  private static final PrefabProperty<Float> HUE_REMAP_STARTING_HUE =
      PrefabProperty.numberSlider("startingHue", "Starting Hue", 0f, 0f, 1f, 0.01f);
  private static final PrefabProperty<Float> HUE_REMAP_TARGET_HUE =
      PrefabProperty.numberSlider("targetHue", "Target Hue", 0f, 0f, 1f, 0.01f);
  private static final PrefabProperty<Float> HUE_REMAP_TOLERANCE =
      PrefabProperty.numberSlider("tolerance", "Tolerance", 0.05f, 0f, 0.5f, 0.01f);

  /** Replaces hues close to a starting hue with a target hue. */
  public static final PrefabShader<HueRemapShader> HUE_REMAP =
      register(
          new PrefabShader<>(
              "hue-remap",
              "Hue Remap",
              HueRemapShader.class,
              List.of(HUE_REMAP_STARTING_HUE, HUE_REMAP_TARGET_HUE, HUE_REMAP_TOLERANCE),
              parameters ->
                  new HueRemapShader(
                      parameters.get(HUE_REMAP_STARTING_HUE),
                      parameters.get(HUE_REMAP_TARGET_HUE),
                      parameters.get(HUE_REMAP_TOLERANCE)),
              HueRemapShader::region));

  private PrefabShaders() {}

  /**
   * Registers a shader definition.
   *
   * @param shader definition to register
   * @param <S> shader type
   * @return the registered definition
   * @throws IllegalStateException if the shader class or ID is already registered
   */
  public static synchronized <S extends AbstractShader> PrefabShader<S> register(
      PrefabShader<S> shader) {
    if (BY_CLASS.containsKey(shader.shaderClass())) {
      throw new IllegalStateException(
          "Duplicate prefab shader class: " + shader.shaderClass().getName());
    }
    if (BY_ID.containsKey(shader.id())) {
      throw new IllegalStateException("Duplicate prefab shader ID: " + shader.id());
    }
    BY_CLASS.put(shader.shaderClass(), shader);
    BY_ID.put(shader.id(), shader);
    return shader;
  }

  /**
   * Resolves the definition of a shader class.
   *
   * @param shaderClass shader class
   * @param <S> shader type
   * @return registered definition
   * @throws IllegalArgumentException if the class is not registered
   */
  @SuppressWarnings("unchecked")
  public static synchronized <S extends AbstractShader> PrefabShader<S> require(
      Class<S> shaderClass) {
    PrefabShader<?> shader = BY_CLASS.get(shaderClass);
    if (shader == null) {
      throw new IllegalArgumentException(
          "Shader class is not registered for prefabs: " + shaderClass.getName());
    }
    return (PrefabShader<S>) shader;
  }

  /**
   * Looks up a definition by its serialized ID.
   *
   * @param id shader ID
   * @return registered definition, if any
   */
  public static synchronized Optional<PrefabShader<?>> find(String id) {
    return Optional.ofNullable(BY_ID.get(id));
  }

  /**
   * Returns all registered definitions in registration order.
   *
   * @return registered definitions
   */
  public static synchronized Collection<PrefabShader<?>> all() {
    return Collections.unmodifiableCollection(List.copyOf(BY_ID.values()));
  }
}
