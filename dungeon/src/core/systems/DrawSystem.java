package core.systems;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.elements.tile.PitTile;
import core.level.utils.LevelElement;
import core.network.RenderStateManager;
import core.network.messages.EntityStateUpdate;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.draw.Painter;
import core.utils.components.draw.PainterConfig;
import core.utils.components.path.IPath;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This system draws the entities on the screen based on their render state.
 *
 * <p>This system is DECOUPLED from the live component data for position and animation state. It
 * reads this information from the {@link RenderStateManager}, which is populated by network
 * messages (or the {@code LocalNetworkHandler} in single-player).
 *
 * <p>The system still requires a {@link DrawComponent} on the entity to access the actual
 * textures and animation objects.
 *
 * <p>The animation to be played is determined by the {@code animationState} string received in the
 * {@link EntityStateUpdate}. This system does NOT queue or decide on animations; it only renders
 * the state it is told to.
 *
 * <p>The DrawSystem can't be paused.
 *
 * @see DrawComponent
 * @see RenderStateManager
 * @see EntityStateUpdate
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

  private final Map<IPath, PainterConfig> configs;

  /**
   * Create a new DrawSystem.
   *
   * <p>It only needs the DrawComponent to know *what* to draw (textures/animations). *Where* to
   * draw and *which* animation to play is determined by the RenderStateManager.
   */
  public DrawSystem() {
    super(DrawComponent.class);
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

  /**
   * Draws the level and all entities based on the states in {@link RenderStateManager}.
   *
   * <p>Entities with a {@link PlayerComponent} (the hero) will be drawn on top of others.
   */
  @Override
  public void execute() {
    drawLevel(Game.currentLevel());

    // Get the latest visual states for all entities from the central store.
    Map<Integer, EntityStateUpdate.EntityState> currentRenderStates =
      RenderStateManager.renderableEntityStates();

    // Create a map of all entities for quick lookup by ID.
    Map<Integer, Entity> entityMap =
      Game.allEntities().collect(Collectors.toMap(Entity::id, entity -> entity));

    // Get the hero's ID to ensure it's drawn last (on top).
    Optional<Integer> heroIdOpt = Game.hero().map(Entity::id);

    // Draw non-hero entities first.
    currentRenderStates.forEach(
      (entityId, renderState) -> {
        if (heroIdOpt.map(heroId -> !heroId.equals(entityId)).orElse(true)) {
          Entity entity = entityMap.get(entityId);
          if (entity != null && shouldDraw(renderState)) {
            draw(entity, renderState);
          }
        }
      });

    // Draw the hero last.
    heroIdOpt.ifPresent(
      heroId -> {
        EntityStateUpdate.EntityState heroState = currentRenderStates.get(heroId);
        Entity heroEntity = entityMap.get(heroId);
        if (heroState != null && heroEntity != null && shouldDraw(heroState)) {
          draw(heroEntity, heroState);
        }
      });
  }

  /**
   * Checks if an entity should be drawn based on its render state.
   *
   * @param renderState The authoritative render state for the entity.
   * @return true if the entity should be drawn, false otherwise.
   */
  private boolean shouldDraw(EntityStateUpdate.EntityState renderState) {
    if (!renderState.isVisible()) {
      return false;
    }

    Point position = renderState.position();
    Tile tile = Game.currentLevel().tileAt(position);

    return tile != null && tile.visible();
  }

  /**
   * Draws a single entity.
   *
   * @param entity The entity, used to get the DrawComponent.
   * @param renderState The authoritative state containing position and animation info.
   */
  private void draw(final Entity entity, final EntityStateUpdate.EntityState renderState) {
    DrawComponent dc =
      entity
        .fetch(DrawComponent.class)
        .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));

    String animationStateName = renderState.animationState();

    Animation animation = dc.animationMap().get(animationStateName);

    if (animation == null) {
      LOGGER.warning(
        "No animation found for state '"
          + animationStateName
          + "' in entity "
          + entity.id()
          + ". Check if the animation was loaded and the state name is correct.");

      // TODO: Fallback to latest animation
      animation = dc.animationMap().get("idle");
      if (animation == null) return;
    }

    IPath currentAnimationTexture = animation.nextAnimationTexturePath();
    if (!configs.containsKey(currentAnimationTexture)) {
      configs.put(
        currentAnimationTexture,
        new PainterConfig(currentAnimationTexture, 0, 0, dc.tintColor()));
    }
    PainterConfig conf = this.configs.get(currentAnimationTexture);
    conf.tintColor(dc.tintColor());

    // Use the position from the render state.
    PAINTER.draw(renderState.position(), currentAnimationTexture, conf);
  }

  /** DrawSystem can't be paused. */
  @Override
  public void stop() {
    run = true;
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

  private boolean isTilePitAndOpen(final Tile tile) {
    if (tile instanceof PitTile) {
      return ((PitTile) tile).isOpen();
    } else {
      return false;
    }
  }
}
