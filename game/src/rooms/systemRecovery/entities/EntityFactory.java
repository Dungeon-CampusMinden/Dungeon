package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
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
import java.util.Map;
import java.util.function.Supplier;
import rooms.systemRecovery.items.BatteryItem;

/** Factory methods for System Recovery room entities. */
public class EntityFactory {

  /**
   * Creates a cryo box entity.
   *
   * @param point position of the cryo box
   * @param on whether the active texture should be used
   * @return configured cryo box entity
   */
  public static Entity cryoBox(Point point, boolean on) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    if (on) entity.add(new DrawComponent(new SimpleIPath("objects/tech/CryoBox.png")));
    else entity.add(new DrawComponent(new SimpleIPath("objects/tech/CryoBoxOFF.png")));

    return entity;
  }

  /**
   * Creates an interactable room label entity.
   *
   * @param point position of the label entity
   * @param text popup text
   * @param titel popup title
   * @return configured label entity
   */
  public static Entity roomLabel(Point point, String text, String titel) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(point));
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Computer_1.png")));
    entity.add(
        new InteractionComponent(
            new Interaction((_, _) -> DialogUtils.showTextPopup(text, titel))));
    return entity;
  }

  /**
   * Creates an inactive module socket.
   *
   * @param point socket position
   * @return module socket entity
   */
  public static Entity moduleSocket(Point point) {
    return moduleSocket(point, false);
  }

  /**
   * Creates a module socket with either its inactive or active appearance.
   *
   * @param point socket position
   * @param active whether the socket is already activated
   * @return module socket entity
   */
  public static Entity moduleSocket(Point point, boolean active) {
    Entity entity = new Entity("module_socket");
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(
        new DrawComponent(
            new SimpleIPath(
                active ? "objects/tech/Screen_info_1.png" : "objects/tech/Screen_info_2.png")));
    entity.add(
        new InteractionComponent(
            new Interaction(
                (socket, _) ->
                    DialogUtils.showTextPopup(
                        socket.name().equals("module_socket_active")
                            ? "Der Sockel ist aktiv, aber noch leer."
                            : "Dieser Sockel ist noch inaktiv.",
                        "Modulsockel"))));
    if (active) {
      entity.name("module_socket_active");
    }
    return entity;
  }

  /**
   * Activates an existing module socket.
   *
   * @param socket socket entity to activate
   */
  public static void activateModuleSocket(Entity socket) {
    socket.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_info_1.png")));
    socket.name("module_socket_active");
  }

  /**
   * Creates a visual module chip on a socket.
   *
   * @param point chip position
   * @param moduleName module name shown when interacting with the chip
   * @return module chip entity
   */
  public static Entity moduleChip(Point point, String moduleName) {
    Entity entity = new Entity("module_" + moduleName.toLowerCase());
    entity.add(new PositionComponent(point));
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_info_1.png")));
    entity.add(
        new InteractionComponent(
            new Interaction(
                (_, _) ->
                    DialogUtils.showTextPopup(
                        moduleName.equals("GPU")
                            ? "Die GPU ist kaputt und muss entfernt werden."
                            : "Der Sockel ist mit " + moduleName + " belegt.",
                        "Modulsockel"))));
    return entity;
  }

  /**
   * Creates an interactable display whose text is evaluated on interaction.
   *
   * @param point display position
   * @param textSupplier supplies the current display text
   * @return display entity
   */
  public static Entity moduleDisplay(Point point, Supplier<String> textSupplier) {
    Entity entity = new Entity("module_display");
    entity.add(new PositionComponent(point));
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_device.png")));
    entity.add(
        new InteractionComponent(
            new Interaction(
                (_, _) -> DialogUtils.showTextPopup(textSupplier.get(), "Modulspeicher-Display"))));
    return entity;
  }

  /**
   * Creates a battery box that accepts one {@link BatteryItem} through the dual-inventory UI.
   *
   * <p>When a battery is inserted, {@code onBatteryInserted} is executed and the box inventory is
   * removed so the inserted battery cannot be taken out again.
   *
   * @param point position of the battery box
   * @param onBatteryInserted callback executed after a battery was inserted
   * @return configured battery box entity
   */
  public static Entity batteryBox(Point point, Runnable onBatteryInserted) {
    Entity entity = new Entity("battery_box");
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Small_Device.png")));

    InventoryComponent inventory = new InventoryComponent(1);
    inventory.onItemAdded(
        item -> {
          if (!(item instanceof BatteryItem)) {
            return;
          }
          if (onBatteryInserted != null) {
            onBatteryInserted.run();
          }
          entity.remove(InventoryComponent.class);
        });
    entity.add(inventory);
    entity.add(new InteractionComponent(new Interaction(EntityFactory::openDualInventory)));
    return entity;
  }

  private static void openDualInventory(Entity container, Entity who) {
    if (!container.isPresent(InventoryComponent.class)) {
      DialogUtils.showTextPopup("Die Batteriebox ist bereits verriegelt.", "Batteriebox");
      return;
    }
    DialogContext context =
        new DialogContext(
            DialogType.DefaultTypes.DUAL_INVENTORY,
            true,
            Map.of(
                DialogContextKeys.ENTITY, who.id(),
                DialogContextKeys.SECONDARY_ENTITY, container.id()));
    context.owner(who.id());
    DialogFactory.show(context, who.id());
  }
}
