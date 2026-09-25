package feature.prefabs.types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import engine.utils.components.draw.TextureGenerator;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.shader.WaterShader;
import engine.utils.components.path.SimpleIPath;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.Region;
import java.util.List;

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
  private static final PrefabProperty<Integer> FOAM_MIN_WIDTH =
      PrefabProperty.integer("foamMinWidth", "Foam Min", 1, 0, 32);
  private static final PrefabProperty<Integer> FOAM_MAX_WIDTH =
      PrefabProperty.integer("foamMaxWidth", "Foam Max", 3, 0, 32);
  private static final PrefabProperty<Float> LINE_INTERVAL =
      PrefabProperty.floating("lineInterval", "Shore Line Interval (s)", 2.5f, 0.1f, 60f);
  private static final List<PrefabProperty<?>> PROPERTIES =
      List.of(
          REGION,
          COLOR,
          SPEED,
          LAYER,
          REPEAT,
          FOAM_MIN_WIDTH,
          FOAM_MAX_WIDTH,
          LINE_INTERVAL);

  private static final String SHADER_KEY = "water";
  private static final int PIXELS_PER_TILE = 16;
  private static final int MAX_CANVAS_PIXELS = 4096;

  /** Creates the water prefab definition. */
  public WaterPrefab() {
    super(TYPE, "Water", PrefabSide.CLIENT, PROPERTIES);
  }

  /**
   * Creates a bound view of an authored water instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public WaterPrefab(ILevel level, String name) {
    super(TYPE, "Water", PrefabSide.CLIENT, PROPERTIES, level, name);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Region region = value(instance, REGION);
    int layer = value(instance, LAYER);
    float width = region.topRight().x() - region.bottomLeft().x();
    float height = region.topRight().y() - region.bottomLeft().y();
    if (width == 0f || height == 0f) return List.of();

    Entity water = context.createEntity(instance.name());
    DrawComponent draw = new DrawComponent(new Animation(new SimpleIPath(canvasTexture(width, height))));
    draw.depth(layer);
    PositionComponent position = new PositionComponent(region.bottomLeft());
    position.scale(Vector2.of(width / draw.getWidth(), height / draw.getHeight()));
    water.add(position);
    water.add(draw);

    draw.shaders()
        .add(
            SHADER_KEY,
            new WaterShader()
                .region(new Rectangle(region.bottomLeft(), region.topRight()))
                .color(value(instance, COLOR))
                .speed(value(instance, SPEED))
                .repeat(value(instance, REPEAT))
                .foamMinWidth(value(instance, FOAM_MIN_WIDTH))
                .foamMaxWidth(value(instance, FOAM_MAX_WIDTH))
                .lineInterval(value(instance, LINE_INTERVAL)));
    return List.of(water);
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

  /**
   * Returns the path of a white texture whose pixel size matches the water region. Entity shaders
   * render at sprite resolution, so this keeps the water on the same pixel grid as the tiles.
   *
   * @param width region width in world units
   * @param height region height in world units
   * @return path of the registered canvas texture
   */
  private static String canvasTexture(float width, float height) {
    int pixelWidth = canvasPixels(width);
    int pixelHeight = canvasPixels(height);
    String path = "generated/water_" + pixelWidth + "x" + pixelHeight + ".png";
    if (!TextureMap.instance().containsKey(path)) {
      TextureGenerator.registerGenerateColorTexture(path, pixelWidth, pixelHeight, Color.WHITE);
    }
    return path;
  }

  private static int canvasPixels(float worldSize) {
    return MathUtils.clamp(Math.round(Math.abs(worldSize) * PIXELS_PER_TILE), 1, MAX_CANVAS_PIXELS);
  }
}
