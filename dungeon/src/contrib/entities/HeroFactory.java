package contrib.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.audio.Sound;
import contrib.components.*;
import contrib.configuration.KeyboardConfig;
import contrib.hud.DialogUtils;
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
import core.utils.IVector2;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.components.MissingComponentException;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/** A utility class for building the hero entity in the game world. */
public final class HeroFactory {

  /** If true, the hero can be moved with the mouse. */
  public static final boolean ENABLE_MOUSE_MOVEMENT = true;

  /**
   * The default size of the inventory.
   *
   * @see InventoryComponent
   */
  public static final int DEFAULT_INVENTORY_SIZE = 6;

  private static final IPath HERO_FILE_PATH = new SimpleIPath("character/wizard");
  private static final IVector2 SPEED_HERO = IVector2.of(7.5f, 7.5f);
  private static final int FIREBALL_COOL_DOWN = 500;
  private static final int HERO_HP = 25;
  private static Skill HERO_SKILL =
      new Skill(new FireballSkill(SkillTools::cursorPositionAsPoint), FIREBALL_COOL_DOWN);

  private static Consumer<Entity> HERO_DEATH =
      (entity) -> {
        DialogUtils.showTextPopup("You died!", "Game Over", Game::exit);
      };

  /**
   * The default speed of the hero.
   *
   * @return Copy of the default speed of the hero.
   */
  public static IVector2 defaultHeroSpeed() {
    return IVector2.of(SPEED_HERO);
  }

  /**
   * Gets the current skill of the hero.
   *
   * @return The current skill of the hero.
   */
  public static Skill getHeroSkill() {
    return HERO_SKILL;
  }

  /**
   * Set the skill callback for the hero. The skill callback is executed when the hero uses the
   * skill.
   *
   * <p>By default, the hero uses the {@link FireballSkill}.
   *
   * @param skillCallback The skill callback.
   * @see Skill#Skill(Consumer, long)
   */
  public static void setHeroSkillCallback(Consumer<Entity> skillCallback) {
    HERO_SKILL = new Skill(skillCallback, FIREBALL_COOL_DOWN);
  }

  /**
   * Sets the callback to execute when the hero dies.
   *
   * @param deathCallback Callback that will be executed on the hero's death.
   */
  public static void heroDeath(Consumer<Entity> deathCallback) {
    HERO_DEATH = deathCallback;
  }

