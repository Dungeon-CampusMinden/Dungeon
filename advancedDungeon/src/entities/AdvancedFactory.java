package entities;

import components.AntiMaterialBarrierComponent;
import components.LasergridComponent;
import components.PortalCubeComponent;
import components.PortalSphereComponent;
import components.ToggleableComponent;
import components.ai.PelletLauncherBehaviour;
import contrib.components.*;
import contrib.components.CollideComponent;
import contrib.components.SpikyComponent;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.utils.ICommand;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.skill.Skill;
import contrib.utils.components.skill.SkillTools;
import core.Component;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import produsAdvanced.abstraction.portals.PortalFactory;
import produsAdvanced.abstraction.portals.components.PortalExtendComponent;
import produsAdvanced.abstraction.portals.components.TractorBeamComponent;
import skills.EnergyPelletSkill;

/**
 * A utility class for building different miscellaneous entities in the game world of the advanced
 * dungeon.
 */
public class AdvancedFactory {

  private static final SimpleIPath LASER_GRID = new SimpleIPath("portal/laser_grid");
  private static final int LASER_GRID_DMG = 9999;
  private static final int LASER_GRID_CD = 10;

  private static final SimpleIPath ANTI_MATERIAL_BARRIER =
      new SimpleIPath("portal/anti_material_barrier");

  private static final SimpleIPath PORTAL_CUBE = new SimpleIPath("portal/portal_cube.png");
  private static final float cube_mass = 3f;
  private static final float cube_maxSpeed = 10f;

  private static final SimpleIPath PORTAL_SPHERE = new SimpleIPath("portal/kubus");
  private static final float sphere_mass = 3f;
  private static final float sphere_maxSpeed = 10f;
  private static final SimpleIPath CUBE_PRESSURE_PLATE = new SimpleIPath("objects/pressureplate");
  private static final SimpleIPath SPHERE_PRESSURE_PLATE =
      new SimpleIPath("portal/kubus_pressureplate");

  private static final SimpleIPath PELLET_LAUNCHER = new SimpleIPath("portal/pellet_launcher");
  private static final SimpleIPath PELLET_CATCHER = new SimpleIPath("portal/pellet_catcher");
  private static int launcherNumber = 0;

  /**
   * Creates a laser grid entity at the given position.
   *
   * <p>Objects can pass through it but the hero dies upon collision.
   *
   * @param spawnPoint The position the laser grid will be spawned.
   * @param horizontal whether the laser grid texture ist aligned horizontal or not (will be aligned
   *     vertical if false).
   * @return a new laser grid entity.
   */
  public static Entity laserGrid(Point spawnPoint, boolean horizontal) {
    Entity grid = new Entity("laserGrid");
    grid.add(new PositionComponent(spawnPoint));
    grid.add(new LasergridComponent(true));

    // the laser grid can't be solid to let objects pass
    CollideComponent colComp = new CollideComponent();
    colComp.isSolid(false);
    grid.add(colComp);

    grid.add(new SpikyComponent(LASER_GRID_DMG, DamageType.PHYSICAL, LASER_GRID_CD));
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(LASER_GRID);

    DrawComponent dc;

    if (horizontal) {
      State stHorizontalOn = State.fromMap(animationMap, "horizontal_on");
      State stHorizontalOff = State.fromMap(animationMap, "horizontal_off");
      StateMachine sm = new StateMachine(Arrays.asList(stHorizontalOn, stHorizontalOff));
      sm.addTransition(stHorizontalOn, "deactivate_laser_grid", stHorizontalOff);
      sm.addTransition(stHorizontalOff, "activate_laser_grid", stHorizontalOn);
      dc = new DrawComponent(sm);
    } else {
      State stVerticalOn = State.fromMap(animationMap, "vertical_on");
      State stVerticalOff = State.fromMap(animationMap, "vertical_off");
      StateMachine sm = new StateMachine(Arrays.asList(stVerticalOn, stVerticalOff));
      sm.addTransition(stVerticalOn, "deactivate_laser_grid", stVerticalOff);
      sm.addTransition(stVerticalOff, "activate_laser_grid", stVerticalOn);
      dc = new DrawComponent(sm);
    }

    grid.add(dc);

    return grid;
  }

