package core.network.messages.client2server;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.components.PathComponent;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.Point;

public record HeroTargetMoveCommand(Point targetPoint) implements ClientMessage {
  @Override
  public void process() {
    Game.hero()
        .ifPresent(
            hero -> {
              Point heroPos =
                  hero.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);
              if (heroPos == null) return;

              GraphPath<Tile> path = LevelUtils.calculatePath(heroPos, targetPoint);
              // If the path is null or empty, try to find a nearby tile that is accessible and
              // calculate a path to it
              if (path == null || path.getCount() == 0) {
                Tile nearTile =
                    LevelUtils.tilesInRange(targetPoint, 1f).stream()
                        .filter(tile -> LevelUtils.calculatePath(heroPos, tile.position()) != null)
                        .findFirst()
                        .orElse(null);
                // If no accessible tile is found, abort
                if (nearTile == null) return;
                path = LevelUtils.calculatePath(heroPos, nearTile.position());
              }

              // Stores the path in Hero's PathComponent
              GraphPath<Tile> finalPath = path;
              hero.fetch(PathComponent.class)
                  .ifPresentOrElse(
                      pathComponent -> pathComponent.path(finalPath),
                      () -> hero.add(new PathComponent(finalPath)));
            });
  }
}
