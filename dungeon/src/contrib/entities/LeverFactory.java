package contrib.entities;

import contrib.components.InteractionComponent;
import contrib.components.LeverComponent;
import contrib.utils.ICommand;
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
   * @param pos The position where the lever will be created.
   * @param onInteract The behavior when the lever is interacted with. (isOn, lever, who)
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
              entity
                  .fetch(DrawComponent.class)
                  .ifPresent(
                      drawComponent -> drawComponent.currentAnimation(lc.isOn() ? "on" : "off"));
            }));
    return lever;
  }

  /**
   * Creates a lever entity at a given position, with a specified behavior when interacted with. The
   * lever is initially off. The lever is interactable and can be toggled on and off.
   *
   * <p>The lever will use the default leaver design.
   *
   * @param pos The position where the lever will be created.
   * @param onInteract The behavior when the lever is interacted with. (isOn, lever, who)
   * @return The created lever entity.
   * @see LeverComponent LeverComponent
   * @see contrib.systems.LeverSystem LeverSystem
   */
  public static Entity createLever(Point pos, ICommand onInteract) {
    return createLever(pos, onInteract, Design.LEAVER);
  }

  /**
   * Creates a lever entity at a given position, with a specified behavior when interacted with. The
   * lever is initially off. The lever is interactable and can be toggled on and off.
   *
   * <p>The lever will use the torch design.
   *
   * @param pos The position where the lever will be created.
   * @param onInteract The behavior when the lever is interacted with. (isOn, lever, who)
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
    return createLever(pos, ICommand.EMPTY_COMMAND);
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
    return createTorch(pos, ICommand.EMPTY_COMMAND);
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
