package contrib.entities;

import contrib.components.InteractionComponent;
import contrib.components.LeverComponent;
import contrib.systems.EventScheduler;
import contrib.utils.ICommand;
import contrib.utils.IEntityCommand;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
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
   * <p>This method initializes a standard lever using {@code createLever(pos)} and augments it with
   * a timed command. Once activated, the lever will automatically reset to the "off" state after
   * the specified time duration.
   *
   * <p>The timing behavior relies on an {@code EventScheduler} being present and correctly
   * integrated into the game loop to execute delayed actions.
   *
   * @param pos the position at which to place the lever
   * @param time the duration (in ticks or game-specific time units) after which the lever resets
   *     itself to "off"
   * @return the configured lever entity with a timed reset behavior
   */
  public static Entity createTimedLever(Point pos, int time) {
    Entity l = createLever(pos);
    l.fetch(LeverComponent.class).orElseThrow().command(leverTimer(l, time));
    return l;
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

  private static IEntityCommand leverTimer(Entity lever, int timeInMs) {
    final EventScheduler.ScheduledAction[] a1 = {null};
    return new IEntityCommand() {
      @Override
      public void execute(Entity entity) {
        if (a1[0] == null || !EventScheduler.isScheduled(a1[0])) {
          a1[0] =
              EventScheduler.scheduleAction(
                  () ->
                      entity
                          .fetch(LeverComponent.class)
                          .ifPresent(
                              lc -> {
                                if (lc.isOn()) lc.toggle();
                              }),
                  timeInMs);
        }
      }

      @Override
      public void undo(Entity entity) {
        EventScheduler.cancelAction(a1[0]);
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
        List.of(new SimpleIPath("objects/torch/off/torch_0.png")));

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
