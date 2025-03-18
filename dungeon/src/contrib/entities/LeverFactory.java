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
  private static final IPath LEVER_TEXTURE_ON = new SimpleIPath("objects/lever/on/lever_0.png");
  private static final IPath LEVER_TEXTURE_OFF = new SimpleIPath("objects/lever/off/lever_0.png");

  private static final IPath TORCH_TEXTURE_OFF = new SimpleIPath("objects/torch/off/torch_0.png");
  private static final List<IPath> TORCH_TEXTURE_ON =
      List.of(
          new SimpleIPath("objects/torch/on/torch_1.png"),
          new SimpleIPath("objects/torch/on/torch_2.png"),
          new SimpleIPath("objects/torch/on/torch_3.png"),
          new SimpleIPath("objects/torch/on/torch_4.png"),
          new SimpleIPath("objects/torch/on/torch_5.png"),
          new SimpleIPath("objects/torch/on/torch_6.png"),
          new SimpleIPath("objects/torch/on/torch_7.png"),
          new SimpleIPath("objects/torch/on/torch_8.png"));

  /**
   * Creates a lever entity at a given position, with a specified behavior when interacted with. The
   * lever is initially off. The lever is interactable and can be toggled on and off.
   *
   * @param pos The position where the lever will be created.
   * @param onInteract The behavior when the lever is interacted with. (isOn, lever, who)
   * @param torchDesign True if the lever should look like a torch, false if it should look like a
   *     lever
   * @return The created lever entity.
   * @see LeverComponent LeverComponent
   * @see contrib.systems.LeverSystem LeverSystem
   */
  public static Entity createLever(Point pos, ICommand onInteract, boolean torchDesign) {
    Entity lever = new Entity("lever");

    lever.add(new PositionComponent(pos));
    DrawComponent dc;
    Map<String, Animation> animationMap;
    if (torchDesign) {
      dc = new DrawComponent(Animation.fromSingleImage(TORCH_TEXTURE_OFF));
      animationMap =
          Map.of("off", dc.currentAnimation(), "on", Animation.fromCollection(TORCH_TEXTURE_ON));

    } else {
      dc = new DrawComponent(Animation.fromSingleImage(LEVER_TEXTURE_OFF));
      animationMap =
          Map.of("off", dc.currentAnimation(), "on", Animation.fromSingleImage(LEVER_TEXTURE_ON));
    }
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
   * @param pos The position where the lever will be created.
   * @param onInteract The behavior when the lever is interacted with. (isOn, lever, who)
   * @return The created lever entity.
   * @see LeverComponent LeverComponent
   * @see contrib.systems.LeverSystem LeverSystem
   */
  public static Entity createLever(Point pos, ICommand onInteract) {
    return createLever(pos, onInteract, false);
  }

  /**
   * Creates a lever entity at a given position with a default interaction behavior.
   *
   * <p>The lever is initially off. This method provides a default behavior where no action is
   * performed when the lever is interacted with (empty command).
   *
   * @param pos The position where the lever will be created.
   * @param torchDesign True if the lever should look like a torch, false if it should look like a
   *     lever
   * @return The created lever entity.
   * @see LeverComponent LeverComponent
   * @see contrib.systems.LeverSystem LeverSystem
   */
  public static Entity createLever(Point pos, boolean torchDesign) {
    return createLever(
        pos,
        new ICommand() {
          @Override
          public void execute() {}

          @Override
          public void undo() {}
        },
        torchDesign);
  }

  /**
   * Creates a lever entity at a given position with a default interaction behavior.
   *
   * <p>The lever is initially off. This method provides a default behavior where no action is
   * performed when the lever is interacted with (empty command).
   *
   * @param pos The position where the lever will be created.
   * @return The created lever entity.
   * @see LeverComponent LeverComponent
   * @see contrib.systems.LeverSystem LeverSystem
   */
  public static Entity createLever(Point pos) {
    return createLever(
        pos,
        new ICommand() {
          @Override
          public void execute() {}

          @Override
          public void undo() {}
        });
  }
}
