package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.animation.AnimationConfig;
import engine.utils.components.path.SimpleIPath;
import feature.hud.DialogUtils;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.function.BiConsumer;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Builds scanner devices and search-robot visuals used by riddles 3, 4 and 9. */
public final class ScannerEntityFactory {

  private ScannerEntityFactory() {}

  /**
   * Creates the scanner that passes over module chips.
   *
   * @param point scanner position
   * @param widthScale horizontal scale relative to the source texture
   * @return configured module scanner
   */
  public static Entity moduleScanner(Point point, float widthScale) {
    Entity entity = new Entity("module_scanner");
    entity.add(new PositionComponent(point));
    entity.add(
        new DrawComponent(
            new Animation(
                new SimpleIPath("objects/tech/module_scanner.png"),
                new AnimationConfig().scaleX(widthScale).scaleY(1f))));
    return entity;
  }

  /**
   * Creates the stationary search robot.
   *
   * @param point robot position
   * @return configured search robot
   */
  public static Entity searchRobot(Point point) {
    Entity entity = new Entity("search_robot");
    entity.add(new PositionComponent(point));
    DrawComponent draw =
        new DrawComponent(new Animation(new SimpleIPath("objects/tech/transport_robot.png")));
    draw.depth(DepthLayer.AbovePlayer.depth());
    entity.add(draw);
    return entity;
  }

  /**
   * Creates the controller that accepts the programmed search chip.
   *
   * @param point controller position
   * @param onInteract optional custom interaction callback
   * @return configured search-robot controller
   */
  public static Entity searchRobotController(Point point, BiConsumer<Entity, Entity> onInteract) {
    Entity entity = new Entity("search_robot_controller");
    entity.add(new PositionComponent(point));
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_device.png")));
    entity.add(
        new InteractionComponent(
            new Interaction(
                onInteract == null
                    ? (_, who) ->
                        DialogUtils.showTextPopup(
                            SystemRecoveryText.key("world.search.controller"),
                            SystemRecoveryText.key("world.search.title"),
                            who.id())
                    : onInteract)));
    return entity;
  }

  /**
   * Creates the non-interactive item visual collected by the search robot.
   *
   * @param point target-item position
   * @return configured search target
   */
  public static Entity searchTargetItem(Point point) {
    Entity entity = new Entity("search_target_item");
    entity.add(new PositionComponent(point));
    DrawComponent draw = new DrawComponent(new SimpleIPath("objects/tech/Hand_scanner.png"));
    draw.depth(DepthLayer.AbovePlayer.depth());
    entity.add(draw);
    return entity;
  }

  /**
   * Creates the visual grab arm that carries the locator chip to its destination.
   *
   * @param point initial grab-arm position
   * @return configured grab-arm entity
   */
  public static Entity chipGrabArm(Point point) {
    Entity entity = moduleScanner(point, 0.35f);
    entity.name("storage_chip_grab_arm");
    entity.fetch(DrawComponent.class).ifPresent(draw -> draw.depth(DepthLayer.AbovePlayer.depth()));
    return entity;
  }
}
