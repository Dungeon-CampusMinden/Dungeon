package feature.prefabs.types;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.systems.DrawSystem;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import engine.utils.components.draw.shader.ShaderList;
import engine.utils.components.draw.shader.WaterShader;
import engine.utils.components.path.SimpleIPath;
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

/** Client-side water rectangle rendered on a configurable depth layer. */
public final class WaterPrefab extends Prefab {

  private static final String TYPE = "water";
  private static final PrefabProperty<Region> REGION =
      PrefabProperty.region("region", "Region", new Region(new Point(0, 0), new Point(1, 1)));
  private static final PrefabProperty<Color> COLOR =
      PrefabProperty.color("color", "Color", new Color(0.1f, 0.4f, 0.7f, 1f));
  private static final PrefabProperty<Float> SPEED =
      PrefabProperty.numberSlider("speed", "Speed", 0.15f, 0f, 2f, 0.01f);
  private static final PrefabProperty<Integer> LAYER =
      PrefabProperty.integer("layer", "Layer", -50, Integer.MIN_VALUE, Integer.MAX_VALUE);
  private static final PrefabProperty<Float> REPEAT =
      PrefabProperty.floating("repeat", "Repeat", 1f, 0.1f, 64f);

  private static final Map<ILevel, Long> LEVEL_IDS = new WeakHashMap<>();
  private static final Map<ILevel, Map<String, OwnedShader>> OWNED_SHADERS =
      new IdentityHashMap<>();
  private static long nextLevelId;

  private record OwnedShader(ShaderList shaders, String key, WaterShader shader) {}

  /** Creates the water prefab definition. */
  public WaterPrefab() {
    super(TYPE, "Water", PrefabSide.CLIENT, List.of(REGION, COLOR, SPEED, LAYER, REPEAT));
  }

  /**
   * Creates a bound view of an authored water instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public WaterPrefab(ILevel level, String name) {
    super(
        TYPE,
        "Water",
        PrefabSide.CLIENT,
        List.of(REGION, COLOR, SPEED, LAYER, REPEAT),
        level,
        name);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Region region = value(instance, REGION);
    int layer = value(instance, LAYER);
    float width = region.topRight().x() - region.bottomLeft().x();
    float height = region.topRight().y() - region.bottomLeft().y();
    if (width == 0f || height == 0f) return List.of();

    Entity water = context.createEntity(instance.name());
    DrawComponent draw = new DrawComponent(new SimpleIPath("hud/white.png"));
    draw.tintColor(Color.rgba8888(Color.BLACK));
    draw.depth(layer);
    PositionComponent position = new PositionComponent(region.bottomLeft());
    position.scale(Vector2.of(width / draw.getWidth(), height / draw.getHeight()));
    water.add(position);
    water.add(draw);

    WaterShader shader =
        new WaterShader()
            .region(new Rectangle(region.bottomLeft(), region.topRight()))
            .color(value(instance, COLOR))
            .speed(value(instance, SPEED))
            .repeat(value(instance, REPEAT));
    ShaderList shaders = DrawSystem.getInstance().entityDepthShaders(layer);
    String key = shaderKey(context.level(), instance.name());
    synchronized (OWNED_SHADERS) {
      if (!shaders.add(key, shader)) {
        throw new IllegalStateException(
            "Cannot add water shader: shader key collision '" + key + "'");
      }
      OWNED_SHADERS
          .computeIfAbsent(context.level(), ignored -> new HashMap<>())
          .put(instance.name(), new OwnedShader(shaders, key, shader));
    }
    return List.of(water);
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
    feedback.label(
        new Point(
            (region.bottomLeft().x() + region.topRight().x()) * 0.5f,
            (region.bottomLeft().y() + region.topRight().y()) * 0.5f),
        instance.name());
  }

  private static String shaderKey(ILevel level, String name) {
    synchronized (LEVEL_IDS) {
      long id = LEVEL_IDS.computeIfAbsent(level, ignored -> nextLevelId++);
      return TYPE + ":level-" + id + ":" + name;
    }
  }
}
