package feature.prefabs.types;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.systems.DrawSystem;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Vector2;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.shader.ShaderList;
import engine.utils.components.draw.shader.WaterShader;
import engine.utils.components.path.SimpleIPath;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.PrefabSpawner;
import feature.prefabs.Region;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Client-side water rectangle rendered on a configurable depth layer.
 *
 * <p>All water instances on the same layer of a level share one {@link WaterShader}. Their regions
 * are merged, so the shore only appears where water meets non-water. Color, speed and the other
 * look settings of a layer come from its first water instance in the level file.
 */
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
      List.of(REGION, COLOR, SPEED, LAYER, REPEAT, FOAM_MIN_WIDTH, FOAM_MAX_WIDTH, LINE_INTERVAL);

  private static final int MAX_FIELD_PIXELS = 4096;
  // Squared distance larger than any field can produce, marks water before the transform.
  private static final float INF = 1e20f;
  private static final Map<ILevel, Map<Integer, LayerWater>> LAYERS = new IdentityHashMap<>();
  private static final Map<ILevel, Long> LEVEL_IDS = new WeakHashMap<>();
  private static long nextLevelId;

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

    // The entity only marks the water on its depth layer, the shared layer shader draws it.
    Entity water = context.createEntity(instance.name());
    DrawComponent draw = new DrawComponent(new SimpleIPath("hud/white.png"));
    draw.tintColor(Color.rgba8888(Color.BLACK));
    draw.depth(layer);
    PositionComponent position = new PositionComponent(region.bottomLeft());
    position.scale(Vector2.of(width / draw.getWidth(), height / draw.getHeight()));
    water.add(position);
    water.add(draw);

    WaterSettings settings =
        new WaterSettings(
            new Rectangle(region.bottomLeft(), region.topRight()),
            value(instance, COLOR),
            value(instance, SPEED),
            value(instance, REPEAT),
            value(instance, FOAM_MIN_WIDTH),
            value(instance, FOAM_MAX_WIDTH),
            value(instance, LINE_INTERVAL));
    ILevel level = context.level();
    synchronized (LAYERS) {
      Map<Integer, LayerWater> layers = LAYERS.computeIfAbsent(level, ignored -> new HashMap<>());
      // A changed instance may have moved to another layer.
      for (LayerWater other : layers.values()) {
        if (other.layer != layer && other.instances.remove(instance.name()) != null) {
          scheduleRebuild(level, other);
        }
      }
      LayerWater layerWater = layers.computeIfAbsent(layer, ignored -> newLayerWater(level, layer));
      layerWater.instances.put(instance.name(), settings);
      scheduleRebuild(level, layerWater);
    }
    return List.of(water);
  }

  @Override
  public void onDespawn(PrefabCreationContext context, PrefabInstance instance) {
    synchronized (LAYERS) {
      Map<Integer, LayerWater> layers = LAYERS.get(context.level());
      if (layers == null) return;
      for (LayerWater layerWater : layers.values()) {
        if (layerWater.instances.remove(instance.name()) != null) {
          scheduleRebuild(context.level(), layerWater);
        }
      }
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

  /**
   * Creates the empty shared water state of a depth layer.
   *
   * @param level owning level
   * @param layer depth layer
   * @return new layer state
   */
  private static LayerWater newLayerWater(ILevel level, int layer) {
    long id;
    synchronized (LEVEL_IDS) {
      id = LEVEL_IDS.computeIfAbsent(level, ignored -> nextLevelId++);
    }
    String name = "level-" + id + "-layer-" + layer;
    return new LayerWater(
        layer,
        TYPE + ":" + name,
        "generated/water-field/" + name + ".png",
        DrawSystem.getInstance().entityDepthShaders(layer));
  }

  /**
   * Rebuilds a layer once the current batch of prefab changes is done, so spawning many water
   * instances only rebuilds each layer once.
   *
   * @param level owning level
   * @param layerWater layer state to rebuild
   */
  private static void scheduleRebuild(ILevel level, LayerWater layerWater) {
    PrefabSpawner.afterChanges(
        layerWater,
        () -> {
          synchronized (LAYERS) {
            rebuild(level, layerWater);
          }
        });
  }

  /**
   * Regenerates the shore field of a layer and updates its shared shader, or removes both once no
   * water is left on the layer.
   *
   * @param level owning level
   * @param layerWater layer state to rebuild
   */
  private static void rebuild(ILevel level, LayerWater layerWater) {
    WaterShader shader = layerWater.shader;
    if (layerWater.instances.isEmpty()) {
      if (shader != null && layerWater.shaders.get(layerWater.shaderKey) == shader) {
        layerWater.shaders.remove(layerWater.shaderKey);
      }
      layerWater.shader = null;
      layerWater.fieldRegions = List.of();
      Texture field = TextureMap.instance().remove(layerWater.fieldPath);
      if (field != null) field.dispose();
      Map<Integer, LayerWater> layers = LAYERS.get(level);
      if (layers != null && layers.get(layerWater.layer) == layerWater) {
        layers.remove(layerWater.layer);
        if (layers.isEmpty()) LAYERS.remove(level);
      }
      return;
    }

    // The shore field only depends on the regions, other settings only update the shader.
    List<Rectangle> regions =
        layerWater.instances.values().stream().map(WaterSettings::region).toList();
    if (!regions.equals(layerWater.fieldRegions)
        || !TextureMap.instance().containsKey(layerWater.fieldPath)) {
      layerWater.fieldRegion = buildShoreField(regions, layerWater.fieldPath);
      layerWater.fieldRegions = regions;
    }
    WaterSettings primary = primarySettings(level, layerWater);
    if (shader == null) {
      shader = new WaterShader();
      layerWater.shader = shader;
    }
    shader
        .region(layerWater.fieldRegion)
        .shoreField(layerWater.fieldPath)
        .color(primary.color())
        .speed(primary.speed())
        .repeat(primary.repeat())
        .foamMinWidth(primary.foamMinWidth())
        .foamMaxWidth(primary.foamMaxWidth())
        .lineInterval(primary.lineInterval());
    if (layerWater.shaders.get(layerWater.shaderKey) != shader
        && !layerWater.shaders.add(layerWater.shaderKey, shader)) {
      throw new IllegalStateException(
          "Cannot add water shader: shader key collision '" + layerWater.shaderKey + "'");
    }
  }

  /**
   * Returns the settings of the water instance on the layer that comes first in the level file, so
   * the look of a layer does not change when other instances are edited.
   *
   * @param level owning level
   * @param layerWater layer state
   * @return settings used for the whole layer
   */
  private static WaterSettings primarySettings(ILevel level, LayerWater layerWater) {
    for (PrefabInstance authored : level.prefabs()) {
      WaterSettings settings = layerWater.instances.get(authored.name());
      if (settings != null) return settings;
    }
    return layerWater.instances.values().iterator().next();
  }

  /**
   * Generates the shore field texture of a layer from the union of its water regions and registers
   * it in the texture map. Each pixel stores the distance from its center to the center of the
   * nearest non-water pixel.
   *
   * @param regions water regions of the layer
   * @param fieldPath texture map path to register the shore field at
   * @return world region covered by the generated texture
   */
  private static Rectangle buildShoreField(List<Rectangle> regions, String fieldPath) {
    float minX = Float.MAX_VALUE;
    float minY = Float.MAX_VALUE;
    float maxX = -Float.MAX_VALUE;
    float maxY = -Float.MAX_VALUE;
    for (Rectangle r : regions) {
      minX = Math.min(minX, r.x());
      minY = Math.min(minY, r.y());
      maxX = Math.max(maxX, r.x() + r.width());
      maxY = Math.max(maxY, r.y() + r.height());
    }

    // Use the water pixel grid, halving the resolution only if the texture would get too big.
    int pixelsPerTile = WaterShader.PIXELS_PER_TILE;
    while (pixelsPerTile > 1
        && Math.max(maxX - minX, maxY - minY) * pixelsPerTile + 2 > MAX_FIELD_PIXELS) {
      pixelsPerTile /= 2;
    }
    float waterPixelsPerFieldPixel = (float) WaterShader.PIXELS_PER_TILE / pixelsPerTile;

    // One pixel of land around the union guarantees a shore at the outer edges.
    int originX = (int) Math.floor(minX * pixelsPerTile) - 1;
    int originY = (int) Math.floor(minY * pixelsPerTile) - 1;
    int width = (int) Math.ceil(maxX * pixelsPerTile) + 1 - originX;
    int height = (int) Math.ceil(maxY * pixelsPerTile) + 1 - originY;

    float[] distances = new float[width * height];
    for (Rectangle r : regions) {
      int x0 = firstPixelCenterAtOrAfter(r.x() * pixelsPerTile - originX);
      int x1 = firstPixelCenterAtOrAfter((r.x() + r.width()) * pixelsPerTile - originX);
      int y0 = firstPixelCenterAtOrAfter(r.y() * pixelsPerTile - originY);
      int y1 = firstPixelCenterAtOrAfter((r.y() + r.height()) * pixelsPerTile - originY);
      for (int y = Math.max(y0, 0); y < Math.min(y1, height); y++) {
        Arrays.fill(distances, y * width + Math.max(x0, 0), y * width + Math.min(x1, width), INF);
      }
    }
    squaredDistanceTransform(distances, width, height);

    Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
    ByteBuffer pixels = pixmap.getPixels();
    float scale = 255f / WaterShader.SHORE_FIELD_MAX_PIXELS;
    for (int i = 0; i < distances.length; i++) {
      float value = 0f;
      if (distances[i] > 0f) {
        // Convert to water pixels while keeping the shore half a water pixel from the center.
        float fieldPixels = (float) Math.sqrt(distances[i]);
        value = (fieldPixels - 0.5f) * waterPixelsPerFieldPixel + 0.5f;
      }
      int encoded = Math.round(Math.min(value, WaterShader.SHORE_FIELD_MAX_PIXELS) * scale);
      // Pixmap rows are uploaded bottom-up in texture space, matching world y.
      pixels.put(i * 4, (byte) encoded);
      pixels.put(i * 4 + 1, (byte) 0);
      pixels.put(i * 4 + 2, (byte) 0);
      pixels.put(i * 4 + 3, (byte) 255);
    }
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    texture.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
    TextureMap.instance().putTexture(new SimpleIPath(fieldPath), texture);

    return new Rectangle(
        (float) width / pixelsPerTile,
        (float) height / pixelsPerTile,
        (float) originX / pixelsPerTile,
        (float) originY / pixelsPerTile);
  }

  /**
   * Returns the index of the first pixel whose center lies at or after a pixel grid coordinate.
   *
   * @param gridCoordinate coordinate in field pixels
   * @return pixel index
   */
  private static int firstPixelCenterAtOrAfter(float gridCoordinate) {
    return (int) Math.ceil(gridCoordinate - 0.5f);
  }

  /**
   * Replaces every non-zero value with the squared Euclidean distance to the nearest zero value
   * (Felzenszwalb and Huttenlocher), first along columns, then along rows.
   *
   * @param grid row-major values, 0 for land and {@link #INF} for water
   * @param width grid width
   * @param height grid height
   */
  private static void squaredDistanceTransform(float[] grid, int width, int height) {
    int size = Math.max(width, height);
    float[] line = new float[size];
    float[] result = new float[size];
    int[] parabolas = new int[size];
    float[] bounds = new float[size + 1];
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) line[y] = grid[y * width + x];
      distanceTransform1d(line, height, result, parabolas, bounds);
      for (int y = 0; y < height; y++) grid[y * width + x] = result[y];
    }
    for (int y = 0; y < height; y++) {
      System.arraycopy(grid, y * width, line, 0, width);
      distanceTransform1d(line, width, result, parabolas, bounds);
      System.arraycopy(result, 0, grid, y * width, width);
    }
  }

  /**
   * One dimensional squared distance transform of a sampled function.
   *
   * @param f sampled function
   * @param n number of samples
   * @param d output squared distances
   * @param v scratch buffer for parabola locations
   * @param z scratch buffer for parabola boundaries
   */
  private static void distanceTransform1d(float[] f, int n, float[] d, int[] v, float[] z) {
    int k = 0;
    v[0] = 0;
    z[0] = -Float.MAX_VALUE;
    z[1] = Float.MAX_VALUE;
    for (int q = 1; q < n; q++) {
      float s = intersection(f, q, v[k]);
      while (s <= z[k]) {
        k--;
        s = intersection(f, q, v[k]);
      }
      k++;
      v[k] = q;
      z[k] = s;
      z[k + 1] = Float.MAX_VALUE;
    }
    k = 0;
    for (int q = 0; q < n; q++) {
      while (z[k + 1] < q) k++;
      float dq = q - v[k];
      d[q] = dq * dq + f[v[k]];
    }
  }

  /**
   * Returns where the parabolas rooted at two samples intersect.
   *
   * @param f sampled function
   * @param q first sample
   * @param p second sample
   * @return intersection position
   */
  private static float intersection(float[] f, int q, int p) {
    return ((f[q] + (float) q * q) - (f[p] + (float) p * p)) / (2f * q - 2f * p);
  }

  /**
   * Settings of a single authored water region.
   *
   * @param region water bounds
   * @param color water color
   * @param speed wave speed
   * @param repeat wave pattern scale
   * @param foamMinWidth minimum foam rim width in pixels
   * @param foamMaxWidth maximum foam rim width in pixels
   * @param lineInterval seconds between shore foam lines
   */
  private record WaterSettings(
      Rectangle region,
      Color color,
      float speed,
      float repeat,
      int foamMinWidth,
      int foamMaxWidth,
      float lineInterval) {}

  /** Shared water state of one depth layer in one level. */
  private static final class LayerWater {
    private final int layer;
    private final String shaderKey;
    private final String fieldPath;
    private final ShaderList shaders;
    private final Map<String, WaterSettings> instances = new LinkedHashMap<>();
    private WaterShader shader;
    private List<Rectangle> fieldRegions = List.of();
    private Rectangle fieldRegion;

    /**
     * Creates the empty state of a layer.
     *
     * @param layer depth layer
     * @param shaderKey key of the shared shader in the layer shader list
     * @param fieldPath texture map path of the shore field
     * @param shaders shader list of the depth layer
     */
    private LayerWater(int layer, String shaderKey, String fieldPath, ShaderList shaders) {
      this.layer = layer;
      this.shaderKey = shaderKey;
      this.fieldPath = fieldPath;
      this.shaders = shaders;
    }
  }
}
