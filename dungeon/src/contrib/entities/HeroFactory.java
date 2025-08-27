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
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import contrib.utils.components.skill.damageSkill.projectile.FireballSkill;
import core.Entity;
import core.Game;
import core.components.*;
import core.level.Tile;
import core.level.loader.DungeonLoader;
import core.level.utils.LevelUtils;
import core.utils.*;
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
  private static final Vector2 STEP_SPEED = Vector2.of(5, 5);
  private static final int HERO_HP = 25;
  private static final float HERO_MAX_SPEED = STEP_SPEED.x();
  private static final String MOVEMENT_ID = "Movement";
  private static final float HERO_MASS = 1.3f;
  private static Skill HERO_SKILL = FireballSkill.fireballSkill(SkillTools::cursorPositionAsPoint);

  private static Consumer<Entity> HERO_DEATH =
      (hero) ->
          DialogUtils.showTextPopup(
              "You died!",
              "Game Over",
              () -> {
                hero.fetch(HealthComponent.class)
                    .ifPresent(hc -> hc.currentHealthpoints(hc.maximalHealthpoints()));
                // reset the animation queue
                hero.fetch(DrawComponent.class).ifPresent(DrawComponent::deQueueAll);
                DungeonLoader.reloadCurrentLevel();
              });

  /**
   * The default speed of the hero.
   *
   * @return Copy of the default speed of the hero.
   */
  public static Vector2 defaultHeroSpeed() {
    return Vector2.of(STEP_SPEED);
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
   * <p>It will have a {@link CameraComponent}, {@link PlayerComponent}{, {@link PlayerComponent},
   * {@link PositionComponent}, {@link VelocityComponent}, {@link DrawComponent}, {@link
   * CollideComponent} and {@link HealthComponent}.
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
   * <p>It will have a {@link CameraComponent}, {@link PlayerComponent}, {@link InputComponent}
   * {@link PositionComponent}, {@link VelocityComponent}, {@link DrawComponent}, {@link
   * CollideComponent} and {@link HealthComponent}.
   *
   * @param deathCallback function that will be executed if the hero dies
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newHero(Consumer<Entity> deathCallback) throws IOException {
    Entity hero = new Entity("hero");
    hero.persistent(true);
    PlayerComponent pc = new PlayerComponent();
    hero.add(pc);
    CameraComponent cc = new CameraComponent();
    hero.add(cc);
    PositionComponent poc = new PositionComponent();
    hero.add(poc);
    hero.add(new VelocityComponent(HERO_MAX_SPEED, HERO_MASS, (e) -> {}, true));
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
              Entity cameraDummy = new Entity("heroCamera");
              cameraDummy.add(cc);
              cameraDummy.add(poc);
              Game.add(cameraDummy);

              deathCallback.accept(entity);
            });
    hero.add(hc);
    CollideComponent col =
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
            CollideComponent.DEFAULT_COLLIDER);
    col.onHold(
        (you, other, direction) -> {
          if (other.isPresent(KineticComponent.class)) resolveCollisionWithMomentum(hero, other);
        });
    hero.add(col);

    InputComponent inputComp = new InputComponent();
    hero.add(
        new CatapultableComponent(
            entity -> inputComp.deactivateControls(true),
            entity -> inputComp.deactivateControls(false)));
    hero.add(inputComp);
    InventoryComponent invComp = new InventoryComponent(DEFAULT_INVENTORY_SIZE);
    hero.add(invComp);

    // hero movement
    registerMovement(
        inputComp, core.configuration.KeyboardConfig.MOVEMENT_UP.value(), Vector2.of(0, 1));
    registerMovement(
        inputComp, core.configuration.KeyboardConfig.MOVEMENT_DOWN.value(), Vector2.of(0, -1));
    registerMovement(
        inputComp, core.configuration.KeyboardConfig.MOVEMENT_RIGHT.value(), Vector2.of(1, 0));
    registerMovement(
        inputComp, core.configuration.KeyboardConfig.MOVEMENT_LEFT.value(), Vector2.of(-1, 0));

    if (ENABLE_MOUSE_MOVEMENT) {
      // Mouse Left Click
      registerMouseLeftClick(inputComp);

      // Mouse Movement (Right Click)
      inputComp.registerCallback(
          KeyboardConfig.MOUSE_MOVE.value(),
          innerHero -> {
            // Small adjustment to get the correct tile
            Point mousePos =
                SkillTools.cursorPositionAsPoint().translate(Vector2.of(-0.5f, -0.25f));

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
    inputComp.registerCallback(
        KeyboardConfig.INVENTORY_OPEN.value(),
        (entity) -> toggleInventory(entity, pc, invComp),
        false,
        true);

    if (ENABLE_MOUSE_MOVEMENT) {
      inputComp.registerCallback(
          KeyboardConfig.MOUSE_INVENTORY_TOGGLE.value(),
          (entity) -> toggleInventory(entity, pc, invComp),
          false,
          true);
    }

    registerCloseUI(inputComp);

    inputComp.registerCallback(
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
    inputComp.registerCallback(
        KeyboardConfig.FIRST_SKILL.value(), heroEntity -> HERO_SKILL.execute(heroEntity));

    return hero;
  }

  /**
   * Registers a callback for closing the UI when the CLOSE_UI key is pressed.
   *
   * <p>This will close the topmost UI dialog that has the close key configured to close it.
   *
   * @param ic The {@link InputComponent} of the hero.
   */
  public static void registerCloseUI(InputComponent ic) {
    ic.registerCallback(
        KeyboardConfig.CLOSE_UI.value(),
        (e) -> {
          var firstUI =
              Game.levelEntities() // would be nice to directly access HudSystems
                  // stream (no access to the System object)
                  .filter(x -> x.isPresent(UIComponent.class)) // find all Entities
                  // that have a
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

  private static void registerMovement(InputComponent ic, int key, Vector2 direction) {
    ic.registerCallback(
        key,
        entity -> {
          VelocityComponent vc =
              entity
                  .fetch(VelocityComponent.class)
                  .orElseThrow(
                      () -> MissingComponentException.build(entity, VelocityComponent.class));

          Optional<Vector2> existingForceOpt = vc.force(MOVEMENT_ID);
          Vector2 newForce = STEP_SPEED.scale(direction);

          Vector2 updatedForce =
              existingForceOpt.map(existing -> existing.add(newForce)).orElse(newForce);

          if (updatedForce.lengthSquared() > 0) {
            updatedForce = updatedForce.normalize().scale(STEP_SPEED.length());
            vc.applyForce(MOVEMENT_ID, updatedForce);
          }

          if (ENABLE_MOUSE_MOVEMENT) {
            entity.fetch(PathComponent.class).ifPresent(PathComponent::clear);
          }
        });
  }

  private static void registerMouseLeftClick(InputComponent ic) {
    if (!Objects.equals(
        KeyboardConfig.MOUSE_FIRST_SKILL.value(), KeyboardConfig.MOUSE_INTERACT_WORLD.value())) {
      ic.registerCallback(
          KeyboardConfig.MOUSE_FIRST_SKILL.value(), hero -> HERO_SKILL.execute(hero), true, false);
      ic.registerCallback(
          KeyboardConfig.MOUSE_INTERACT_WORLD.value(),
          HeroFactory::handleInteractWithClosestInteractable,
          false,
          false);
    } else {
      // If interact and skill are the same, only one callback can be used, so we only interact if
      // interaction is possible
      ic.registerCallback(
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
    pos = pos.translate(Vector2.of(-0.5f, -0.25f));

    Tile mouseTile = Game.tileAt(pos).orElse(null);
    if (mouseTile == null) return Optional.empty();

    return Game.entityAtTile(mouseTile)
        .filter(e -> e.isPresent(InteractionComponent.class))
        .findFirst();
  }

  private static void resolveCollisionWithMomentum(Entity hero, Entity other) {
    Optional<VelocityComponent> optVc1 = hero.fetch(VelocityComponent.class);
    Optional<VelocityComponent> optVc2 = other.fetch(VelocityComponent.class);

    if (optVc1.isEmpty() || optVc2.isEmpty()) return;

    VelocityComponent vc1 = optVc1.get();
    VelocityComponent vc2 = optVc2.get();

    Vector2 v1 = vc1.currentVelocity();
    Vector2 v2 = vc2.currentVelocity();
    float m1 = vc1.mass();
    float m2 = vc2.mass();

    Vector2 p1 = v1.scale(m1); // Impuls Entity 1
    Vector2 p2 = v2.scale(m2); // Impuls Entity 2

    Vector2 p = p1.add(p2); // Total Impuls
    Vector2 v = p.scale(1f / (m1 + m2)); // New velocity after collision

    double length = v.length(); // Länge des Vektors behalten (neue Geschwindigkeit)

    Direction d = v.direction(); // Richtung des Vektors nach Quadranten bestimmen

    Vector2 newVelocity = d.scale(length); // Neue Geschwindigkeit in Richtung des Vektors
    vc1.applyForce("Collision", newVelocity.scale(-1));
    vc2.applyForce("Collision", newVelocity);
  }
}
