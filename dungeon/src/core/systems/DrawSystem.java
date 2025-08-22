package core.systems;

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
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.draw.Painter;
import core.utils.components.draw.PainterConfig;
import core.utils.components.path.IPath;
import java.util.*;
import java.util.stream.Collectors;

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

  public static boolean ALLOW_RENDER = true;

  /**
   * The batch is necessary to draw ALL the stuff. Every object that uses draw need to know the
   * batch.
   */
  private final SpriteBatch BATCH = ALLOW_RENDER ? new SpriteBatch() : null;

  /** Draws objects. */
  private final Painter PAINTER = new Painter(BATCH);

  /** offset the coordinate by half a tile, it makes every Entity not walk on the sidewalls. */
  private static final float X_OFFSET = 0.5f;

  /**
   * offset the coordinate by a quarter tile,it looks a bit more like every Entity is not walking
   * over walls.
   */
  private static final float Y_OFFSET = 0.25f;

  private final Map<IPath, PainterConfig> configs;

  /** Create a new DrawSystem. */
  public DrawSystem() {
    super(DrawComponent.class, PositionComponent.class);
    configs = new HashMap<>();
  }

  /**
   * Get the {@link Painter} that is used by this system.
   *
   * @return the {@link #PAINTER} of the DrawSystem
   */
  public Painter painter() {
    return PAINTER;
  }

  /**
   * Get the {@link SpriteBatch} that is used by this system.
   *
   * @return the {@link #BATCH} of the DrawSystem
   */
  public SpriteBatch batch() {
    return BATCH;
  }

  /**
   * Will draw entities at their position with their current animation.
   *
   * <p>All entities with a {@link PlayerComponent} will be drawn on top.
   *
   * @see DrawComponent
   * @see Animation
   */
  @Override
  public void execute() {
    Map<Boolean, List<Entity>> partitionedEntities =
        filteredEntityStream(DrawComponent.class, PositionComponent.class)
            .collect(Collectors.partitioningBy(entity -> entity.isPresent(PlayerComponent.class)));
    List<Entity> players = partitionedEntities.get(true);
    List<Entity> npcs = partitionedEntities.get(false);

    Game.currentLevel().ifPresent(this::drawLevel);
    npcs.stream().filter(this::shouldDraw).forEach(entity -> draw(buildDataObject(entity)));
    players.stream().filter(this::shouldDraw).forEach(entity -> draw(buildDataObject(entity)));
  }

  /**
   * Checks if an entity should be drawn. By checking:
   *
   * <ol>
   *   <li>The tile the entity is on is visible
   *   <li>The entity itself is visible
   * </ol>
   *
   * @param entity the entity to check
   * @return true if the entity should be drawn, false otherwise
   * @see DrawComponent#isVisible()
   */
  private boolean shouldDraw(Entity entity) {
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));

    DrawComponent dc =
        entity
            .fetch(DrawComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));

    if (!dc.isVisible()) return false;

    return Game.currentLevel()
        .flatMap(level -> level.tileAt(pc.position()))
        .map(Tile::visible)
        .orElse(false);
  }

  private void draw(final DSData dsd) {
    reduceFrameTimer(dsd.dc);
    setNextAnimation(dsd.dc);
    final Animation animation = dsd.dc.currentAnimation();
    if (!ALLOW_RENDER || animation == null) return;
    IPath currentAnimationTexture = animation.nextAnimationTexturePath();
    if (!configs.containsKey(currentAnimationTexture)) {
      configs.put(
          currentAnimationTexture,
          new PainterConfig(currentAnimationTexture, 0, 0, dsd.dc.tintColor()));
    }
    PainterConfig conf = this.configs.get(currentAnimationTexture);
    conf.tintColor(dsd.dc.tintColor());
    PAINTER.draw(dsd.pc.position(), currentAnimationTexture, conf);
  }

  /**
   * Reduce the frame timer for each animation in the queue, remove animations that have a frame
   * time < 0.
   *
   * @param dc Component to iterate over
   */
  private void reduceFrameTimer(final DrawComponent dc) {
    // iterate through animationQueue
    for (Map.Entry<IPath, Integer> entry : dc.animationQueue().entrySet()) {
      // reduce remaining frame time of animation by 1
      entry.setValue(entry.getValue() - 1);
    }
    // remove animations when there is no remaining frame time
    dc.animationQueue().entrySet().stream()
        .filter(x -> x.getValue() < 0)
        .forEach(x -> dc.deQueue(x.getKey()));
  }

  /**
   * Checks the status of animations in the animationQueue and selects the next animation by
   * priority.
   *
   * @param dc DrawComponent to draw
   */
  private void setNextAnimation(final DrawComponent dc) {

    Optional<Map.Entry<IPath, Integer>> highestFind =
        dc.animationQueue().entrySet().stream()
            .max(Comparator.comparingInt(x -> x.getKey().priority()));

    // when there is an animation load it
    if (highestFind.isPresent()) {
      IPath highestPrio = highestFind.get().getKey();
      // making sure the animation exists
      dc.animationMap().get(highestPrio.pathString());
      // changing the Animation
      dc.currentAnimation(highestPrio);
    }
  }

  /** DrawSystem can't be paused. */
  @Override
  public void stop() {
    run = true;
  }

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
    if (!ALLOW_RENDER) return;

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
