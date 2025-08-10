package contrib.entities;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.components.InteractionComponent;
import contrib.components.PathComponent;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import core.utils.components.MissingComponentException;

import java.util.Optional;

public class HeroController {

  /** If true, the hero can be moved with the mouse. */
  public static final boolean ENABLE_MOUSE_MOVEMENT = true;

  /** The ID for the movement force. */
  public static final String MOVEMENT_ID = "Movement";

  private static final Vector2 STEP_SPEED = HeroFactory.defaultHeroSpeed();

  private HeroController() {}

  public static void moveHero(Entity hero, Direction direction) {
    VelocityComponent vc =
      hero
        .fetch(VelocityComponent.class)
        .orElseThrow(
          () -> MissingComponentException.build(hero, VelocityComponent.class));

    Optional<Vector2> existingForceOpt = vc.force(MOVEMENT_ID);
    Vector2 newForce = STEP_SPEED.scale(direction);

    Vector2 updatedForce =
      existingForceOpt.map(existing -> existing.add(newForce)).orElse(newForce);

    if (updatedForce.lengthSquared() > 0) {
      updatedForce = updatedForce.normalize().scale(STEP_SPEED.length());
      vc.applyForce(MOVEMENT_ID, updatedForce);
    }

    if (ENABLE_MOUSE_MOVEMENT) {
      hero.fetch(PathComponent.class).ifPresent(PathComponent::clear);
    }
  }

  public static void moveHeroPath(Entity hero, Point target) {
    Point heroPos =
      hero
        .fetch(PositionComponent.class)
        .map(PositionComponent::position)
        .orElse(null);
    if (heroPos == null) return;

    GraphPath<Tile> path = LevelUtils.calculatePath(heroPos, target);
    // If the path is null or empty, try to find a nearby tile that is accessible and
    // calculate a path to it
    if (path == null || path.getCount() == 0) {
      Tile nearTile =
        LevelUtils.tilesInRange(target, 1f).stream()
          .filter(tile -> LevelUtils.calculatePath(heroPos, tile.position()) != null)
          .findFirst()
          .orElse(null);
      // If no accessible tile is found, abort
      if (nearTile == null) return;
      path = LevelUtils.calculatePath(heroPos, nearTile.position());
    }

    // Stores the path in Hero's PathComponent
    GraphPath<Tile> finalPath = path;
    hero
      .fetch(PathComponent.class)
      .ifPresentOrElse(
        pathComponent -> pathComponent.path(finalPath),
        () -> hero.add(new PathComponent(finalPath)));
  }

  public static void useSkill(Entity hero, int skillId, Point target) {
    // TODO: Implement logic to use skillId; Use target to determine direction
    HeroFactory.getHeroSkill().execute(hero);
  }

  public static void interact(Entity hero, Point point) {
    Game.entityAtTile(Game.tileAT(point))
      .findFirst()
      .ifPresent(
        interactable -> {
          interactable
            .fetch(InteractionComponent.class)
            .ifPresent(ic -> ic.triggerInteraction(interactable, hero));
        });
  }
}
