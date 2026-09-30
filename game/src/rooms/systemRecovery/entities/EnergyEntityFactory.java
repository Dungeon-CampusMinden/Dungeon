package rooms.systemRecovery.entities;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.state.State;
import engine.utils.components.draw.state.StateMachine;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.components.InventoryComponent;
import feature.components.UIComponent;
import feature.hud.DialogUtils;
import feature.hud.UIUtils;
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

/** Builds the energy crate and battery insertion entities used by riddle 1. */
public final class EnergyEntityFactory {

  /** Shared texture for System Recovery crates whose values are shown by a fill shader. */
  public static final String ENERGY_CRATE_TEXTURE = "objects/crate/system_recovery_energy.png";

  /** Transparent mask that lets the fill shader color only the crate's central energy gauge. */
  public static final String ENERGY_CRATE_FILL_TEXTURE =
      "objects/crate/system_recovery_energy_fill.png";

  private EnergyEntityFactory() {}

  /**
   * Creates an energy crate; its stored value is rendered by the fill shader.
   *
   * @param point position of the crate
   * @return configured energy crate
   */
  public static Entity energyCrate(Point point) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(new DrawComponent(new SimpleIPath(ENERGY_CRATE_TEXTURE)));
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
          closeBatteryBoxInventory(entity);
          markBatteryInserted(entity);
          if (onBatteryInserted != null) onBatteryInserted.run();
        });
    entity.add(inventory);
    entity.add(new InteractionComponent(new Interaction(EnergyEntityFactory::openDualInventory)));
    return entity;
  }

  /**
   * Projects the inserted-battery state once, both during play and checkpoint restoration.
   *
   * @param entity battery box receiving the inserted state
   */
  public static void markBatteryInserted(Entity entity) {
    if (entity == null || !entity.isPresent(InventoryComponent.class)) return;
    entity.fetch(DrawComponent.class).ifPresent(draw -> draw.sendSignal("battery_inserted"));
    entity.remove(InventoryComponent.class);
  }

  private static void closeBatteryBoxInventory(Entity batteryBox) {
    Game.allPlayers()
        .forEach(
            player ->
                player
                    .fetch(UIComponent.class)
                    .filter(
                        ui ->
                            ui.dialogContext().dialogType()
                                    == DialogType.DefaultTypes.DUAL_INVENTORY
                                && ui.dialogContext()
                                    .find(DialogContextKeys.SECONDARY_ENTITY, Integer.class)
                                    .filter(id -> id == batteryBox.id())
                                    .isPresent())
                    .ifPresent(ui -> UIUtils.closeDialog(ui, true)));
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
