package systems;

import components.TorchComponent;
import contrib.utils.EntityUtils;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelElement;
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
 */
public class FogOfWarSystem extends System {
  private static final int DISTANCE_TRANSITION_SIZE = 2; // size of distance transition (in tiles)
  private static final int HIDE_ENTITY_THRESHOLD =
      0xFFFFFF99; // tint color threshold for hiding entities
  private static final int[][] mult = { // needed for casting light
    {1, 0, 0, -1}, {0, 1, -1, 0}, {0, -1, -1, 0}, {-1, 0, 0, -1},
    {-1, 0, 0, 1}, {0, -1, 1, 0}, {0, 1, 1, 0}, {1, 0, 0, 1}
  };
  private static final float TINT_COLOR_WALL_DISTANCE_SCALE =
      1.5f; // scale factor for behind wall distance fog
  private static final float TINT_COLOR_DISTANCE_SCALE = .5f; // scale factor for distance fog

  /** The view distance (range for tiles that are fully visible). */
  private static int currentViewDistance = 7;

  /** The maximum view distance (all tiles to consider for calculation). */
  private static final int MAX_VIEW_DISTANCE = 25;

  private final Map<Tile, Integer> darkenedTiles = new HashMap<>();
  private final List<Entity> hiddenEntities = new ArrayList<>();
  private boolean active = true;

  /**
   * Resets the FogOfWarSystem.
   *
   * <p>This method clears the sets of darkened tiles and hidden entities.
   *
   * @param revert If true, the FogOfWarSystem will also revert to its initial state.
   * @see #reset()
   * @see #revert()
   */
  public void reset(boolean revert) {
    darkenedTiles.clear();
    hiddenEntities.clear();
    if (revert) {
      revert();
    }
  }

  /**
   * Resets the FogOfWarSystem to its initial state.
   *
   * <p>This method clears the sets of darkened tiles and hidden entities, and resets the last known
   * hero position. The last known hero position is set to the current hero's position if a hero
   * exists, otherwise it is set to (0,0). This method also reverts the FogOfWarSystem to its
   *
   * @see #revert()
   */
  public void reset() {
    reset(true);
  }

  /** Reverts the FogOfWarSystem. This reveals all darkened tiles and hidden entities. */
  public void revert() {
    revertTilesBackToLight(darkenedTiles.keySet().stream().toList());
    revealHiddenEntities();
  }

  /**
   * Sets the view distance of the FogOfWarSystem.
   *
   * <p>The view distance is the range of tiles that are fully visible to the player. The view
   * distance is used to calculate the tint color of tiles that are beyond the view distance.
   *
   * <p>NOTE: it can't be greater than the {@link #MAX_VIEW_DISTANCE maximum view distance}.
   *
   * @param newViewDistance The new view distance of the FogOfWarSystem.
   * @throws IllegalArgumentException if the view distance is greater than the maximum view distance
   *     or less than 0.
   */
  public static void currentViewDistance(int newViewDistance) {
    if (newViewDistance > MAX_VIEW_DISTANCE || newViewDistance < 0) {
      throw new IllegalArgumentException(
          "View distance must be between 0 and " + MAX_VIEW_DISTANCE);
    }
    currentViewDistance = newViewDistance;
  }

  /**
   * Returns the current view distance of the FogOfWarSystem.
   *
   * @return The current view distance of the FogOfWarSystem.
   */
  public static int currentViewDistance() {
    return currentViewDistance;
  }

