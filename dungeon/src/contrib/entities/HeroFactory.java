package contrib.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import contrib.components.*;
import contrib.configuration.KeyboardConfig;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.utils.components.health.Damage;
import contrib.utils.components.interaction.InteractionTool;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.*;
import core.level.Tile;
import core.level.utils.LevelUtils;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

/** A utility class for building the hero entity in the game world. */
public final class HeroFactory {

  public static final boolean ENABLE_MOUSE_MOVEMENT = true;
  private static final int DEFAULT_INVENTORY_SIZE = 6;
  private static final IPath HERO_FILE_PATH = new SimpleIPath("character/wizard");
  private static final Vector2 SPEED_HERO = new Vector2(7.5f, 7.5f);
  private static final int FIREBALL_COOL_DOWN = 500;
  private static final int HERO_HP = 20;

  /**
   * Get an Entity that can be used as a playable character.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link * CameraComponent}, {@link core.components.PlayerComponent}. {@link
   * PositionComponent}, {@link * VelocityComponent} {@link core.components.DrawComponent}, {@link
   * contrib.components.CollideComponent} and {@link * HealthComponent}.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newHero() throws IOException {
    Entity hero = new Entity("hero");
    CameraComponent cc = new CameraComponent();
    hero.add(cc);
    PositionComponent poc = new PositionComponent();
    hero.add(poc);
    hero.add(new VelocityComponent(SPEED_HERO.x, SPEED_HERO.y, entity -> {}, true));
    hero.add(new DrawComponent(HERO_FILE_PATH));
    HealthComponent hc =
        new HealthComponent(
            HERO_HP,
            entity -> {
              // play sound
              Sound sound = Gdx.audio.newSound(Gdx.files.internal("sounds/death.wav"));
              long soundId = sound.play();
              sound.setLooping(soundId, false);
              sound.setVolume(soundId, 0.3f);
              sound.setLooping(soundId, false);
              sound.play();
              sound.setVolume(soundId, 0.9f);

              // relink components for camera
              Entity cameraDummy = new Entity();
              cameraDummy.add(cc);
              cameraDummy.add(poc);
              Game.add(cameraDummy);
            });
    hero.add(hc);
    hero.add(
        new CollideComponent(
            (you, other, direction) ->
                other
                    .fetch(SpikyComponent.class)
                    .ifPresent(
                        spikyComponent -> {
                          if (spikyComponent.isActive()) {
                            hc.receiveHit(
                                new Damage(
                                    spikyComponent.damageAmount(),
                                    spikyComponent.damageType(),
                                    other));
                            spikyComponent.activateCoolDown();
                          }
                        }),
            (you, other, direction) -> {}));

    PlayerComponent pc = new PlayerComponent();
    hero.add(pc);
    InventoryComponent ic = new InventoryComponent(DEFAULT_INVENTORY_SIZE);
    hero.add(ic);
    Skill fireball =
        new Skill(new FireballSkill(SkillTools::cursorPositionAsPoint), FIREBALL_COOL_DOWN);

    // hero movement
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
    // Mouse movement
    if (ENABLE_MOUSE_MOVEMENT) {
      // Mouse Left Click
      registerMouseLeftClick(pc, fireball);

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

    pc.registerCallback(
        KeyboardConfig.INVENTORY_OPEN.value(),
        (e) -> {
          UIComponent uiComponent = e.fetch(UIComponent.class).orElse(null);
          if (uiComponent != null) {
            if (uiComponent.dialog() instanceof GUICombination) {
              InventoryGUI.inHeroInventory = false;
              e.remove(UIComponent.class);
            }
          } else {
            InventoryGUI.inHeroInventory = true;
            e.add(new UIComponent(new GUICombination(new InventoryGUI(ic)), true));
          }
        },
        false);

    pc.registerCallback(
        KeyboardConfig.CLOSE_UI.value(),
        (e) -> {
          var firstUI =
              Game.entityStream() // would be nice to directly access HudSystems
                  // stream (no access to the System object)
                  .filter(x -> x.isPresent(UIComponent.class)) // find all Entities
                  // which have a
                  // UIComponent
                  .map(
                      x ->
                          new Tuple<>(
                              x,
                              x.fetch(UIComponent.class)
                                  .orElseThrow(
                                      () ->
                                          MissingComponentException.build(
                                              x, UIComponent.class)))) // create a tuple to
                  // still have access to
                  // the UI Entity
                  .filter(x -> x.b().closeOnUICloseKey())
                  .max(Comparator.comparingInt(x -> x.b().dialog().getZIndex())) // find dialog
                  // with highest
                  // z-Index
                  .orElse(null);
          if (firstUI != null) {
            InventoryGUI.inHeroInventory = false;
            firstUI.a().remove(UIComponent.class);
            if (firstUI.a().componentStream().findAny().isEmpty()) {
              Game.remove(firstUI.a()); // delete unused Entity
            }
          }
        },
        false,
        true);

    pc.registerCallback(
        KeyboardConfig.INTERACT_WORLD.value(),
        InteractionTool::interactWithClosestInteractable,
        false);

    // skills
    pc.registerCallback(KeyboardConfig.FIRST_SKILL.value(), fireball::execute);

    // hud
    //    UIComponent uiComponent =
    //        new UIComponent(new GUICombination(new InventoryGUI("", ic, 3, false)), false, false);
    //    hero.add(uiComponent);
    return hero;
  }

  private static void registerMouseLeftClick(PlayerComponent pc, Skill fireball) {
    if (!Objects.equals(
        KeyboardConfig.MOUSE_FIRST_SKILL.value(), KeyboardConfig.MOUSE_INTERACT_WORLD.value())) {
      pc.registerCallback(KeyboardConfig.MOUSE_FIRST_SKILL.value(), fireball::execute, true, false);
      pc.registerCallback(
          KeyboardConfig.MOUSE_INTERACT_WORLD.value(),
          HeroFactory::handleInteractWithClosestInteractable,
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
              fireball.execute(hero);
            } else {
              handleInteractWithClosestInteractable(hero);
            }
          },
          true,
          false);
    }
  }

  private static void handleInteractWithClosestInteractable(Entity hero) {
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
