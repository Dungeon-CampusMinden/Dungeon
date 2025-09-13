package contrib.entities;

import contrib.components.*;
import contrib.systems.EventScheduler;
import contrib.utils.ICommand;
import contrib.utils.IEntityCommand;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Map;

/** The LeverFactory class is responsible for creating lever entities. */
public class LeverFactory {

  private static final float DEFAULT_INTERACTION_RADIUS = 2.5f;
  private static final IPath LEVER_PATH = new SimpleIPath("objects/lever");
  private static final IPath TORCH_PATH = new SimpleIPath("objects/torch");

  /**
   * Creates a lever entity at a given position, with a specified behavior when interacted with. The
   * lever is initially off. The lever is interactable and can be toggled on and off.
   *
   * <p>The given `onInteract` command can be a simple {@link ICommand} or if needed an {@link
   * IEntityCommand} that takes the lever entity as context.
   *
   * @param pos The position where the lever will be created.
   * @param onInteract The behavior when the lever is interacted with. (isOn, lever, who)
   * @param texturePath defines the texture(s) to use for the lever
   * @return The created lever entity.
   * @see LeverComponent LeverComponent
   * @see contrib.systems.LeverSystem LeverSystem
   */
  public static Entity createLever(Point pos, ICommand onInteract, IPath texturePath) {
    Entity lever = new Entity("lever");
    lever.add(new PositionComponent(pos));

    Map<String, Animation> map = Animation.loadAnimationSpritesheet(texturePath);
    State stOff = State.fromMap(map, "off");
    State stOn = State.fromMap(map, "on");
    StateMachine sm = new StateMachine(Arrays.asList(stOff, stOn));
    sm.addTransition(stOff, "on", stOn);
    sm.addTransition(stOn, "off", stOff);
    DrawComponent dc = new DrawComponent(sm);
    lever.add(dc);

    lever.add(new LeverComponent(false, onInteract));
    lever.add(
        new InteractionComponent(
            DEFAULT_INTERACTION_RADIUS,
            true,
            (entity, who) -> {
              LeverComponent lc =
                  entity
                      .fetch(LeverComponent.class)
                      .orElseThrow(
                          () -> MissingComponentException.build(entity, LeverComponent.class));
              lc.toggle();
              entity
                  .fetch(DrawComponent.class)
                  .ifPresent(drawComponent -> drawComponent.sendSignal(lc.isOn() ? "on" : "off"));
            }));
    return lever;
  }

  /**
   * Creates a lever entity at a given position, with a specified behavior when interacted with. The
   * lever is initially off. The lever is interactable and can be toggled on and off.
   *
   * <p>The given `onInteract` command can be a simple {@link ICommand} or if needed an {@link
   * IEntityCommand} that takes the lever entity as context.
   *
   * <p>The lever will use the default leaver design.
   *
   * @param pos The position where the lever will be created.
   * @param onInteract The behavior when the lever is interacted with.
   * @return The created lever entity.
   * @see LeverComponent LeverComponent
   * @see contrib.systems.LeverSystem LeverSystem
   */
  public static Entity createLever(Point pos, ICommand onInteract) {
    return createLever(pos, onInteract, LEVER_PATH);
  }

  /**
   * Creates a timed lever entity at the specified position.
   *
   * <p>Once activated, the lever will automatically reset to the "off" state after the specified
   * time duration.
   *
   * <p><strong>Note:</strong> Requires a properly functioning {@code EventScheduler} and {@link
   * core.systems.LevelSystem} to work.
   *
   * @param pos the position at which to place the lever
   * @param time the duration after which the lever resets itself to "off"
   * @return the configured lever entity with a timed reset behavior
   */
  public static Entity createTimedLever(Point pos, int time) {
    return createLever(pos, leverTimer(time));
  }

  /**
   * Creates a lever entity at a given position, with a specified behavior when interacted with. The
   * lever is initially off. The lever is interactable and can be toggled on and off.
   *
   * <p>The lever will use the torch design.
   *
   * @param pos The position where the lever will be created.
   * @param onInteract The behavior when the lever is interacted with.
   * @return The created lever entity.
   * @see LeverComponent LeverComponent
   * @see contrib.systems.LeverSystem LeverSystem
   */
  public static Entity createTorch(Point pos, ICommand onInteract) {
    return createLever(pos, onInteract, TORCH_PATH);
  }

  /**
   * Creates a lever entity at a given position, with a specified behavior when interacted with. The
   * lever is initially off. The lever is interactable and can be toggled on and off.
   *
   * <p>The level will trigger no event on interaction, it only toggles the {@link
   * LeverComponent#isOn()} value.
   *
   * <p>The lever will use the default design.
   *
   * @param pos The position where the lever will be created.
   * @return The created lever entity.
   * @see LeverComponent LeverComponent
   * @see contrib.systems.LeverSystem LeverSystem
   */
  public static Entity createLever(Point pos) {
    return createLever(pos, ICommand.NOOP);
  }