  /**
   * Checks if the FogOfWarSystem is active.
   *
   * @return true if the FogOfWarSystem is active, false otherwise.
   */
  public boolean active() {
    return active;
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
      revert();
      reset();
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
              visibleTiles.addAll(castLight(i + 1, start, lSlope, radius, xx, xy, yx, yy, heroPos));
              newStart = rSlope;
            }
          }
        }
      }
      if (blocked) break;
    }
    return visibleTiles;
  }

  private void darkenTile(Tile tile, int maxDistance, float scale, Point heroPos) {
    int newTint = getTintColor(tile.coordinate().toPoint(), maxDistance, scale, heroPos);
    int orgTint = tile.tintColor();
    int mixedTint = orgTint == -1 ? newTint : (orgTint & 0xFFFFFF00) | (newTint & 0x000000FF);
    if (!darkenedTiles.containsKey(tile)) {
      darkenedTiles.put(tile, orgTint);
    }
    tile.tintColor(mixedTint);
  }

  /**
   * Calculates the tint color for a tile based on its distance from the hero's position. The tint
   * color is represented as an ARGB integer, where the alpha component is adjusted based on the
   * distance. The closer the tile is to the hero, the more transparent (closer to white) it
   * becomes. If the tile is beyond the view distance, it is fully opaque.
   *
   * @param tilePos The position of the tile for which to calculate the tint color.
   * @param maxDistance The maximum distance from the hero's position at which the tile is fully
   *     opaque.
   * @param scale The scale factor for the distance. The larger the scale, the more transparent the
   *     tiles will be.
   * @param heroPos The position of the hero.
   * @return The calculated tint color as an ARGB integer.
   */
  private int getTintColor(Point tilePos, int maxDistance, float scale, Point heroPos) {
    float distance =
        (float)
            heroPos
                .toCoordinate()
                .toPoint()
                .distance(tilePos); // point -> coordinate -> point to floor the value
    if (distance > maxDistance) {
      return 0xFFFFFF00;
    }
    float distanceFactor = Math.min(1, distance * scale / (maxDistance));
    int alpha = (int) (255 * (1 - distanceFactor));

    return 0xFFFFFF00 | alpha;
  }

  private void revertTilesBackToLight(List<Tile> visibleTiles) {
    Iterator<Tile> iterator = darkenedTiles.keySet().iterator();
    while (iterator.hasNext()) {
      Tile darkenTile = iterator.next();
      Integer originalTint = darkenedTiles.get(darkenTile);
      // match non-current tile or tile that are far away
      if (visibleTiles.contains(darkenTile)) {
        darkenTile.tintColor(originalTint);
        iterator.remove();
      }
    }
  }

  private void hideAllHiddenEntities() {
    darkenedTiles.keySet().stream()
        .filter(tile -> tile.tintColor() < HIDE_ENTITY_THRESHOLD)
        .flatMap(Game::entityAtTile)
        .filter(entity -> entity.isPresent(DrawComponent.class))
        .filter(entity -> !isAntiTorchAndLit(entity)) // Ignore anti-torches
        .filter(entity -> !entity.name().contains("tpball")) // Ignore tpballs
        .forEach(
            entity -> {
              DrawComponent dc =
                  entity
                      .fetch(DrawComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(entity, DrawComponent.class));
              dc.setVisible(false);
              hiddenEntities.add(entity);
            });
  }

  private boolean isAntiTorchAndLit(Entity entity) {
    return entity.name().contains("anti_torch")
        && entity
            .fetch(TorchComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, TorchComponent.class))
            .lit();
  }

  private void revealHiddenEntities() {
    for (Entity entity : hiddenEntities) {
      PositionComponent pc =
          entity
              .fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
      Tile tile = Game.tileAT(pc.position());
      if (!darkenedTiles.containsKey(tile) || tile.tintColor() >= HIDE_ENTITY_THRESHOLD) {
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
    if (!active) return;

    Point heroPos = EntityUtils.getHeroPosition();
    if (heroPos == null) return; // no hero, no fog of war

    List<Tile> allTilesInView = LevelUtils.tilesInRange(heroPos, MAX_VIEW_DISTANCE);
    // Revert all darkened tiles back to light that are not in view
    List<Tile> tilesOutsideView = new ArrayList<>(darkenedTiles.keySet());
    tilesOutsideView.removeAll(allTilesInView);
    revertTilesBackToLight(tilesOutsideView);

    List<Tile> visibleTiles = new ArrayList<>();
    visibleTiles.add(Game.tileAT(heroPos));
    // Cast light into the surrounding tiles
    for (int octant = 0; octant < 8; octant++) {
      visibleTiles.addAll(
          castLight(
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
    List<Tile> distancedTiles = new ArrayList<>(visibleTiles.stream().toList()); // copy

    // Handle tiles that are beyond the view distance
    distancedTiles.removeAll(LevelUtils.tilesInRange(heroPos, currentViewDistance));
    distancedTiles.forEach(
        (tile) ->
            darkenTile(
                tile,
                currentViewDistance + DISTANCE_TRANSITION_SIZE,
                TINT_COLOR_DISTANCE_SCALE,
                heroPos));
    visibleTiles.removeAll(distancedTiles); // remove distanced tiles from visible tiles
    allTilesInView.removeAll(distancedTiles); // and from tile behind walls

    allTilesInView.removeAll(visibleTiles); // remove visible tiles from tiles behind walls

    // Darken tiles that are behind walls
    allTilesInView.forEach(
        (tile) -> darkenTile(tile, currentViewDistance, TINT_COLOR_WALL_DISTANCE_SCALE, heroPos));

    // Revert all visible tiles back to light
    revertTilesBackToLight(visibleTiles);

    // Hide entities in the fog of war
    hideAllHiddenEntities();

    // Reveal entities in the visible area
    revealHiddenEntities();
  }

  /**
   * Updates the tile in the fog of war system.
   *
   * <p>This method updates the tile in the fog of war system. If the old tile is darkened, the tint
   * color is transferred to the new tile. This happens after {@link
   * core.level.elements.ILevel#changeTileElementType(Tile, LevelElement) changing the tile element
   * type}.
   *
   * @param oldTile The old tile.
   * @param newTile The new tile.
   */
  public void updateTile(Tile oldTile, Tile newTile) {
    if (darkenedTiles.containsKey(oldTile)) {
      int tint = darkenedTiles.remove(oldTile);
      darkenedTiles.put(newTile, tint);
    }
  }
}
