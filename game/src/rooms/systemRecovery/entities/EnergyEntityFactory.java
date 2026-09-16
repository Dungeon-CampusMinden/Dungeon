package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.state.State;
import engine.utils.components.draw.state.StateMachine;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.components.InventoryComponent;
import feature.hud.DialogUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.Arrays;
import java.util.Map;
import rooms.systemRecovery.items.BatteryItem;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Builds the Cryo-Box and battery insertion entities used by riddle 1. */
public final class EnergyEntityFactory {

  private EnergyEntityFactory() {}

  /**
   * Creates a Cryo-Box with either its active or inactive texture.
   *
   * @param point position of the Cryo-Box
   * @param active whether the filled appearance should be used
   * @return configured Cryo-Box entity
   */
  public static Entity cryoBox(Point point, boolean active) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(
        new DrawComponent(
            new SimpleIPath(active ? "objects/tech/CryoBox.png" : "objects/tech/CryoBoxOFF.png")));
    return entity;
  }

  /**
   * Creates the wall-mounted battery box. Inserting a battery switches the box on, invokes the
   * callback and removes the box inventory so the battery cannot be extracted again.
   *
   * @param point position of the battery box
   * @param onBatteryInserted callback executed after a BatteryItem was inserted
   * @return configured battery box entity
   */
  public static Entity batteryBox(Point point, Runnable onBatteryInserted) {
    Entity entity = new Entity("battery_box");
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());

    State offState =
        new State(
            StateMachine.IDLE_STATE,
            new Animation(new SimpleIPath("objects/tech/system_battery_box_off.png")));
    State onState =
        new State(
            "battery_inserted",
            new Animation(new SimpleIPath("objects/tech/system_battery_box_on.png")));
    StateMachine stateMachine = new StateMachine(Arrays.asList(offState, onState));
    stateMachine.addTransition(offState, "battery_inserted", onState);
    entity.add(new DrawComponent(stateMachine));

    InventoryComponent inventory = new InventoryComponent(1);
    inventory.onItemAdded(
        item -> {
          if (!(item instanceof BatteryItem)) return;
          entity.fetch(DrawComponent.class).ifPresent(draw -> draw.sendSignal("battery_inserted"));
          if (onBatteryInserted != null) onBatteryInserted.run();
          entity.remove(InventoryComponent.class);
        });
    entity.add(inventory);
    entity.add(new InteractionComponent(new Interaction(EnergyEntityFactory::openDualInventory)));
    return entity;
  }

  private static void openDualInventory(Entity container, Entity player) {
    if (!container.isPresent(InventoryComponent.class)) {
      DialogUtils.showTextPopup(
          SystemRecoveryText.key("world.battery.locked"),
          SystemRecoveryText.key("world.battery.title"),
          player.id());
      return;
    }
    DialogContext context =
        new DialogContext(
            DialogType.DefaultTypes.DUAL_INVENTORY,
            true,
            Map.of(
                DialogContextKeys.ENTITY, player.id(),
                DialogContextKeys.SECONDARY_ENTITY, container.id()));
    context.owner(player.id());
    DialogFactory.show(context, player.id());
  }
}
