package contrib.entities;

import contrib.components.InteractionComponent;
import contrib.components.LeverComponent;
import contrib.utils.ICommand;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

import java.util.Arrays;
import java.util.List;
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
                  .ifPresent(
                      drawComponent -> drawComponent.sendSignal(lc.isOn() ? "on" : "off"));
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
    return createLever(pos, onInteract, LEVER_PATH);
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
}
