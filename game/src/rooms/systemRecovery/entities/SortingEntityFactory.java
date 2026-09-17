package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.hud.DialogUtils;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.function.BiConsumer;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Builds the comparison crystals and sorting-machine entity used by riddle 6. */
public final class SortingEntityFactory {

  private SortingEntityFactory() {}

  /**
   * Creates one visible data crystal for the manual sorting station.
   *
   * @param point crystal position
   * @param value value shown when the crystal is examined
   * @return configured sorting crystal
   */
  public static Entity dataCrystal(Point point, int value) {
    Entity entity = EnergyEntityFactory.cryoBox(point, false);
    entity.name("sort_data_" + value);
    entity.add(
        new InteractionComponent(
            new Interaction(
                (_, who) ->
                    DialogUtils.showTextPopup(
                        SystemRecoveryText.key("world.sort.value", value),
                        SystemRecoveryText.key("world.sort.title"),
                        who.id()))));
    return entity;
  }

  /**
   * Creates the interactable machine that starts the conveyor sort.
   *
   * @param point machine position
   * @param onInteract callback receiving the machine and interacting player
   * @return configured sorting machine
   */
  public static Entity bubbleSortMachine(Point point, BiConsumer<Entity, Entity> onInteract) {
    Entity entity = new Entity("bubble_sort_machine");
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(new DrawComponent(new Animation(new SimpleIPath("objects/tech/Screen_device.png"))));
    entity.add(new InteractionComponent(new Interaction(onInteract)));
    return entity;
  }
}
