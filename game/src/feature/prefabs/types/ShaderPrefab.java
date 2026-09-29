package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.systems.DrawSystem;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.components.draw.shader.AbstractShader;
import engine.utils.components.draw.shader.ColorGradeShader;
import engine.utils.components.draw.shader.HueRemapShader;
import engine.utils.components.draw.shader.ShaderList;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.Region;
import feature.prefabs.shaders.PrefabShader;
import feature.prefabs.shaders.ShaderParameters;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Client-side shader applied either to a half-open normalized world region or to the whole screen.
 *
 * <p>The shader is added to the level shaders or the scene shaders of the {@link DrawSystem}. Only
 * shaders that can be restricted to a region are selectable.
 */
public final class ShaderPrefab extends Prefab {

  private static final String TYPE = "shader";
  private static final String DISPLAY_NAME = "Shader";
  private static final PrefabProperty<Boolean> IS_REGION =
      PrefabProperty.bool("isRegion", "Is Region", true);
  private static final PrefabProperty<Boolean> IS_LEVEL =
      PrefabProperty.bool("isLevel", "Is Level", false);
  private static final PrefabProperty<Region> REGION =
      PrefabProperty.region("region", "Region", new Region(new Point(0, 0), new Point(1, 1)))
          .visibleWhen(IS_REGION::get);
  private static final PrefabProperty<ShaderParameters> SHADER =
      PrefabProperty.shaderParameters(
          "shader", "Shader", ColorGradeShader.class, HueRemapShader.class);
  private static final List<PrefabProperty<?>> PROPERTIES =
      List.of(IS_REGION, IS_LEVEL, REGION, SHADER);

  static {
    for (PrefabShader<?> shader : SHADER.shaderChoices()) {
      if (!shader.supportsRegion()) {
        throw new IllegalStateException(
            "Shader prefab only allows region-capable shaders: " + shader.id());
      }
    }
  }

  /*
   * Shader lists are global to DrawSystem, so include a stable ID for the owning level as well as
   * the authored name. Weak keys avoid retaining levels after their shaders have been despawned.
   */
  private static final Map<ILevel, Long> LEVEL_IDS = new WeakHashMap<>();
  private static final Map<ILevel, Map<String, OwnedShader>> OWNED_SHADERS =
      new IdentityHashMap<>();
  private static long nextLevelId;

  private record OwnedShader(ShaderList shaders, String key, AbstractShader shader) {}

  /** Creates the shader definition. */
  public ShaderPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.CLIENT, PROPERTIES);
  }

  /**
   * Creates a bound view for one authored shader instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public ShaderPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.CLIENT, PROPERTIES, level, name);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Rectangle region = null;
    if (value(instance, IS_REGION)) {
      Region authored = value(instance, REGION);
      region = new Rectangle(authored.bottomLeft(), authored.topRight());
    }
    AbstractShader shader = value(instance, SHADER).createShader(region);
    ShaderList shaders =
        value(instance, IS_LEVEL)
            ? DrawSystem.getInstance().levelShaders()
            : DrawSystem.getInstance().sceneShaders();
    String key = shaderKey(context.level(), instance.name());

    synchronized (OWNED_SHADERS) {
      if (!shaders.add(key, shader)) {
        throw new IllegalStateException(
            "Cannot add shader for prefab '"
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
    if (!REGION.isVisible(instance)) return;
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
