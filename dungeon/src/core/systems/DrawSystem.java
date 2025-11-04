package core.systems;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Affine2;
import contrib.utils.EntityUtils;
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
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.DrawConfig;
import core.utils.components.draw.TextureMap;
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
 */
public final class DrawSystem extends System {

  /**
   * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
   * batch.
   */
  private static final SpriteBatch BATCH = new SpriteBatch();

  private final TreeMap<Integer, List<Entity>> sortedEntities = new TreeMap<>();

  /** Create a new DrawSystem. */
  public DrawSystem() {
    super(DrawComponent.class, PositionComponent.class);
    onEntityAdd = (e) -> onEntityChanged(e, true);
    onEntityRemove = (e) -> onEntityChanged(e, false);
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
    List<Entity> entitiesAtDepth = sortedEntities.get(depth);

    if (entitiesAtDepth == null) {
      if (added) {
        entitiesAtDepth = new ArrayList<>();
        entitiesAtDepth.add(changed);
        sortedEntities.put(depth, entitiesAtDepth);
      }
    } else if (!entitiesAtDepth.contains(changed) && added) {
      entitiesAtDepth.add(changed);
    } else if (!added) {
      entitiesAtDepth.remove(changed);
      if (entitiesAtDepth.isEmpty()) {
        sortedEntities.remove(depth);
      }
    }
  }

  /**
   * Updates an entities depth. This needs to be called in order to update the internal sorting of
   * the DrawSystem.
   *
   * @param entity The entity that changed its depth
   * @param depth The new depth of the entity
   */
  public void changeEntityDepth(Entity entity, int depth) {
    DSData data = buildDataObject(entity);

    int oldDepth = data.dc.depth();
    data.dc.depth(depth);

    // Remove old entry
    sortedEntities.get(oldDepth).remove(entity);

    // Add at new depth
    List<Entity> entitiesAtDepth = sortedEntities.computeIfAbsent(depth, k -> new ArrayList<>());
    entitiesAtDepth.add(entity);
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
    BATCH.begin();

    Game.currentLevel().ifPresent(this::drawLevel);

    for (List<Entity> group : sortedEntities.values()) {
      group.stream()
          .map(this::buildDataObject)
          .sorted(Comparator.comparingDouble((DSData d) -> -EntityUtils.getPosition(d.e).y()))
          .filter(this::shouldDraw)
          .forEach(this::draw);
    }

    BATCH.end();
  }

  /**
   * Checks if an entity should be drawn. By checking:
   *
   * <ol>
   *   <li>The entity itself is visible
   *   <li>Any corner of the sprite is visible
   * </ol>
   *
   * @param data the components of the entity to check
   * @return true if the entity should be drawn, false otherwise
   * @see DrawComponent#isVisible()
   */
  private boolean shouldDraw(DSData data) {
    // New check: first check if entity.dc is visible. Otherwise check if any tiles under the
    // corners of the sprite are visible.
    if (!data.dc.isVisible()) {
      return false;
    }

    Point pos = data.pc.position();
    // Use data.dc.getSpriteWidth() and similar
    float width = data.dc.getWidth() * data.pc.scale().x();
    float height = data.dc.getHeight() * data.pc.scale().y();
    List<Point> corners =
        List.of(
            pos.translate(0, 0),
            pos.translate(width, 0),
            pos.translate(0, height),
            pos.translate(width, height));

    return Game.currentLevel()
        .map(
            level ->
                corners.stream()
                    .anyMatch(
                        c -> {
                          Tile t = level.tileAt(c).orElse(null);
                          return t != null && t.visible() && !isTilePitAndOpen(t);
                        }))
        .orElse(false);
  }

  private void draw(final DSData dsd) {
    dsd.dc.update();
    Sprite sprite = dsd.dc.getSprite();
    DrawConfig conf = new DrawConfig(
        Vector2.ZERO,
        Vector2.of(dsd.dc.getWidth(), dsd.dc.getHeight()),
        dsd.pc.scale(),
        dsd.dc.tintColor(),
        dsd.dc.currentAnimation().getConfig().mirrored(),
        dsd.pc.rotation()
    );

    draw(dsd.pc.position(), sprite, conf);
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
    DrawComponent dc =
        entity
            .fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    return new DSData(entity, dc, pc);
  }

  private void drawLevel(ILevel currentLevel) {
    if (currentLevel == null) throw new IllegalArgumentException("Level to draw can´t be null.");

    Tile[][] layout = currentLevel.layout();
    for (Tile[] tiles : layout) {
      for (int x = 0; x < layout[0].length; x++) {
        Tile t = tiles[x];
        if (t.levelElement() != LevelElement.SKIP && !isTilePitAndOpen(t) && t.visible()) {
          IPath texturePath = t.texturePath();
          draw(t.position(), texturePath, new DrawConfig());
        }
      }
    }
  }

  /**
   * Draws a sprite at a given position with the specified configuration and rotation.
   *
   * <p>The sprite will only be drawn if its position is within the camera's frustum.
   *
   * @param position the world position where the sprite should be drawn
   * @param sprite the {@link Sprite} to draw
   * @param config the {@link DrawConfig} controlling scaling, tint, and offset
   */
  public void draw(final Point position, final Sprite sprite, final DrawConfig config) {
    sprite.setFlip(config.mirrored(), false);

    // Calculate transformations
    Affine2 transform = new Affine2();

    transform.setToTranslation(position.x(), position.y());

    // Scale first while origin is in the bottom-left
    transform.scale(config.scale().x(), config.scale().y());

    // Then rotate around the middle
    transform.translate(config.size().x() / 2f, config.size().y() / 2f);
    transform.rotate(config.rotation());
    transform.translate(-config.size().x() / 2f, -config.size().y() / 2f);

    // Apply tint color if specified
    if (config.tintColor() != -1) {
      BATCH.setColor(new Color(config.tintColor()));
    } else {
      BATCH.setColor(Color.WHITE);
    }
    BATCH.draw(sprite, config.size().x(), config.size().y(), transform);
  }

  /**
   * Draws a texture from a path at a given position using the specified configuration.
   *
   * <p>This method automatically wraps the texture in a {@link Sprite} using {@link TextureMap} and
   * delegates to {@link #draw(Point, Sprite, DrawConfig)}.
   *
   * @param position the world position where the texture should be drawn
   * @param path the {@link IPath} identifying the texture to draw
   * @param config the {@link DrawConfig} controlling scaling, tint, and offset
   */
  public void draw(final Point position, final IPath path, final DrawConfig config) {
    draw(position, new Sprite(TextureMap.instance().textureAt(path)), config);
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
