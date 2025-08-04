package contrib.entities;

import contrib.components.CollideComponent;
import contrib.components.InteractionComponent;
import contrib.components.LeverComponent;
import contrib.components.ProjectileComponent;
import contrib.systems.EventScheduler;
import contrib.utils.ICommand;
import contrib.utils.IEntityCommand;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.List;
import java.util.Map;

/** The LeverFactory class is responsible for creating lever entities. */
public class LeverFactory {

  private static final float DEFAULT_INTERACTION_RADIUS = 2.5f;

  /**
   * Creates a lever entity at a given position, with a specified behavior when interacted with. The
   * lever is initially off. The lever is interactable and can be toggled on and off.
   *
   * <p>The given `onInteract` command can be a simple {@link ICommand} or if needed an {@link
   * IEntityCommand} that takes the lever entity as context.
   *
   * @param pos The position where the lever will be created.
   * @param onInteract The behavior when the lever is interacted with.
   * @param design defines the textures for the lever
   * @return The created lever entity.
   * @see LeverComponent LeverComponent
   * @see contrib.systems.LeverSystem LeverSystem
   */
  public static Entity createLever(Point pos, ICommand onInteract, Design design) {
    Entity lever = new Entity("lever");
    lever.add(new PositionComponent(pos));
    DrawComponent dc = new DrawComponent(Animation.fromCollection(design.texturesOff));
    Map<String, Animation> animationMap =
        Map.of("off", dc.currentAnimation(), "on", Animation.fromCollection(design.texturesOn));
    dc.animationMap(animationMap);
    dc.currentAnimation("off");
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
    return createLever(pos, onInteract, Design.LEAVER);
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
    return createLever(pos, onInteract, Design.TORCH);
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

  public static Entity pressurePlate(Point position) {
    Entity pressurePlate = new Entity("pressureplate");
    pressurePlate.add(new PositionComponent(position.toCenteredPoint()));
    DrawComponent dc = new DrawComponent(Animation.fromCollection(Design.PLATE.texturesOff));
    Map<String, Animation> animationMap =
        Map.of(
            "off", dc.currentAnimation(), "on", Animation.fromCollection(Design.PLATE.texturesOn));
    dc.animationMap(animationMap);
    dc.currentAnimation("off");
    pressurePlate.add(dc);
    LeverComponent lc = new LeverComponent(false, ICommand.NOOP);
    pressurePlate.add(lc);
    TriConsumer<Entity, Entity, Direction> collideEnter =
        (entity, entity2, direction) -> {
          // dont trigger for projectiles
          if (entity2.isPresent(ProjectileComponent.class)) return;
          if (!lc.isOn()) lc.toggle();
        };
    TriConsumer<Entity, Entity, Direction> collideLeave =
        (entity, entity2, direction) -> {
          // dont trigger for projectiles
          if (entity2.isPresent(ProjectileComponent.class)) return;
          // TODO for MP check if there is not other player to collide with
          if (lc.isOn()) lc.toggle();
        };
    pressurePlate.add(new CollideComponent(collideEnter, collideLeave));
    return pressurePlate;
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

  /**
   * Represents different design types with associated texture paths. Each design has a list of
   * textures for both "on" and "off" states.
   */
  public enum Design {

    /** Represents a lever with one texture for "on" and one for "off". */
    LEAVER(
        List.of(new SimpleIPath("objects/lever/on/lever_0.png")),
        List.of(new SimpleIPath("objects/lever/off/lever_0.png"))),

    /** Represents a torch with multiple textures for "on" (animated) and one for "off". */
    TORCH(
        List.of(
            new SimpleIPath("objects/torch/on/torch_1.png"),
            new SimpleIPath("objects/torch/on/torch_2.png"),
            new SimpleIPath("objects/torch/on/torch_3.png"),
            new SimpleIPath("objects/torch/on/torch_4.png"),
            new SimpleIPath("objects/torch/on/torch_5.png"),
            new SimpleIPath("objects/torch/on/torch_6.png"),
            new SimpleIPath("objects/torch/on/torch_7.png"),
            new SimpleIPath("objects/torch/on/torch_8.png")),
        List.of(new SimpleIPath("objects/torch/off/torch_0.png"))),

    /** Represents a pressure plate with multiple textures for "on" (animated) and one for "off". */
    PLATE(
        List.of(new SimpleIPath("objects/pressureplate/on/pressureplate_0.png")),
        List.of(new SimpleIPath("objects/pressureplate/off/pressureplate_0.png")));

    private final List<IPath> texturesOn;
    private final List<IPath> texturesOff;

    /**
     * Constructs a new {@code Design} with specified "on" and "off" textures.
     *
     * @param texturesOn List of textures representing the "on" state.
     * @param texturesOff List of textures representing the "off" state.
     */
    Design(final List<IPath> texturesOn, final List<IPath> texturesOff) {
      this.texturesOn = texturesOn;
      this.texturesOff = texturesOff;
    }
  }
}
