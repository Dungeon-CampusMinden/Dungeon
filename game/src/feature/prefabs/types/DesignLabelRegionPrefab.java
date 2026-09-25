package feature.prefabs.types;

import engine.Entity;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.elements.ILevel;
import engine.level.elements.tile.PitTile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.level.utils.TileTextureFactory;
import engine.utils.Point;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabRegistry;
import feature.prefabs.PrefabSide;
import feature.prefabs.PrefabSpawner;
import feature.prefabs.Region;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Client-side design override for tiles inside a half-open world region.
 *
 * <p>Region corners are normalized. Integer tile coordinates on the bottom-left bounds are included
 * and coordinates on the top-right bounds are excluded. If regions overlap, the later authored
 * active region wins.
 */
public final class DesignLabelRegionPrefab extends Prefab {

  private static final String TYPE = "design-label-region";
  private static final PrefabProperty<Region> REGION =
      PrefabProperty.region("region", "Region", new Region(new Point(0, 0), new Point(1, 1)));
  private static final PrefabProperty<String> DESIGN_LABEL =
      PrefabProperty.selection(
          "designLabel",
          "Design Label",
          DesignLabel.DEFAULT.name(),
          Arrays.stream(DesignLabel.values()).map(Enum::name).toList());

  /** Creates the design-label region definition. */
  public DesignLabelRegionPrefab() {
    super(TYPE, "Design Label Region", PrefabSide.CLIENT, List.of(REGION, DESIGN_LABEL));
  }

  /**
   * Creates a bound view for one authored design-label region.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public DesignLabelRegionPrefab(ILevel level, String name) {
    super(
        TYPE, "Design Label Region", PrefabSide.CLIENT, List.of(REGION, DESIGN_LABEL), level, name);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    return List.of();
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

  /**
   * Recomputes tile designs from the persistent level design and currently spawned client regions.
   *
   * <p>This method is intentionally stateless: no tile's currently overridden label is ever used as
   * a new base, and all winners are resolved in authored order on every refresh. Only tiles whose
   * design changes get their texture resolved again.
   *
   * @param level owning level
   */
  public static void refresh(ILevel level) {
    DesignLabel base =
        level instanceof DungeonLevel dungeonLevel
            ? dungeonLevel.baseDesignLabel()
            : level.designLabel().orElse(DesignLabel.DEFAULT);
    Tile[][] layout = level.layout();
    if (layout.length == 0) return;
    int height = layout.length;
    int width = layout[0].length;
    DesignLabel[][] targets = new DesignLabel[height][width];
    for (DesignLabel[] row : targets) Arrays.fill(row, base);

    for (PrefabInstance authored : level.prefabs()) {
      if (!TYPE.equals(authored.type())
          || !PrefabSpawner.isActive(level, PrefabSide.CLIENT, authored.name(), TYPE)) {
        continue;
      }
      PrefabInstance normalized = PrefabRegistry.require(TYPE).normalize(authored);
      Region region = REGION.get(normalized);
      DesignLabel selected = DesignLabel.valueOf(DESIGN_LABEL.get(normalized));
      // Integer tile coordinates inside [bottomLeft, topRight).
      int x0 = Math.max((int) Math.ceil(region.bottomLeft().x()), 0);
      int y0 = Math.max((int) Math.ceil(region.bottomLeft().y()), 0);
      int x1 = Math.min((int) Math.ceil(region.topRight().x()), width);
      int y1 = Math.min((int) Math.ceil(region.topRight().y()), height);
      for (int y = y0; y < y1; y++) {
        for (int x = x0; x < x1; x++) targets[y][x] = selected;
      }
    }

    List<Tile> changed = new ArrayList<>();
    for (Tile[] row : layout) {
      for (Tile tile : row) {
        if (tile == null) continue;
        DesignLabel target = targets[tile.coordinate().y()][tile.coordinate().x()];
        if (tile.designLabel() != target) {
          tile.designLabel(target);
          changed.add(tile);
        }
      }
    }
    if (changed.isEmpty()) return;

    // The element layout is the same for every tile, so derive it only once.
    LevelElement[][] elements = TileTextureFactory.levelElementLayout(layout);
    Set<Tile> refreshed = Collections.newSetFromMap(new IdentityHashMap<>());
    for (Tile tile : changed) {
      refreshTexture(tile, layout, elements, refreshed);
      // Open pits resolve their texture from the tile above them.
      int x = tile.coordinate().x();
      for (int y = tile.coordinate().y() - 1; y >= 0 && layout[y][x] instanceof PitTile; y--) {
        refreshTexture(layout[y][x], layout, elements, refreshed);
      }
    }
  }

  private static void refreshTexture(
      Tile tile, Tile[][] layout, LevelElement[][] elements, Set<Tile> refreshed) {
    if (!refreshed.add(tile)) return;
    tile.texturePath(
        TileTextureFactory.findTexturePath(tile, layout, elements, tile.levelElement()));
  }
}
