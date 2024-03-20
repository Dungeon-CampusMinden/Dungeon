package systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.util.*;

/**
 * The FogOfWarSystem class is responsible for controlling the fog of war in the game.
 *
 * <p>The fog of war is a game mechanic where areas of the game world that are not in the player's
 * line of sight are obscured. This class maintains a set of tiles that are currently darkened (not
 * visible to the player) and a list of entities that are hidden. It also keeps track of the last
 * known position of the hero (player character) and whether the fog of war system is currently
 * active.
 *
 * <p>The class provides methods to reset the fog of war system, check if it's active, set its
 * active state, and execute the system's logic each game tick.
 */
public class FogOfWarSystem extends System {
  public static int MAX_VIEW_DISTANCE = 25;
  public static final int VIEW_DISTANCE = 7;
  private static final int[][] mult = {
    {1, 0, 0, -1}, {0, 1, -1, 0}, {0, -1, -1, 0}, {-1, 0, 0, -1},
    {-1, 0, 0, 1}, {0, -1, 1, 0}, {0, 1, 1, 0}, {1, 0, 0, 1}
  };
  private static final float TINT_COLOR_DISTANCE_SCALE = 1.5f;
  private final Map<Tile, Integer> darkenedTiles = new HashMap<>();
  private final List<Entity> hiddenEntities = new ArrayList<>();
  private Point lastHeroPos = new Point(0, 0);
  private boolean active = true;

  /**
   * Resets the FogOfWarSystem to its initial state.
   *
   * <p>This method clears the sets of darkened tiles and hidden entities, and resets the last known
   * hero position. The last known hero position is set to the current hero's position if a hero
   * exists, otherwise it is set to (0,0).
   */
  public void reset() {
    this.darkenedTiles.clear();
    this.hiddenEntities.clear();
    this.lastHeroPos =
        Game.hero()
            .map(
                e ->
                    e.fetch(PositionComponent.class)
                        .map(PositionComponent::position)
                        .orElse(new Point(0, 0)))
            .orElse(new Point(0, 0));
  }

  /**
   * Checks if the FogOfWarSystem is active.
   *
   * @return true if the FogOfWarSystem is active, false otherwise.
   */
  public boolean active() {
    return this.active;
  }

  /**
   * Sets the active state of the FogOfWarSystem.
   *
   * <p>If the FogOfWarSystem is set to inactive, it also resets the FogOfWarSystem to its initial
   * state.
   *
   * @param active The new active state of the FogOfWarSystem.
   */
  public void active(boolean active) {
    this.active = active;

    if (!active) {
      this.revertTilesBackToLight(this.darkenedTiles.keySet().stream().toList());
      this.reset();
    }
  }

  private List<Tile> castLight(
      int row, float start, float end, int radius, int xx, int xy, int yx, int yy, Point heroPos) {
    List<Tile> visibleTiles = new ArrayList<>();
    if (start < end) {
      return visibleTiles;
    }
    float newStart = 0.0f;
    for (int i = row; i <= radius; i++) {
      int dx = -i - 1;
      int dy = -i;
      boolean blocked = false;
      while (dx <= 0) {
        dx += 1;
        // Translate the dx, dy coordinates into map coordinates
        int X = (int) (heroPos.x + (dx * xx + dy * xy));
        int Y = (int) (heroPos.y + (dx * yx + dy * yy));
        // l_slope and r_slope store the slopes of the left and right extremities of the square
        // we're considering
        float lSlope = (dx - 0.5f) / (dy + 0.5f);
        float rSlope = (dx + 0.5f) / (dy - 0.5f);
        if (start < rSlope) {
          continue;
        } else if (end > lSlope) {
          break;
        } else {
          // Our light beam is touching this square; light it
          if (dx * dx + dy * dy < radius * radius) {
            Tile tile = Game.tileAT(new Point(X, Y));
            visibleTiles.add(tile);
          }
          Tile tile = Game.tileAT(new Point(X, Y));
          if (tile == null) {
            continue;
          }
          if (blocked) { // previous step was a blocking square

            if (!tile.canSeeThrough()) { // this step is a blocking square
              newStart = rSlope;
              continue;
            } else {
              blocked = false;
              start = newStart;
            }
          } else {
            if (!tile.canSeeThrough() && i < radius) { // this step is a blocking square
              blocked = true;
              visibleTiles.addAll(
                  this.castLight(i + 1, start, lSlope, radius, xx, xy, yx, yy, heroPos));
              newStart = rSlope;
            }
          }
        }
      }
      if (blocked) break;
    }
    return visibleTiles;
  }

