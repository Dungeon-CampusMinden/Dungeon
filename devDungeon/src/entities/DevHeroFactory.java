package entities;

import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.math.Vector2;
import components.PathComponent;
import contrib.components.InteractionComponent;
import contrib.components.UIComponent;
import contrib.configuration.KeyboardConfig;
import contrib.entities.HeroFactory;
import contrib.hud.elements.GUICombination;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class DevHeroFactory extends HeroFactory {
  public static final boolean ENABLE_MOUSE_MOVEMENT = true;
  private static Skill SKILL =
      new Skill(new BurningFireballSkill(SkillTools::cursorPositionAsPoint), 500L);

  /**
   * Update the skill used by the hero.
   *
   * <p>This method should be called whenever the skill was modified, and needs to be updated.
   */
  public static void updateSkill() {
    SKILL = new Skill(new BurningFireballSkill(SkillTools::cursorPositionAsPoint), 500L);
  }

  public static Skill getSkill() {
    return SKILL;
  }

  public static Entity newHero() throws IOException {
    Entity hero = HeroFactory.newHero();
    PlayerComponent pc =
        hero.fetch(PlayerComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PlayerComponent.class));

    // hero movement again to allow for mouse movement
    registerMovement(pc, core.configuration.KeyboardConfig.MOVEMENT_UP.value(), new Vector2(0, 1));
    registerMovement(
        pc, core.configuration.KeyboardConfig.MOVEMENT_UP_SECOND.value(), new Vector2(0, 1));
    registerMovement(
        pc, core.configuration.KeyboardConfig.MOVEMENT_DOWN.value(), new Vector2(0, -1));
    registerMovement(
        pc, core.configuration.KeyboardConfig.MOVEMENT_DOWN_SECOND.value(), new Vector2(0, -1));
    registerMovement(
        pc, core.configuration.KeyboardConfig.MOVEMENT_RIGHT.value(), new Vector2(1, 0));
    registerMovement(
        pc, core.configuration.KeyboardConfig.MOVEMENT_RIGHT_SECOND.value(), new Vector2(1, 0));
    registerMovement(
        pc, core.configuration.KeyboardConfig.MOVEMENT_LEFT.value(), new Vector2(-1, 0));
    registerMovement(
        pc, core.configuration.KeyboardConfig.MOVEMENT_LEFT_SECOND.value(), new Vector2(-1, 0));

    pc.registerCallback(
        KeyboardConfig.FIRST_SKILL.value(), heroEntity -> SKILL.execute(heroEntity));

    // Mouse movement
    if (ENABLE_MOUSE_MOVEMENT) {
      // Mouse Left Click
      registerMouseLeftClick(pc);

      // Mouse Movement (Right Click)
      pc.registerCallback(
          core.configuration.KeyboardConfig.MOUSE_MOVE.value(),
          innerHero -> {
            // Small adjustment to get the correct tile
            Point mousePos = SkillTools.cursorPositionAsPoint();
            mousePos.x = mousePos.x - 0.5f;
            mousePos.y = mousePos.y - 0.25f;

            Point heroPos =
                innerHero
                    .fetch(PositionComponent.class)
                    .map(PositionComponent::position)
                    .orElse(null);
            if (heroPos == null) return;

            GraphPath<Tile> path = LevelUtils.calculatePath(heroPos, mousePos);
            // If the path is null or empty, try to find a nearby tile that is accessible and
            // calculate a path to it
            if (path == null || path.getCount() == 0) {
              Tile nearTile =
                  LevelUtils.tilesInRange(mousePos, 1f).stream()
                      .filter(tile -> LevelUtils.calculatePath(heroPos, tile.position()) != null)
                      .findFirst()
                      .orElse(null);
              // If no accessible tile is found, abort
              if (nearTile == null) return;
              path = LevelUtils.calculatePath(heroPos, nearTile.position());
            }

            // final, so it can be used in the lambda expression below
            final GraphPath<Tile> finalPath = path;
            innerHero
                .fetch(PathComponent.class)
                .ifPresentOrElse(
                    pathComponent -> pathComponent.path(finalPath),
                    () -> innerHero.add(new PathComponent(finalPath)));
          },
          false);
    }
    return hero;
  }

  private static void registerMouseLeftClick(PlayerComponent pc) {
    if (!Objects.equals(
        KeyboardConfig.MOUSE_FIRST_SKILL.value(), KeyboardConfig.MOUSE_INTERACT_WORLD.value())) {
      pc.registerCallback(
          KeyboardConfig.MOUSE_FIRST_SKILL.value(), hero -> SKILL.execute(hero), true, false);
      pc.registerCallback(
          KeyboardConfig.MOUSE_INTERACT_WORLD.value(),
          DevHeroFactory::handleInteractWithClosestInteractable,
          false,
          false);
    } else {
      // If interact and skill are the same, only one callback is needed, so we only interact if
      // interaction is possible
      pc.registerCallback(
          KeyboardConfig.MOUSE_INTERACT_WORLD.value(),
          (hero) -> {
            Point mousePosition = SkillTools.cursorPositionAsPoint();
            Entity interactable = checkIfClickOnInteractable(mousePosition).orElse(null);
            if (interactable == null || !interactable.isPresent(InteractionComponent.class)) {
              SKILL.execute(hero);
            } else {
              handleInteractWithClosestInteractable(hero);
            }
          },
          false,
          false);
    }
  }

  private static void handleInteractWithClosestInteractable(Entity hero) {
    UIComponent uiComponent = hero.fetch(UIComponent.class).orElse(null);
    if (uiComponent != null && uiComponent.dialog() instanceof GUICombination) {
      // close the dialog if already open
      hero.remove(UIComponent.class);
      return;
    }
    Point mousePosition = SkillTools.cursorPositionAsPoint();
    Entity interactable = checkIfClickOnInteractable(mousePosition).orElse(null);
    if (interactable == null) return;
    InteractionComponent ic =
        interactable
            .fetch(InteractionComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(interactable, InteractionComponent.class));
    PositionComponent pc =
        interactable
            .fetch(PositionComponent.class)
            .orElseThrow(
                () -> MissingComponentException.build(interactable, PositionComponent.class));
    PositionComponent heroPC =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    if (Point.calculateDistance(pc.position(), heroPC.position()) < ic.radius())
      ic.triggerInteraction(interactable, hero);
  }

  private static Optional<Entity> checkIfClickOnInteractable(Point pos)
      throws MissingComponentException {
    pos.x = pos.x - 0.5f;
    pos.y = pos.y - 0.25f;
    Tile mouseTile = Game.tileAT(pos);
    if (mouseTile == null) return Optional.empty();

    return Game.entityAtTile(mouseTile)
        .filter(e -> e.isPresent(InteractionComponent.class))
        .findFirst();
  }

  private static void registerMovement(PlayerComponent pc, int key, Vector2 direction) {
    pc.registerCallback(
        key,
        entity -> {
          VelocityComponent vc =
              entity
                  .fetch(VelocityComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(entity, VelocityComponent.class));
          if (direction.x != 0) {
            vc.currentXVelocity(direction.x * vc.xVelocity());
          }
          if (direction.y != 0) {
            vc.currentYVelocity(direction.y * vc.yVelocity());
          }
          // Abort any path finding on own movement
          if (ENABLE_MOUSE_MOVEMENT) {
            PathComponent pathComponent = entity.fetch(PathComponent.class).orElse(null);
            if (pathComponent == null) return;
            pathComponent.clear();
          }
        });
  }
}
