package entities;

import components.LeverComponent;
import contrib.components.InteractionComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Map;
import utils.ICommand;

/** The LeverFactory class is responsible for creating lever entities. */
public class LeverFactory {

  private static final float DEFAULT_INTERACTION_RADIUS = 2.5f;
  private static final IPath LEVER_TEXTURE_ON = new SimpleIPath("objects/lever/on/lever_0.png");
  private static final IPath LEVER_TEXTURE_OFF = new SimpleIPath("objects/lever/off/lever_0.png");

  /**
   * Creates a lever entity at a given position, with a specified behavior when interacted with. The
   * lever is initially off. The lever is interactable and can be toggled on and off.
   *
   * @param pos The position where the lever will be created.
   * @param onInteract The behavior when the lever is interacted with. (isOn, lever, who)
   * @return The created lever entity.
   * @see components.LeverComponent LeverComponent
   * @see systems.LeverSystem LeverSystem
   */
  public static Entity createLever(Point pos, ICommand onInteract) {
    Entity lever = new Entity("lever");

    lever.add(new PositionComponent(pos));
    DrawComponent dc = new DrawComponent(Animation.fromSingleImage(LEVER_TEXTURE_OFF));
    Map<String, Animation> animationMap =
        Map.of("off", dc.currentAnimation(), "on", Animation.fromSingleImage(LEVER_TEXTURE_ON));
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
                      drawComponent -> {
                        drawComponent.currentAnimation(lc.isOn() ? "on" : "off");
                      });
            }));
    return lever;
  }
}
