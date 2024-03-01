package contrib.systems;

import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelUtils;
import core.level.utils.TileTextureFactory;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import java.util.*;

public class FogOfWarSystem extends System {
  public static final int VIEW_DISTANCE = 15;
  public static final int RESOLUTION = 90;
  private static final Map<Tile, IPath> originalTextures = new HashMap<>();
    private static final Set<Tile> darkenedTiles = new HashSet<>();
  private static final List<Entity> hiddenEntities = new ArrayList<>();
  private static Point lastHeroPos = new Point(0, 0);

  private static List<Point> generatePointsAroundHero(Point heroPos, int resolution) {
    List<Point> points = new ArrayList<>();
    double angleIncrement = 360.0 / resolution;
    for (int i = 0; i < resolution; i++) {
      double angle = Math.toRadians(i * angleIncrement);
      double dx = (VIEW_DISTANCE * Math.cos(angle));
      double dy = (VIEW_DISTANCE * Math.sin(angle));
      points.add(new Point((float) (heroPos.x + dx), (float) (heroPos.y + dy)));
    }

    return points;
  }

  private static List<Tile> raycastToEachPoint(Point heroPos, List<Point> points, int distance) {
    List<Tile> currentDarkenedTiles = new ArrayList<>();
    points.forEach(
        point -> {
          List<Tile> tilesInRay = LevelUtils.ray(heroPos, point, 1, distance);
          boolean wallEncountered = false;

          for (Tile tile : tilesInRay) {
            if (!wallEncountered && !tile.canSeeThrough()) {
              wallEncountered = true;
              continue;
            }
            if (wallEncountered) {
              darkenTile(tile, point);
              currentDarkenedTiles.add(tile);
            }
          }
        });
    return currentDarkenedTiles;
  }

  private static void darkenTile(Tile tile, Point point) {
    if (!originalTextures.containsKey(tile)) {
      originalTextures.put(tile, tile.texturePath());
    }
    IPath newTexture =
        TileTextureFactory.findTexturePath(
            new TileTextureFactory.LevelPart(
                LevelElement.FLOOR,
                DesignLabel.DARK,
                new LevelElement[][] {},
                point.toCoordinate()));
    tile.texturePath(newTexture);
    FogOfWarSystem.darkenedTiles.add(tile);
  }

  private static void revertTilesBackToLight(Point heroPos, List<Tile> current, int distance) {
    Iterator<Tile> iterator = FogOfWarSystem.darkenedTiles.iterator();
    while (iterator.hasNext()) {
      Tile tile = iterator.next();
      // match non-current tile or tile that are far away
      if (!current.contains(tile) || heroPos.distance(tile.position()) > distance - 1) {
        IPath originalTexture = originalTextures.get(tile);
        tile.texturePath(originalTexture);
        iterator.remove();
      }
    }
  }

  private static void darkenTilesFurtherThanDistance(Point heroPos, int distance) {
    Tile[][] allTiles = Game.currentLevel().layout();
    for (Tile[] allTile : allTiles) {
      for (int x = 0; x < allTiles[0].length; x++) {
        Tile tile = allTile[x];
        if (heroPos.distance(tile.position()) > distance - 1) {
          darkenTile(tile, tile.position());
        }
      }
    }
  }

  private static void hideAllHiddenEntities() {
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

  private static void revealHiddenEntities() {
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

      List<Point> points = generatePointsAroundHero(heroPos, RESOLUTION);
      List<Tile> current = raycastToEachPoint(heroPos, points, VIEW_DISTANCE);
      revertTilesBackToLight(heroPos, current, VIEW_DISTANCE);
      darkenTilesFurtherThanDistance(heroPos, VIEW_DISTANCE);
      // entities
      hideAllHiddenEntities();
    }
    revealHiddenEntities();
  }
}