  private void darkenTile(Tile tile) {
    int newTint = this.getTintColor(tile.coordinate().toPoint());
    if (!this.darkenedTiles.containsKey(tile)) {
      int orgTint = tile.tintColor();
      tile.tintColor(newTint);
      this.darkenedTiles.put(tile, orgTint);
    } else {
      tile.tintColor(newTint);
    }
  }

  /**
   * Calculates the tint color for a tile based on its distance from the hero's position. The tint
   * color is represented as an ARGB integer, where the alpha component is adjusted based on the
   * distance. The closer the tile is to the hero, the more transparent (closer to white) it
   * becomes. If the tile is beyond the view distance, it is fully opaque.
   *
   * @param tilePos The position of the tile for which to calculate the tint color.
   * @return The calculated tint color as an ARGB integer.
   */
  private int getTintColor(Point tilePos) {
    float distance =
        (float)
            this.lastHeroPos
                .toCoordinate()
                .toPoint()
                .distance(tilePos); // point -> coordinate -> point to floor the value
    if (distance > VIEW_DISTANCE) {
      return 0xFFFFFF00;
    }
    float distanceFactor = Math.min(1, distance * TINT_COLOR_DISTANCE_SCALE / VIEW_DISTANCE);
    int alpha = (int) (255 * (1 - distanceFactor));

    return 0xFFFFFF00 | alpha;
  }

  private void revertTilesBackToLight(List<Tile> visibleTiles) {
    Iterator<Tile> iterator = this.darkenedTiles.keySet().iterator();
    while (iterator.hasNext()) {
      Tile darkenTile = iterator.next();
      Integer originalTint = this.darkenedTiles.get(darkenTile);
      // match non-current tile or tile that are far away
      if (visibleTiles.contains(darkenTile)) {
        darkenTile.tintColor(originalTint);
        iterator.remove();
      }
    }
  }

  private void hideAllHiddenEntities() {
    for (Tile darkenTile : this.darkenedTiles.keySet()) {
      for (Entity entity : Game.entityAtTile(darkenTile).toList()) {
        if (entity.isPresent(DrawComponent.class)) {
          DrawComponent dc =
              entity
                  .fetch(DrawComponent.class)
                  .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));
          dc.setVisible(false);
          this.hiddenEntities.add(entity);
        }
      }
    }
  }

  private void revealHiddenEntities() {
    for (Entity entity : this.hiddenEntities) {
      PositionComponent pc =
          entity
              .fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
      Tile tile = Game.tileAT(pc.position());
      if (!this.darkenedTiles.containsKey(tile)) {
        DrawComponent dc =
            entity
                .fetch(DrawComponent.class)
                .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));
        dc.setVisible(true);
      }
    }
  }

  @Override
  public void execute() {
    if (!this.active) return;

    // Only update the fog of war if the hero has moved
    Entity hero = Game.hero().orElse(null);
    if (hero == null) return; // If hero dies, the system will not be updated
    Point heroPos =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class))
            .position();

    // max diff
    if (!this.lastHeroPos.toCoordinate().equals(heroPos.toCoordinate())) {
      this.lastHeroPos = heroPos;

      List<Tile> allTilesInView = LevelUtils.tilesInRange(heroPos, MAX_VIEW_DISTANCE);

      List<Tile> visibleTiles = new ArrayList<>();
      visibleTiles.add(Game.tileAT(heroPos));
      // Cast light into the surrounding tiles
      for (int octant = 0; octant < 8; octant++) {
        visibleTiles.addAll(
            this.castLight(
                1,
                1.0f,
                0.0f,
                MAX_VIEW_DISTANCE,
                mult[octant][0],
                mult[octant][1],
                mult[octant][2],
                mult[octant][3],
                heroPos));
      }

      // Invert
      allTilesInView.removeAll(visibleTiles);
      allTilesInView.forEach(this::darkenTile);
      // Revert tiles back to light
      this.revertTilesBackToLight(visibleTiles);

      // Hide entities in the fog of war
      this.hideAllHiddenEntities();
    }

    // Reveal entities in the visible area
    this.revealHiddenEntities();
  }
}
