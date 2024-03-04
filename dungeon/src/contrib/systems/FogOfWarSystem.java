package contrib.systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import java.util.*;

public class FogOfWarSystem extends System {
  public static final int VIEW_DISTANCE = 20;
  private static final int[][] mult = {
    {1, 0, 0, -1}, {0, 1, -1, 0}, {0, -1, -1, 0}, {-1, 0, 0, -1},
    {-1, 0, 0, 1}, {0, -1, 1, 0}, {0, 1, 1, 0}, {1, 0, 0, 1}
  };
  private final Map<Tile, IPath> originalTextures = new HashMap<>();
  private final Set<Tile> darkenedTiles = new HashSet<>();
  private final List<Entity> hiddenEntities = new ArrayList<>();
  private Point lastHeroPos = new Point(0, 0);

  public void reset() {
    originalTextures.clear();
    darkenedTiles.clear();
    hiddenEntities.clear();
    lastHeroPos = new Point(0, 0);
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

  private void darkenTile(Tile tile) {
    if (!originalTextures.containsKey(tile)) {
      originalTextures.put(tile, tile.texturePath());
    }
    if (!darkenedTiles.contains(tile)) {
      IPath newTexture =
          TileTextureFactory.findTexturePath(
              new TileTextureFactory.LevelPart(
                  LevelElement
                      .SKIP, // TODO: Change Texture to a darkened version or add Variable at top
                  DesignLabel.DARK,
                  new LevelElement[][] {},
                  tile.position().toCoordinate()));
      tile.texturePath(newTexture);
      darkenedTiles.add(tile);
    }
  }

  private void revertTilesBackToLight(List<Tile> visibleTiles) {
    Iterator<Tile> iterator = darkenedTiles.iterator();
    while (iterator.hasNext()) {
      Tile darkenTile = iterator.next();
      // match non-current tile or tile that are far away
      if (visibleTiles.contains(darkenTile)) {
        IPath originalTexture = originalTextures.get(darkenTile);
        darkenTile.texturePath(originalTexture);
        iterator.remove();
      }
    }
  }

  private void hideAllHiddenEntities() {
    for (Tile darkenTile : darkenedTiles) {
      for (Entity entity : Game.entityAtTile(darkenTile).toList()) {
        if (entity.isPresent(DrawComponent.class)) {
          DrawComponent dc =
              entity
                  .fetch(DrawComponent.class)
                  .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));
          dc.setVisible(false);
          hiddenEntities.add(entity);
        }
      }
    }
  }

  private void revealHiddenEntities() {
    for (Entity entity : hiddenEntities) {
      PositionComponent pc =
          entity
              .fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
      Tile tile = Game.tileAT(pc.position());
      if (!darkenedTiles.contains(tile)) {
        DrawComponent dc =
            entity
                .fetch(DrawComponent.class)
                .orElseThrow(() -> MissingComponentException.build(entity, DrawComponent.class));
        dc.setVisible(true);
      }
    }
  }

  private List<Tile> getAllTilesInRange(Point heroPos, int range) {
    List<Tile> allTiles = new ArrayList<>();
    for (int x = -range; x < range; x++) {
      for (int y = -range; y < range; y++) {
        Tile tile = Game.tileAT(new Point(heroPos.x + x, heroPos.y + y));
        if (tile != null) {
          allTiles.add(tile);
        }
      }
    }
    return allTiles;
  }

  @Override
  public void execute() {
    // Only update the fog of war if the hero has moved
    Entity hero = Game.hero().orElseThrow(MissingHeroException::new);
    Point heroPos =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class))
            .position();

    // max diff
    if (lastHeroPos.distance(heroPos) > 0.5) {
      lastHeroPos = heroPos;

      List<Tile> allTilesInView = getAllTilesInRange(heroPos, VIEW_DISTANCE);

      List<Tile> visibleTiles = new ArrayList<>();
      visibleTiles.add(Game.tileAT(heroPos));
      // Cast light into the surrounding tiles
      for (int octant = 0; octant < 8; octant++) {
        visibleTiles.addAll(
            castLight(
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
      revertTilesBackToLight(visibleTiles);

      // Hide entities in the fog of war
      hideAllHiddenEntities();
    }

    // Reveal entities in the visible area
    revealHiddenEntities();
  }
}