  /**
   * Get an Entity that can be used as a playable character.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link CameraComponent}, {@link core.components.PlayerComponent}. {@link
   * PositionComponent}, {@link VelocityComponent}, {@link core.components.DrawComponent}, {@link
   * contrib.components.CollideComponent} and {@link HealthComponent}.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newHero() throws IOException {
    return newHero(HERO_DEATH);
  }

  /**
   * Get an Entity that can be used as a playable character.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link CameraComponent}, {@link core.components.PlayerComponent}. {@link
   * PositionComponent}, {@link VelocityComponent}, {@link core.components.DrawComponent}, {@link
   * contrib.components.CollideComponent} and {@link HealthComponent}.
   *
   * @param deathCallback function that will be executed if the hero dies
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newHero(Consumer<Entity> deathCallback) throws IOException {
    Entity hero = new Entity("hero");
    CameraComponent cc = new CameraComponent();
    hero.add(cc);
    PositionComponent poc = new PositionComponent();
    hero.add(poc);
    hero.add(new VelocityComponent(SPEED_HERO.x(), SPEED_HERO.y(), (e) -> {}, true));
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

              deathCallback.accept(entity);
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

    // hero movement
    registerMovement(pc, core.configuration.KeyboardConfig.MOVEMENT_UP.value(), IVector2.of(0, 1));
    registerMovement(
        pc, core.configuration.KeyboardConfig.MOVEMENT_DOWN.value(), IVector2.of(0, -1));
    registerMovement(
        pc, core.configuration.KeyboardConfig.MOVEMENT_RIGHT.value(), IVector2.of(1, 0));
    registerMovement(
        pc, core.configuration.KeyboardConfig.MOVEMENT_LEFT.value(), IVector2.of(-1, 0));

    if (ENABLE_MOUSE_MOVEMENT) {
      // Mouse Left Click
      registerMouseLeftClick(pc);

      // Mouse Movement (Right Click)
      pc.registerCallback(
          KeyboardConfig.MOUSE_MOVE.value(),
          innerHero -> {
            // Small adjustment to get the correct tile
            Point mousePos =
                SkillTools.cursorPositionAsPoint().translate(IVector2.of(-0.5f, -0.25f));

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

            // Stores the path in Hero's PathComponent
            GraphPath<Tile> finalPath = path;
            innerHero
                .fetch(PathComponent.class)
                .ifPresentOrElse(
                    pathComponent -> pathComponent.path(finalPath),
                    () -> innerHero.add(new PathComponent(finalPath)));
          },
          false);
    }

    // UI controls
    pc.registerCallback(
        KeyboardConfig.INVENTORY_OPEN.value(),
        (entity) -> {
          toggleInventory(entity, pc, ic);
        },
        false,
        true);

    if (ENABLE_MOUSE_MOVEMENT) {
      pc.registerCallback(
          KeyboardConfig.MOUSE_INVENTORY_TOGGLE.value(),
          (entity) -> {
            toggleInventory(entity, pc, ic);
          },
          false,
          true);
    }

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
        entity -> {
          UIComponent uiComponent = entity.fetch(UIComponent.class).orElse(null);
          if (uiComponent != null
              && uiComponent.dialog() instanceof GUICombination
              && !InventoryGUI.inHeroInventory) {
            // if chest or cauldron
            entity.remove(UIComponent.class);
          } else {
            InteractionTool.interactWithClosestInteractable(entity);
          }
        },
        false);

    // skills
    pc.registerCallback(
        KeyboardConfig.FIRST_SKILL.value(), heroEntity -> HERO_SKILL.execute(heroEntity));

    return hero;
  }

  private static void toggleInventory(Entity entity, PlayerComponent pc, InventoryComponent ic) {
    if (pc.openDialogs()) {
      return;
    }

    UIComponent uiComponent = entity.fetch(UIComponent.class).orElse(null);
    if (uiComponent != null) {
      if (uiComponent.dialog() instanceof GUICombination) {
        InventoryGUI.inHeroInventory = false;
        entity.remove(UIComponent.class);
      }
    } else {
      InventoryGUI.inHeroInventory = true;
      entity.add(new UIComponent(new GUICombination(new InventoryGUI(ic)), true));
    }
  }

  private static void registerMovement(PlayerComponent pc, int key, IVector2 direction) {
    pc.registerCallback(
        key,
        entity -> {
          VelocityComponent vc =
              entity
                  .fetch(VelocityComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(entity, VelocityComponent.class));
          if (direction.x() != 0) {
            vc.currentXVelocity(direction.x() * vc.xVelocity());
          }
          if (direction.y() != 0) {
            vc.currentYVelocity(direction.y() * vc.yVelocity());
          }
          // Abort any path finding on own movement
          if (ENABLE_MOUSE_MOVEMENT) {
            entity.fetch(PathComponent.class).ifPresent(PathComponent::clear);
          }
        });
  }

  private static void registerMouseLeftClick(PlayerComponent pc) {
    if (!Objects.equals(
        KeyboardConfig.MOUSE_FIRST_SKILL.value(), KeyboardConfig.MOUSE_INTERACT_WORLD.value())) {
      pc.registerCallback(
          KeyboardConfig.MOUSE_FIRST_SKILL.value(), hero -> HERO_SKILL.execute(hero), true, false);
      pc.registerCallback(
          KeyboardConfig.MOUSE_INTERACT_WORLD.value(),
          HeroFactory::handleInteractWithClosestInteractable,
          false,
          false);
    } else {
      // If interact and skill are the same, only one callback can be used, so we only interact if
      // interaction is possible
      pc.registerCallback(
          KeyboardConfig.MOUSE_INTERACT_WORLD.value(),
          (hero) -> {
            Point mousePosition = SkillTools.cursorPositionAsPoint();
            Entity interactable = checkIfClickOnInteractable(mousePosition).orElse(null);
            if (interactable == null || !interactable.isPresent(InteractionComponent.class)) {
              HERO_SKILL.execute(hero);
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
    pos = pos.translate(IVector2.of(-0.5f, -0.25f));

    Tile mouseTile = Game.tileAT(pos);
    if (mouseTile == null) return Optional.empty();

    return Game.entityAtTile(mouseTile)
        .filter(e -> e.isPresent(InteractionComponent.class))
        .findFirst();
  }
}
