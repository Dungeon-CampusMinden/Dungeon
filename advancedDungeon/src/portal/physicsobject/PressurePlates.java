package portal.physicsobject;

import contrib.components.*;
import contrib.utils.ICommand;
import core.Component;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;

/**
 * Utility class for creating configurable pressure plate entities.
 *
 * <p>Pressure plates act as switches that can be activated by entities standing on them. They may
 * be configured to respond only to specific entity types (such as cubes or spheres) and activate
 * once a defined mass threshold is reached.
 *
 * <p>Projectile entities are ignored and do not affect the activation state.
 */
public class PressurePlates {
  private static final SimpleIPath CUBE_PRESSURE_PLATE = new SimpleIPath("objects/pressureplate");
  private static final SimpleIPath SPHERE_PRESSURE_PLATE =
      new SimpleIPath("portal/kubus_pressureplate");

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
   * @param command Command to execute on trigger and release
   * @return a fully constructed {@link Entity} representing the configured pressure plate
   */
  private static Entity createPressurePlate(
      String name,
      IPath spriteSheet,
      float massTrigger,
      Class<? extends Component> requiredComponent,
      Point position,
      ICommand command) {

    Entity pressurePlate = new Entity(name);
    pressurePlate.add(new PositionComponent(position));

    Map<String, Animation> map = Animation.loadAnimationSpritesheet(spriteSheet);
    State stOff = State.fromMap(map, "off");
    State stOn = State.fromMap(map, "on");
    StateMachine sm = new StateMachine(Arrays.asList(stOff, stOn));
    sm.addTransition(stOff, "on", stOn);
    sm.addTransition(stOn, "off", stOff);
    pressurePlate.add(new DrawComponent(sm, DepthLayer.Ground));

    LeverComponent leverComponent = new LeverComponent(false, command);
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
        position,
        ICommand.NOOP);
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
   * @param command Command to execute on trigger and release
   * @return the newly created cube pressure plate entity
   */
  public static Entity cubePressurePlate(Point position, float massTrigger, ICommand command) {
    return createPressurePlate(
        "cube-pressureplate",
        CUBE_PRESSURE_PLATE,
        massTrigger,
        PortalCubeComponent.class,
        position,
        command);
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
        position,
        ICommand.NOOP);
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
   * @param command Command to execute on trigger and release
   * @return the newly created sphere pressure plate entity
   */
  public static Entity spherePressurePlate(Point position, float massTrigger, ICommand command) {
    return createPressurePlate(
        "sphere-pressureplate",
        SPHERE_PRESSURE_PLATE,
        massTrigger,
        PortalSphereComponent.class,
        position,
        command);
  }
}