  /**
   * Creates a lever entity at a given position, with a specified behavior when interacted with. The
   * lever is initially off. The lever is interactable and can be toggled on and off.
   *
   * <p>The level will trigger no event on interaction, it only toggles the {@link
   * LeverComponent#isOn()} value.
   *
   * <p>The lever will use the def torch.
   *
   * @param pos The position where the lever will be created.
   * @return The created lever entity.
   * @see LeverComponent LeverComponent
   * @see contrib.systems.LeverSystem LeverSystem
   */
  public static Entity createTorch(Point pos) {
    return createTorch(pos, ICommand.NOOP);
  }

  /**
   * Creates a pressure plate entity at the specified position.
   *
   * <p>The pressure plate acts as a switch that becomes active (i.e., {@code true}) if the total
   * mass of entities with a {@code CollideComponent} standing on it reaches or exceeds the
   * specified mass trigger threshold.
   *
   * <p>Entities that have a {@code ProjectileComponent} are excluded and do not trigger the plate.
   *
   * <p>This pressure plate does not emit any events on interaction; it solely toggles the {@link
   * LeverComponent#isOn()} state based on the mass currently on the plate.
   *
   * @param position the position where the pressure plate entity should be created
   * @param massTrigger the mass threshold at which the pressure plate becomes triggered
   * @return the newly created pressure plate entity
   */
  public static Entity pressurePlate(Point position, float massTrigger) {
    Entity pressurePlate = new Entity("pressureplate");
    pressurePlate.add(new PositionComponent(position));

    Map<String, Animation> map =
        Animation.loadAnimationSpritesheet(new SimpleIPath("objects/pressureplate"));
    State stOff = State.fromMap(map, "off");
    State stOn = State.fromMap(map, "on");
    StateMachine sm = new StateMachine(Arrays.asList(stOff, stOn));
    sm.addTransition(stOff, "on", stOn);
    sm.addTransition(stOn, "off", stOff);
    DrawComponent dc = new DrawComponent(sm, DepthLayer.Ground);
    pressurePlate.add(dc);

    LeverComponent leverComponent = new LeverComponent(false, ICommand.NOOP);
    pressurePlate.add(leverComponent);
    PressurePlateComponent pressurePlateComponent = new PressurePlateComponent(massTrigger);
    pressurePlate.add(pressurePlateComponent);
    TriConsumer<Entity, Entity, Direction> onCollideEnter =
        (self, other, dir) -> {
          if (other.isPresent(ProjectileComponent.class)) return;

          other
              .fetch(VelocityComponent.class)
              .ifPresent(vc -> pressurePlateComponent.increase(vc.mass()));
        };

    TriConsumer<Entity, Entity, Direction> onCollideLeave =
        (self, other, dir) -> {
          if (other.isPresent(ProjectileComponent.class)) return;

          other
              .fetch(VelocityComponent.class)
              .ifPresent(vc -> pressurePlateComponent.decrease(vc.mass()));
        };

    pressurePlate.add(new CollideComponent(onCollideEnter, onCollideLeave).isSolid(false));
    return pressurePlate;
  }

  /**
   * Creates a pressure plate entity at the specified position.
   *
   * <p>The pressure plate acts as a switch that becomes active (i.e., {@code true}) if at least one
   * entity with a {@code CollideComponent} stands on it, triggering at the default mass threshold.
   *
   * <p>Entities with a {@code ProjectileComponent} do not trigger the pressure plate.
   *
   * <p>The plate does not emit events on interaction; it only toggles the {@link
   * LeverComponent#isOn()} state.
   *
   * <p>The plate triggers at the default mass threshold defined by {@link
   * PressurePlateComponent#DEFAULT_MASS_TRIGGER}.
   *
   * @param position the position where the pressure plate entity should be created
   * @return the newly created pressure plate entity
   */
  public static Entity pressurePlate(Point position) {
    return pressurePlate(position, PressurePlateComponent.DEFAULT_MASS_TRIGGER);
  }

  /**
   * Creates a command that automatically turns off a lever after a specified delay.
   *
   * <p>This command is intended to be attached to a {@link LeverComponent}. When the lever is
   * activated, it schedules an action via the {@link EventScheduler} to toggle the lever off after
   * the given time delay. If the command is executed again while a previous action is still
   * pending, no new action will be scheduled.
   *
   * <p>The {@code undo} method cancels the scheduled toggle action if it hasn't executed yet.
   *
   * <p><strong>Note:</strong> Requires a properly functioning {@code EventScheduler} and {@link
   * core.systems.LevelSystem} to work.
   *
   * @param timeInMs the time in milliseconds after which the lever should automatically toggle off
   * @return an {@link IEntityCommand} that schedules a timed lever reset
   */
  private static IEntityCommand leverTimer(int timeInMs) {
    return new IEntityCommand() {
      private EventScheduler.ScheduledAction scheduledAction;

      @Override
      public void execute(Entity lever) {
        // prevent recursive calling
        if (scheduledAction == null || !EventScheduler.isScheduled(scheduledAction)) {
          scheduledAction =
              EventScheduler.scheduleAction(
                  () ->
                      lever
                          .fetch(LeverComponent.class)
                          .filter(LeverComponent::isOn)
                          .ifPresent(LeverComponent::toggle),
                  timeInMs);
        }
      }

      @Override
      public void undo(Entity lever) {
        if (scheduledAction != null) {
          EventScheduler.cancelAction(scheduledAction);
          scheduledAction = null;
        }
      }
    };
  }
}
