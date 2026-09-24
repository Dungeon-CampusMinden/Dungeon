package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.systems.DrawSystem;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.components.draw.shader.ColorGradeShader;
import engine.utils.components.draw.shader.ShaderList;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.Region;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Client-side color grading applied to a half-open normalized world region. */
public final class ColorGradeRegionPrefab extends Prefab {

  private static final String TYPE = "shader-region";
  private static final PrefabProperty<Region> REGION =
      PrefabProperty.region("region", "Region", new Region(new Point(0, 0), new Point(1, 1)));
  private static final PrefabProperty<Float> HUE =
      PrefabProperty.numberSlider("hue", "Hue", -1f, -1f, 1f, 0.01f);
  private static final PrefabProperty<Float> SATURATION_MULT =
      PrefabProperty.numberSlider("saturationMult", "Saturation Multiplier", 1f, 0f, 10f, 0.1f);
  private static final PrefabProperty<Float> VALUE_MULT =
      PrefabProperty.numberSlider("valueMult", "Value Multiplier", 1f, 0f, 10f, 0.1f);
  private static final PrefabProperty<Float> TRANSITION_SIZE =
      PrefabProperty.numberSlider("transitionSize", "Transition Size", 2f, 0f, 20f, 0.1f);
  private static final PrefabProperty<Boolean> SCENE =
      PrefabProperty.bool("scene", "Scene", false);

  /*
   * Shader lists are global to DrawSystem, so include a stable ID for the owning level as well as
   * the authored name. Weak keys avoid retaining levels after their shaders have been despawned.
   */
  private static final Map<ILevel, Long> LEVEL_IDS = new WeakHashMap<>();
  private static final Map<ILevel, Map<String, OwnedShader>> OWNED_SHADERS =
      new IdentityHashMap<>();
  private static long nextLevelId;

  private record OwnedShader(ShaderList shaders, String key, ColorGradeShader shader) {}

  /** Creates the color-grade region definition. */
  public ColorGradeRegionPrefab() {
    super(
        TYPE,
        "Color Grade Region",
        PrefabSide.CLIENT,
        List.of(REGION, HUE, SATURATION_MULT, VALUE_MULT, TRANSITION_SIZE, SCENE));
  }

  /**
   * Creates a bound view for one authored color-grade region.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public ColorGradeRegionPrefab(ILevel level, String name) {
    super(
        TYPE,
        "Color Grade Region",
        PrefabSide.CLIENT,
        List.of(REGION, HUE, SATURATION_MULT, VALUE_MULT, TRANSITION_SIZE, SCENE),
        level,
        name);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Region region = value(instance, REGION);
    boolean scene = value(instance, SCENE);
    ColorGradeShader shader =
        new ColorGradeShader()
            .region(new Rectangle(region.bottomLeft(), region.topRight()))
            .hue(value(instance, HUE))
            .saturationMultiplier(value(instance, SATURATION_MULT))
            .valueMultiplier(value(instance, VALUE_MULT))
            .transitionSize(value(instance, TRANSITION_SIZE));
    ShaderList shaders =
        scene ? DrawSystem.getInstance().sceneShaders() : DrawSystem.getInstance().levelShaders();
    String key = shaderKey(context.level(), instance.name());

    synchronized (OWNED_SHADERS) {
      if (!shaders.add(key, shader)) {
        throw new IllegalStateException(
            "Cannot add color-grade shader for prefab '"
                + instance.name()
                + "': shader key collision '"
                + key
                + "'");
      }
      OWNED_SHADERS
          .computeIfAbsent(context.level(), ignored -> new HashMap<>())
          .put(instance.name(), new OwnedShader(shaders, key, shader));
    }
    return List.of();
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    synchronized (OWNED_SHADERS) {
      Map<String, OwnedShader> byName = OWNED_SHADERS.get(context.level());
      if (byName == null) return;
      OwnedShader owned = byName.remove(instance.name());
      if (owned != null && owned.shaders().get(owned.key()) == owned.shader()) {
        owned.shaders().remove(owned.key());
      }
      if (byName.isEmpty()) OWNED_SHADERS.remove(context.level());
    }
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Region region = value(instance, REGION);
    feedback.point(region.bottomLeft(), null);
    feedback.point(region.topRight(), null);
    feedback.rectangle(region.bottomLeft(), region.topRight());
    feedback.label(midpoint(region.bottomLeft(), region.topRight()), instance.name());
  }

  private static Point midpoint(Point first, Point second) {
    return new Point(first.x() * 0.5f + second.x() * 0.5f, first.y() * 0.5f + second.y() * 0.5f);
  }

  private static String shaderKey(ILevel level, String instanceName) {
    synchronized (LEVEL_IDS) {
      long levelId = LEVEL_IDS.computeIfAbsent(level, ignored -> nextLevelId++);
      return TYPE + ":level-" + levelId + ":" + instanceName;
    }
  }
}
