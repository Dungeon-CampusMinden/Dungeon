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

public class FogOfWarSystem extends System {
  public static final int VIEW_DISTANCE = 30;
  private static final int[][] mult = {
    {1, 0, 0, -1}, {0, 1, -1, 0}, {0, -1, -1, 0}, {-1, 0, 0, -1},
    {-1, 0, 0, 1}, {0, -1, 1, 0}, {0, 1, 1, 0}, {1, 0, 0, 1}
  };
  private static final float TINT_COLOR_DISTANCE_SCALE = 1.5f;
  private static final int BRIGHTEST_TINT_COLOR = 128;
  private final Set<Tile> darkenedTiles = new HashSet<>();
  private final List<Entity> hiddenEntities = new ArrayList<>();
  private Point lastHeroPos = new Point(0, 0);

  public void reset() {
    this.darkenedTiles.clear();
    this.hiddenEntities.clear();
    this.lastHeroPos = new Point(0, 0);
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
    if (!this.darkenedTiles.contains(tile)) {
      int newTint = this.getTintColor(tile.coordinate().toPoint());
      tile.tintColor(newTint);
      this.darkenedTiles.add(tile);
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
    float distance = (float) this.lastHeroPos.distance(tilePos);
    if (distance > VIEW_DISTANCE) {
      return 0xffffff00;
    }
    float distanceFactor = distance * TINT_COLOR_DISTANCE_SCALE / VIEW_DISTANCE;
    int alpha = (int) (BRIGHTEST_TINT_COLOR - (BRIGHTEST_TINT_COLOR * distanceFactor));
    return 0xffffff00 | alpha;
  }

  private void revertTilesBackToLight(List<Tile> visibleTiles) {
    Iterator<Tile> iterator = this.darkenedTiles.iterator();
    while (iterator.hasNext()) {
      Tile darkenTile = iterator.next();
      // match non-current tile or tile that are far away
      if (visibleTiles.contains(darkenTile)) {
        darkenTile.tintColor(-1);
        iterator.remove();
      }
    }
  }

  private void hideAllHiddenEntities() {
    for (Tile darkenTile : this.darkenedTiles) {
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
      if (!this.darkenedTiles.contains(tile)) {
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
    // Only update the fog of war if the hero has moved
    Entity hero = Game.hero().orElse(null);
    if (hero == null) return; // If hero dies, the system will not be updated
    Point heroPos =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class))
            .position();

    // max diff
    if (this.lastHeroPos.distance(heroPos) > 0.5) {
      this.lastHeroPos = heroPos;

      List<Tile> allTilesInView = LevelUtils.tilesInRange(heroPos, VIEW_DISTANCE);

      List<Tile> visibleTiles = new ArrayList<>();
      visibleTiles.add(Game.tileAT(heroPos));
      // Cast light into the surrounding tiles
      for (int octant = 0; octant < 8; octant++) {
        visibleTiles.addAll(
            this.castLight(
                1,
                1.0f,
                0.0f,
                VIEW_DISTANCE,
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