  /**
   * Creates an anti-material barrier entity at the given position.
   *
   * <p>The hero can pass through it but all objects disintegrate upon collision.
   *
   * @param spawnPoint The position the barrier will be spawned.
   * @param horizontal whether the barrier texture ist aligned horizontal or not (will be aligned
   *     vertical if false).
   * @return a new anti-material barrier entity.
   */
  public static Entity antiMaterialBarrier(Point spawnPoint, boolean horizontal) {
    Entity barrier = new Entity("antiMaterialBarrier");
    barrier.add(new PositionComponent(spawnPoint));
    barrier.add(new AntiMaterialBarrierComponent(true));

    CollideComponent colComp = getCollideComponent();
    barrier.add(colComp);

    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(ANTI_MATERIAL_BARRIER);

    DrawComponent dc;

    if (horizontal) {
      State stHorizontalOn = State.fromMap(animationMap, "horizontal_on");
      State stHorizontalOff = State.fromMap(animationMap, "horizontal_off");
      StateMachine sm = new StateMachine(Arrays.asList(stHorizontalOn, stHorizontalOff));
      sm.addTransition(stHorizontalOn, "deactivate_anti_barrier", stHorizontalOff);
      sm.addTransition(stHorizontalOff, "activate_anti_barrier", stHorizontalOn);
      dc = new DrawComponent(sm);
    } else {
      State stVerticalOn = State.fromMap(animationMap, "vertical_on");
      State stVerticalOff = State.fromMap(animationMap, "vertical_off");
      StateMachine sm = new StateMachine(Arrays.asList(stVerticalOn, stVerticalOff));
      sm.addTransition(stVerticalOn, "deactivate_anti_barrier", stVerticalOff);
      sm.addTransition(stVerticalOff, "activate_anti_barrier", stVerticalOn);
      dc = new DrawComponent(sm);
    }

    barrier.add(dc);

    return barrier;
  }

  /**
   * Creates the CollideComponent for the AntiMaterialBarrier.
   *
   * @return the new CollideComponent.
   */
  public static CollideComponent getCollideComponent() {
    TriConsumer<Entity, Entity, Direction> action =
        (self, other, direction) -> {
          String otherEntityName = other.name();
          if (other.isPresent(PlayerComponent.class)) {
            PortalFactory.clearAllPortals();
          } else if (other.isPresent(TractorBeamComponent.class)
              || other.isPresent(PortalExtendComponent.class)
              || otherEntityName.contains("energyPelletLauncher")) {
            // do nothing
          } else {
            Game.remove(other);
          }
        };

    // the barrier can't be solid to let the Hero pass
    CollideComponent colComp = new CollideComponent(action, CollideComponent.DEFAULT_COLLIDER);
    colComp.isSolid(false);
    return colComp;
  }

