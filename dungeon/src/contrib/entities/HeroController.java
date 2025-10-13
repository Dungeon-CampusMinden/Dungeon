package contrib.entities;

import com.badlogic.gdx.ai.pfa.GraphPath;
import contrib.components.InteractionComponent;
import contrib.components.PathComponent;
import contrib.components.SkillComponent;
import contrib.utils.components.skill.cursorSkill.CursorSkill;
import contrib.utils.components.skill.projectileSkill.ProjectileSkill;
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
import java.util.stream.Stream;

public class HeroController {

  /** If true, the hero can be moved with the mouse. */
  public static final boolean ENABLE_MOUSE_MOVEMENT = true;

  /** The ID for the movement force. */
  public static final String MOVEMENT_ID = "Movement";

  private HeroController() {}

  public static void moveHero(Entity hero, Direction direction) {
    // TODO: get correct class
    CharacterClass heroClass = HeroFactory.DEFAULT_HERO_CLASS;

    VelocityComponent vc =
        hero.fetch(VelocityComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, VelocityComponent.class));

    Optional<Vector2> existingForceOpt = vc.force(MOVEMENT_ID);
    Vector2 newForce = heroClass.speed().scale(direction);

    Vector2 updatedForce =
        existingForceOpt.map(existing -> existing.add(newForce)).orElse(newForce);

    if (updatedForce.lengthSquared() > 0) {
      updatedForce = updatedForce.normalize().scale(heroClass.speed().length());
      vc.applyForce(MOVEMENT_ID, updatedForce);
    }

    if (ENABLE_MOUSE_MOVEMENT) {
      hero.fetch(PathComponent.class).ifPresent(PathComponent::clear);
    }
  }

  public static void moveHeroPath(Entity hero, Point target) {
    Point heroPos =
        hero.fetch(PositionComponent.class).map(PositionComponent::position).orElse(null);
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
    hero.fetch(PathComponent.class)
        .ifPresentOrElse(
            pathComponent -> pathComponent.path(finalPath),
            () -> hero.add(new PathComponent(finalPath)));
  }

  public static void useSkill(Entity hero, Point target) {
    hero.fetch(SkillComponent.class)
        .flatMap(SkillComponent::activeSkill)
        .ifPresent(
            skill -> {
              if (skill instanceof CursorSkill cursorSkill) {
                cursorSkill.cursorPositionSupplier(() -> target);
              } else if (skill instanceof ProjectileSkill projSkill) {
                projSkill.endPointSupplier(() -> target);
              }
              skill.execute(hero);
            });
  }

  /**
   * Handles interaction between the hero and an interactable entity. First attempts to find an
   * interactable entity at the specified point (e.g., mouse cursor position). If no interactable
   * entity is found or the entity is out of range, it searches within a 1-tile radius around the
   * hero. If an interactable entity is found and within its interaction radius, the interaction is
   * triggered.
   *
   * @param hero the hero entity attempting the interaction
   * @param point the target point where the interaction is attempted (e.g., cursor position)
   */
  public static void interact(Entity hero, Point point) {
    PositionComponent heroPc =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

    // Try finding interactable at the exact point first
    Optional<Entity> target =
        Game.tileAt(point)
            .map(Game::entityAtTile)
            .orElse(Stream.empty())
            .filter(e -> e.fetch(InteractionComponent.class).isPresent())
            .findFirst();

    // Check if target at point is in range
    boolean targetInRange = target.map(entity -> canInteract(entity, heroPc)).orElse(false);

    // If nothing found at point OR found but out of range, search in 1-tile radius around hero
    if (target.isEmpty() || !targetInRange) {
      target =
          LevelUtils.tilesInRange(heroPc.position(), 1f).stream()
              .flatMap(Game::entityAtTile)
              .filter(e -> e.fetch(InteractionComponent.class).isPresent())
              .findFirst();
    }

    // Trigger interaction if entity found and within interaction radius
    target.ifPresent(
        entity -> {
          InteractionComponent ic = entity.fetch(InteractionComponent.class).orElseThrow();
          PositionComponent targetPc =
              entity
                  .fetch(PositionComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(entity, PositionComponent.class));

          if (heroPc.position().distance(targetPc.position()) <= ic.radius()) {
            ic.triggerInteraction(entity, hero);
          }
        });
  }

  /**
   * Checks if the hero can interact with the given entity. Returns true if the entity has both
   * position and interaction components, and the hero is within the interaction radius.
   *
   * @param entity the entity to check
   * @param heroPc the hero's position component
   * @return true if interaction is possible, false otherwise
   */
  private static boolean canInteract(Entity entity, PositionComponent heroPc) {
    PositionComponent targetPc = entity.fetch(PositionComponent.class).orElse(null);
    InteractionComponent ic = entity.fetch(InteractionComponent.class).orElse(null);
    return targetPc != null
        && ic != null
        && heroPc.position().distance(targetPc.position()) <= ic.radius();
  }

  public static void changeSkill(Entity hero, boolean nextSkill) {
    hero.fetch(SkillComponent.class)
        .ifPresent(
            skillComponent -> {
              if (nextSkill) skillComponent.nextSkill();
              else skillComponent.prevSkill();
            });
  }
}
