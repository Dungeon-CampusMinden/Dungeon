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
import java.util.AbstractMap;
import java.util.Optional;

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
      hero
        .fetch(VelocityComponent.class)
        .orElseThrow(
          () -> MissingComponentException.build(hero, VelocityComponent.class));

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
            cursorSkill.executeOnCursor(hero, target);
          } else if (skill instanceof ProjectileSkill projSkill) {
            projSkill.endPointSupplier(() -> target);
            projSkill.execute(hero);
          } else {
            skill.execute(hero);
          }
        });
  }

  public static void interact(Entity hero, Point point) {
    Game.tileAt(point)
        .flatMap(
            tile ->
                Game.entityAtTile(tile)
                    .filter(e -> e.fetch(InteractionComponent.class).isPresent())
                    .findFirst()
                    .flatMap(
                        e ->
                            e.fetch(InteractionComponent.class)
                                .map(ic -> new AbstractMap.SimpleEntry<>(e, ic))))
        .ifPresent(pair -> pair.getValue().triggerInteraction(pair.getKey(), hero));
  }

  public static void changeSkill(Entity hero, boolean nextSkill) {
    hero.fetch(SkillComponent.class)
        .ifPresent(skillComponent -> {
          if (nextSkill) skillComponent.nextSkill();
          else skillComponent.prevSkill();
        });
  }
}