  /**
   * Creates a portal cube entity at the given position.
   *
   * @param position The initial position of the portal cube.
   * @return A new portal cube entity.
   */
  public static Entity attachablePortalCube(Point position) {
    Entity portalCube = new Entity("attachablePortalCube");
    portalCube.add(new PortalCubeComponent());
    portalCube.add(new PositionComponent(position));
    portalCube.add(new VelocityComponent(cube_maxSpeed, cube_mass, entity -> {}, false));
    portalCube.add(new DrawComponent(new Animation(PORTAL_CUBE)));

    final boolean[] attached = {false};
    CollideComponent cc = new CollideComponent();
    cc.collideLeave(
        (self, other, dir) -> {
          if (!cc.isSolid() && !attached[0]) {
            cc.isSolid(true);
          }
        });
    portalCube.add(cc);

    portalCube.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (interacted, interactor) -> {
                      PositionComponent interactorPositioncomponent =
                          interactor.fetch(PositionComponent.class).orElseThrow();
                      PositionComponent interactedPositioncomponent =
                          interacted.fetch(PositionComponent.class).orElseThrow();
                      if (!attached[0]) {
                        AttachmentComponent attachmentComponent =
                            new AttachmentComponent(
                                Vector2.ZERO,
                                interactedPositioncomponent,
                                interactorPositioncomponent);
                        portalCube.add(attachmentComponent);
                        cc.isSolid(false);
                        attached[0] = true;
                      } else {
                        portalCube.remove(AttachmentComponent.class);
                        Game.tileAt(interactedPositioncomponent.coordinate())
                            .ifPresent(
                                tile -> {
                                  if (tile.levelElement() == LevelElement.WALL
                                      || tile.levelElement() == LevelElement.GITTER
                                      || tile.levelElement() == LevelElement.GLASSWALL
                                      || tile.levelElement() == LevelElement.PORTAL) {
                                    interactedPositioncomponent.position(
                                        interactorPositioncomponent.position());
                                  }
                                });
                        attached[0] = false;
                      }
                    },
                    2f)));

    return portalCube;
  }

  /**
   * Creates a sphere which can be moved by walking into it.
   *
   * @param position the position where the sphere will spawn.
   * @return the sphere entity
   */
  public static Entity moveableSphere(Point position) {
    Entity sphere = new Entity("moveableSphere");
    sphere.add(new PortalSphereComponent());
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(PORTAL_SPHERE);

    State stIdle = new State("idle", animationMap.get("idle"));
    State stMove = new State("move", animationMap.get("move"));
    StateMachine sm = new StateMachine(Arrays.asList(stIdle, stMove));

    sm.addTransition(stIdle, "move", stMove);
    sm.addTransition(stMove, "move", stMove);
    sm.addTransition(stMove, "idle", stIdle);

    DrawComponent dc = new DrawComponent(sm);
    sphere.add(dc);
    sphere.add(new PositionComponent(position));
    sphere.add(new VelocityComponent(sphere_maxSpeed, sphere_mass, entity -> {}, false));

    final boolean[] attached = {false};
    CollideComponent cc = new CollideComponent();
    cc.collideLeave(
        (self, other, dir) -> {
          if (!cc.isSolid() && !attached[0]) {
            cc.isSolid(true);
          }
        });
    sphere.add(cc);

    sphere.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (interacted, interactor) -> {
                      PositionComponent interactorPositioncomponent =
                          interactor.fetch(PositionComponent.class).orElseThrow();
                      PositionComponent interactedPositioncomponent =
                          interacted.fetch(PositionComponent.class).orElseThrow();
                      if (!attached[0]) {
                        AttachmentComponent attachmentComponent =
                            new AttachmentComponent(
                                Vector2.ZERO,
                                interactedPositioncomponent,
                                interactorPositioncomponent);
                        sphere.add(attachmentComponent);
                        cc.isSolid(false);
                        attached[0] = true;
                      } else {
                        sphere.remove(AttachmentComponent.class);
                        Game.tileAt(interactedPositioncomponent.coordinate())
                            .ifPresent(
                                tile -> {
                                  if (tile.levelElement() == LevelElement.WALL
                                      || tile.levelElement() == LevelElement.GITTER
                                      || tile.levelElement() == LevelElement.GLASSWALL
                                      || tile.levelElement() == LevelElement.PORTAL) {
                                    interactedPositioncomponent.position(
                                        interactorPositioncomponent.position());
                                  }
                                });
                        attached[0] = false;
                      }
                    },
                    2f)));

    return sphere;
  }

  /**
   * Creates a pressure plate entity that can be configured to only respond to a specific type of
   * entity (e.g., cubes or spheres). This shared helper method handles the setup of components,
   * animations, and collision behavior.
   *
   * <p>The resulting pressure plate acts as a switch that becomes active (i.e., {@code true}) if
   * the total mass of entities standing on it—filtered by the {@code requiredComponent}—reaches or
   * exceeds the specified mass trigger threshold.
   *
   * <p>Entities with a {@code ProjectileComponent} are ignored and do not affect the plate’s state.
   *
   * @param name the internal name of the pressure plate entity
   * @param spriteSheet the sprite sheet used for the plate’s animation states
   * @param massTrigger the total mass required to activate the pressure plate
   * @param requiredComponent the component type that entities must have to interact with this plate
   *     (e.g., {@code PortalCubeComponent.class} or {@code PortalSphereComponent.class})
   * @param position the world position where the pressure plate should be created
   * @return a fully constructed {@link Entity} representing the configured pressure plate
   */
  private static Entity createPressurePlate(
      String name,
      IPath spriteSheet,
      float massTrigger,
      Class<? extends Component> requiredComponent,
      Point position) {

    Entity pressurePlate = new Entity(name);
    pressurePlate.add(new PositionComponent(position));

    Map<String, Animation> map = Animation.loadAnimationSpritesheet(spriteSheet);
    State stOff = State.fromMap(map, "off");
    State stOn = State.fromMap(map, "on");
    StateMachine sm = new StateMachine(Arrays.asList(stOff, stOn));
    sm.addTransition(stOff, "on", stOn);
    sm.addTransition(stOn, "off", stOff);
    pressurePlate.add(new DrawComponent(sm, DepthLayer.Ground));

    LeverComponent leverComponent = new LeverComponent(false, ICommand.NOOP);
    pressurePlate.add(leverComponent);

    PressurePlateComponent pressurePlateComponent = new PressurePlateComponent(massTrigger);
    pressurePlate.add(pressurePlateComponent);

    TriConsumer<Entity, Entity, Direction> onCollideEnter =
        (self, other, dir) -> {
          if (other.isPresent(ProjectileComponent.class)) return;

          other
              .fetch(VelocityComponent.class)
              .filter(vc -> other.isPresent(requiredComponent))
              .ifPresent(vc -> pressurePlateComponent.increase(vc.mass()));
        };

    TriConsumer<Entity, Entity, Direction> onCollideLeave =
        (self, other, dir) -> {
          if (other.isPresent(ProjectileComponent.class)) return;

          other
              .fetch(VelocityComponent.class)
              .filter(vc -> other.isPresent(requiredComponent))
              .ifPresent(vc -> pressurePlateComponent.decrease(vc.mass()));
        };

    pressurePlate.add(new CollideComponent(onCollideEnter, onCollideLeave).isSolid(false));

    return pressurePlate;
  }

  /**
   * Creates a cube-activated pressure plate entity at the specified position.
   *
   * <p>This plate becomes active (i.e., {@code true}) when the total mass of entities with a {@link
   * PortalCubeComponent} standing on it reaches or exceeds the given mass trigger threshold.
   *
   * <p>Entities that have a {@code ProjectileComponent} are excluded and do not trigger the plate.
   *
   * <p>The cube pressure plate does not emit events on interaction; it solely toggles the {@link
   * LeverComponent#isOn()} state based on the total mass currently on the plate.
   *
   * @param position the world position where the pressure plate should be created
   * @param massTrigger the total mass threshold that activates the pressure plate
   * @return the newly created cube pressure plate entity
   */
  public static Entity cubePressurePlate(Point position, float massTrigger) {
    return createPressurePlate(
        "cube-pressureplate",
        CUBE_PRESSURE_PLATE,
        massTrigger,
        PortalCubeComponent.class,
        position);
  }

  /**
   * Creates a sphere-activated pressure plate entity at the specified position.
   *
   * <p>This plate becomes active (i.e., {@code true}) when the total mass of entities with a {@link
   * PortalSphereComponent} standing on it reaches or exceeds the given mass trigger threshold.
   *
   * <p>Entities that have a {@code ProjectileComponent} are excluded and do not trigger the plate.
   *
   * <p>The sphere pressure plate does not emit events on interaction; it solely toggles the {@link
   * LeverComponent#isOn()} state based on the total mass currently on the plate.
   *
   * @param position the world position where the pressure plate should be created
   * @param massTrigger the total mass threshold that activates the pressure plate
   * @return the newly created sphere pressure plate entity
   */
  public static Entity spherePressurePlate(Point position, float massTrigger) {
    return createPressurePlate(
        "sphere-pressureplate",
        SPHERE_PRESSURE_PLATE,
        massTrigger,
        PortalSphereComponent.class,
        position);
  }

  /**
   * Creates a new entity that can shoot energy pellets.
   *
   * @param position the position of the pellet launcher
   * @param direction the direction the pellet launcher is facing.
   * @param attackRange Maximum travel range of the energy pellet.
   * @param projectileLifetime Time in ms before the projectile is removed.
   * @return a new energyPelletLauncher entity.
   */
  public static Entity energyPelletLauncher(
      Point position, Direction direction, float attackRange, long projectileLifetime) {
    launcherNumber++;
    String uniqueName = "energyPelletLauncher_" + launcherNumber;
    Entity launcher = new Entity(uniqueName);
    launcher.add(new PositionComponent(position));
    DrawComponent dc = chooseTexture(direction, PELLET_LAUNCHER);
    launcher.add(dc);
    launcher.add(new CollideComponent());
    String uniqueSkillName = uniqueName + "_skill";
    Skill energyPelletSkill =
        new EnergyPelletSkill(
            uniqueSkillName,
            SkillTools::playerPositionAsPoint,
            EnergyPelletSkill.COOLDOWN,
            attackRange,
            projectileLifetime);
    launcher.add(
        new AIComponent(
            entity -> {},
            new PelletLauncherBehaviour(
                uniqueSkillName, position, attackRange, direction, energyPelletSkill),
            entity -> false));
    launcher.add(new SkillComponent(energyPelletSkill));

    return launcher;
  }

  /**
   * Creates a new entity that can catch energy pellets.
   *
   * @param position the position of the pellet catcher.
   * @param catchDirection the direction the pellet catcher is facing.
   * @return a new energyPelletCatcher entity.
   */
  public static Entity energyPelletCatcher(Point position, Direction catchDirection) {
    Entity catcher = new Entity("energyPelletCatcher");
    catcher.add(new PositionComponent(position));
    DrawComponent dc = chooseTexture(catchDirection, PELLET_CATCHER);
    catcher.add(dc);
    catcher.add(new ToggleableComponent(false));

    TriConsumer<Entity, Entity, Direction> action =
        (self, other, direction) -> {
          if (other.name().matches("energyPelletLauncher_\\d+_skill_projectile")) {
            self.fetch(ToggleableComponent.class).ifPresent(ToggleableComponent::toggle);
            Game.remove(other);
          }
        };

    CollideComponent colComp = new CollideComponent(action, CollideComponent.DEFAULT_COLLIDER);
    catcher.add(colComp);

    return catcher;
  }

  /**
   * This method help to choose the correct single texture from an animationMap.
   *
   * @param direction the direction the entity is facing.
   * @param path the path of the texture.
   * @return a new DrawComponent including the correct StateMachine for the texture.
   */
  private static DrawComponent chooseTexture(Direction direction, SimpleIPath path) {
    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(path);
    StateMachine sm =
        switch (direction) {
          case DOWN -> {
            State top = State.fromMap(animationMap, "top");
            yield new StateMachine(List.of(top));
          }
          case LEFT -> {
            State right = State.fromMap(animationMap, "right");
            yield new StateMachine(List.of(right));
          }
          case RIGHT -> {
            State left = State.fromMap(animationMap, "left");
            yield new StateMachine(List.of(left));
          }
          default -> {
            State bottom = State.fromMap(animationMap, "bottom");
            yield new StateMachine(List.of(bottom));
          }
        };

    return new DrawComponent(sm);
  }
}
