package core.systems;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.PitTile;
import core.level.utils.LevelElement;
import core.utils.components.draw.Painter;
import core.utils.components.draw.PainterConfig;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.IPath;
import java.util.*;

/**
 * This system draws the entities on the screen.
 *
 * <p>Each entity with a {@link DrawComponent} and a {@link PositionComponent} will be drawn on the
 * screen.
 *
 * <p>This System will also draw the level.
 *
 * <p>The system will get the current animation from the {@link DrawComponent} and will get the next
 * animation frame from the {@link Animation}, and then draw it on the current position stored in
 * the {@link PositionComponent}.
 *
 * <p>This system will not queue animations. This must be done by other systems. The system
 * evaluates the queue and draws the animation with the highest priority in the queue.
 *
 * <p>The DrawSystem can't be paused.
 *
 * @see DrawComponent
 * @see Animation
 * @see Painter
 */
public final class DrawSystem extends System {

  /**
   * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
   * batch.
   */
  private static final SpriteBatch BATCH = new SpriteBatch();

  /** Draws objects. */
  private static final Painter PAINTER = new Painter(BATCH);

  /** offset the coordinate by half a tile, it makes every Entity not walk on the sidewalls. */
  private static final float X_OFFSET = 0.5f;

  /**
   * offset the coordinate by a quarter tile,it looks a bit more like every Entity is not walking
   * over walls.
   */
  private static final float Y_OFFSET = 0.25f;

  private final TreeMap<Integer, List<DSData>> sortedEntities = new TreeMap<>();
  private final Map<IPath, PainterConfig> configs;

  /** Create a new DrawSystem. */
  public DrawSystem() {
    super(DrawComponent.class, PositionComponent.class);
    onEntityAdd = (e) -> onEntityChanged(e, true);
    onEntityRemove = (e) -> onEntityChanged(e, false);
    configs = new HashMap<>();
  }

  /**
   * Get the {@link Painter} that is used by this system.
   *
   * @return the {@link #PAINTER} of the DrawSystem
   */
  public static Painter painter() {
    return PAINTER;
  }

  /**
   * Get the {@link SpriteBatch} that is used by this system.
   *
   * @return the {@link #BATCH} of the DrawSystem
   */
  public static SpriteBatch batch() {
    return BATCH;
  }

  private void onEntityChanged(Entity changed, boolean added) {
    DSData data = buildDataObject(changed);
    int depth = data.dc.depth();
    List<DSData> entitiesAtDepth = sortedEntities.get(depth);

    if (entitiesAtDepth == null) {
      if (added) {
        entitiesAtDepth = new ArrayList<>();
        entitiesAtDepth.add(data);
        sortedEntities.put(depth, entitiesAtDepth);
      }
    } else if (!entitiesAtDepth.contains(data) && added) {
      entitiesAtDepth.add(data);
    } else if (!added) {
      entitiesAtDepth.remove(data);
      if (entitiesAtDepth.isEmpty()) {
        sortedEntities.remove(depth);
      }
    }
  }

  public void onEntityChangedDepth(Entity entity) {
    DSData data = buildDataObject(entity);
    int oldDepth = Integer.MIN_VALUE;
    int newDepth = data.dc.depth();

    // Find entry in our map
    for (Map.Entry<Integer, List<DSData>> entry : sortedEntities.entrySet()) {
      if (entry.getValue().contains(data)) {
        oldDepth = entry.getKey();
      }
    }

    // Remove old entry
    if (oldDepth != Integer.MIN_VALUE) {
      sortedEntities.get(oldDepth).remove(data);
    }

    // Add at new depth
    List<DSData> entitiesAtDepth = sortedEntities.get(newDepth);
    if (entitiesAtDepth == null) {
      entitiesAtDepth = new ArrayList<>();
      sortedEntities.put(newDepth, entitiesAtDepth);
    } else {
      entitiesAtDepth.add(data);
    }
  }

  /**
   * Will draw entities at their position with their current animation.
   *
   * <p>All entities with a {@link PlayerComponent} will be drawn on top.
   *
   * @see DrawComponent
   */
  @Override
  public void execute() {
    BATCH.setProjectionMatrix(CameraSystem.camera().combined);
    BATCH.begin();

    drawLevel(Game.currentLevel());

    sortedEntities.values().stream()
        .flatMap(
            list ->
                list.stream().sorted(Comparator.comparingDouble(data -> -data.pc.position().y())))
        .filter(this::shouldDraw)
        .forEach(this::draw);

    BATCH.end();
  }

  /**
   * Checks if an entity should be drawn. By checking:
   *
   * <ol>
   *   <li>The tile the entity is on is visible
   *   <li>The entity itself is visible
   * </ol>
   *
   * @param data the entity to check
   * @return true if the entity should be drawn, false otherwise
   * @see DrawComponent#isVisible()
   */
  private boolean shouldDraw(DSData data) {
    Tile tile = Game.currentLevel().tileAt(data.pc.position());
    if (tile == null) return false;
    if (!data.dc.isVisible()) return false;
    return tile.visible();
  }

  private void draw(final DSData dsd) {
    dsd.dc.update();
    Sprite sprite = dsd.dc.getSprite();
    PainterConfig conf =
        new PainterConfig(0, 0, dsd.dc.getWidth(), dsd.dc.getHeight(), dsd.dc.tintColor());
    if (dsd.dc.currentAnimation().getConfig().centered()) {
      conf =
          new PainterConfig(
              -dsd.dc.getWidth() / 2,
              -dsd.dc.getHeight() / 2,
              dsd.dc.getWidth(),
              dsd.dc.getHeight(),
              dsd.dc.tintColor());
    }
    PAINTER.draw(dsd.pc.position(), sprite, conf, dsd.pc.rotation());
  }

  /** DrawSystem can't be paused. */
  @Override
  public void stop() {
    run = true;
  }

  /**
   * Builds the data record used by this system.
   *
   * @param entity The entity with a DrawComponent and a PositionComponent
   * @return The data record
   */
  private DSData buildDataObject(final Entity entity) {
    DrawComponent dc = entity.fetch(DrawComponent.class).get();
    PositionComponent pc = entity.fetch(PositionComponent.class).get();
    return new DSData(entity, dc, pc);
  }

  private void drawLevel(ILevel currentLevel) {
    if (currentLevel == null) throw new IllegalArgumentException("Level to draw can´t be null.");
    Map<IPath, PainterConfig> mapping = new HashMap<>();

    Tile[][] layout = currentLevel.layout();
    for (Tile[] tiles : layout) {
      for (int x = 0; x < layout[0].length; x++) {
        Tile t = tiles[x];
        if (t.levelElement() != LevelElement.SKIP && !isTilePitAndOpen(t) && t.visible()) {
          IPath texturePath = t.texturePath();
          if (!mapping.containsKey(texturePath)
              || (mapping.get(texturePath).tintColor() != t.tintColor())) {
            mapping.put(
                texturePath, new PainterConfig(texturePath, X_OFFSET, Y_OFFSET, t.tintColor()));
          }
          PAINTER.draw(t.position(), texturePath, mapping.get(texturePath));
        }
      }
    }
  }

  /**
   * Checks if the provided tile is an instance of PitTile and if it's open.
   *
   * @param tile The tile to check.
   * @return true if the tile is an instance of PitTile, and it's open, false otherwise.
   */
  private boolean isTilePitAndOpen(final Tile tile) {
    if (tile instanceof PitTile) {
      return ((PitTile) tile).isOpen();
    } else {
      return false;
    }
  }

  private record DSData(Entity e, DrawComponent dc, PositionComponent pc) {}
}
