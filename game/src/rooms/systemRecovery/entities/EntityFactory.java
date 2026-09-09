package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.animation.AnimationConfig;
import engine.utils.components.draw.animation.SpritesheetConfig;
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
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import rooms.systemRecovery.items.BatteryItem;
import rooms.systemRecovery.modules.display.DisplayTextComponent;

/**
 * Stateless builders for System Recovery's reusable world objects.
 *
 * <p>Energy boxes and battery sockets belong to riddle 1; module sockets/chips to riddle 2;
 * scanners to riddles 3, 4 and 6; conveyor packages to riddles 4 and 6; comparison containers to
 * riddle 5. Displays are shared across rooms. Progression and object ownership live in the
 * corresponding {@code rooms.systemRecovery.riddles} controller, never in this factory.
 */
public final class EntityFactory {

  private EntityFactory() {}

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
                    DialogUtils.showTextPopup(moduleSocketStatus(socket), "Modulsockel"))));
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
    if (!socket.name().startsWith("module_socket_occupied_")) {
      socket.name("module_socket_active");
    }
  }

  /** Marks an active socket as occupied by the given module. */
  public static void occupyModuleSocket(Entity socket, String moduleName) {
    socket.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_info_1.png")));
    socket.name("module_socket_occupied_" + moduleName.toLowerCase());
  }

  /** Marks an occupied socket as active but empty again. */
  public static void clearModuleSocket(Entity socket) {
    socket.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_info_1.png")));
    socket.name("module_socket_active");
  }

  private static String moduleSocketStatus(Entity socket) {
    String name = socket.name();
    if (name.startsWith("module_socket_occupied_")) {
      String moduleName = name.substring("module_socket_occupied_".length());
      return "Der Sockel ist mit " + moduleName.toUpperCase() + " belegt.";
    }
    if (name.equals("module_socket_active")) {
      return "Der Sockel ist aktiv, aber noch leer.";
    }
    return "Dieser Sockel ist noch inaktiv.";
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

  /** Creates one visible data crystal for the manual sorting station. */
  public static Entity sortingDataCrystal(Point point, int value) {
    Entity entity = cryoBox(point, false);
    entity.name("sort_data_" + value);
    entity.add(
        new InteractionComponent(
            new Interaction(
                (_, _) ->
                    DialogUtils.showTextPopup("Sicherheitswert: " + value, "Datenspeicher"))));
    return entity;
  }

  /** Creates the interactable bubble-sort machine. */
  public static Entity bubbleSortMachine(Point point, BiConsumer<Entity, Entity> onInteract) {
    Entity entity = new Entity("bubble_sort_machine");
    entity.add(new PositionComponent(point));
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_device.png")));
    entity.add(new InteractionComponent(new Interaction(onInteract)));
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
    return moduleDisplay(
        point,
        textSupplier,
        (display, _) ->
            DialogUtils.showTextPopup(
                display.fetch(DisplayTextComponent.class).orElseThrow().text(),
                "Modulspeicher-Display"));
  }

  /**
   * Creates an interactable display with a custom, player-aware interaction.
   *
   * @param point display position
   * @param textSupplier supplies the current display text
   * @param onInteract callback receiving the display and the interacting player
   * @return display entity
   */
  public static Entity moduleDisplay(
      Point point, Supplier<String> textSupplier, BiConsumer<Entity, Entity> onInteract) {
    Entity entity = new Entity("module_display");
    entity.add(new PositionComponent(point));
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_device.png")));
    entity.add(new DisplayTextComponent(textSupplier.get()));
    entity.add(new InteractionComponent(new Interaction(onInteract)));
    return entity;
  }

  /**
   * Updates a display's synchronized text.
   *
   * @param display display entity
   * @param text new display text
   */
  public static void updateDisplayText(Entity display, String text) {
    display.fetch(DisplayTextComponent.class).ifPresent(component -> component.text(text));
  }

  /**
   * Creates the visual scanner that passes over module entities.
   *
   * @param point initial scanner position
   * @return scanner entity
   */
  public static Entity moduleScanner(Point point) {
    return moduleScanner(point, 1f);
  }

  /**
   * Creates the visual scanner with a configurable horizontal width.
   *
   * @param point initial scanner position
   * @param widthScale horizontal scale relative to the original scanner texture
   * @return scanner entity
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

  /** Creates the transport-lane scanner using the same visual device as the inventory scanner. */
  public static Entity transportScanner(Point point) {
    return transportScanner(point, 1f);
  }

  /**
   * Creates the transport-lane scanner with an optional wide scan head.
   *
   * @param point scanner position
   * @param widthScale horizontal scale of the scanner
   * @return transport scanner entity
   */
  public static Entity transportScanner(Point point, float widthScale) {
    Entity entity = moduleScanner(point, widthScale);
    entity.name("transport_scanner");
    entity.fetch(DrawComponent.class).ifPresent(draw -> draw.depth(DepthLayer.AbovePlayer.depth()));
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
          if (!(item instanceof BatteryItem)) {
            return;
          }
          entity.fetch(DrawComponent.class).ifPresent(draw -> draw.sendSignal("battery_inserted"));
          if (onBatteryInserted != null) {
            onBatteryInserted.run();
          }
          entity.remove(InventoryComponent.class);
        });
    entity.add(inventory);
    entity.add(new InteractionComponent(new Interaction(EntityFactory::openDualInventory)));
    return entity;
  }

  /** Creates one compact repeating conveyor tile. */
  public static Entity conveyorSegment(Point point) {
    Entity entity = new Entity("transport_conveyor");
    entity.add(new PositionComponent(point));
    entity.add(
        new DrawComponent(
            new Animation(
                new SimpleIPath("objects/tech/transport_conveyor.png"),
                new AnimationConfig(
                        // Use the arrow section of the generated strip as one repeatable sprite.
                        new SpritesheetConfig(650, 230, 1, 1, 300, 430))
                    .scaleX(1.2f)
                    .scaleY(1.0f))));
    entity.fetch(DrawComponent.class).ifPresent(draw -> draw.depth(DepthLayer.Ground.depth()));
    return entity;
  }

  /**
   * Creates a package that can be moved along the transport conveyor.
   *
   * @param point package position
   * @param weight package weight shown in the interaction text
   * @return package entity
   */
  public static Entity transportPackage(Point point, int weight) {
    Entity entity = new Entity("transport_package_" + weight);
    entity.add(new PositionComponent(point));
    DrawComponent draw =
        new DrawComponent(new Animation(new SimpleIPath("objects/crate/basic.png")));
    draw.depth(DepthLayer.ForegroundDeco.depth());
    draw.tintColor(transportPackageTint(weight));
    entity.add(draw);
    entity.add(
        new InteractionComponent(
            new Interaction(
                (_, _) ->
                    DialogUtils.showTextPopup("Transportpaket: " + weight, "Transportlager"))));
    return entity;
  }

  /**
   * Gives each transport weight a stable visual identity while keeping the crate texture visible.
   *
   * @param weight package weight
   * @return RGBA tint for the package
   */
  private static int transportPackageTint(int weight) {
    return switch (weight) {
      case 15 -> 0xFF6666FF;
      case 20 -> 0xFFD34DFF;
      case 30 -> 0x66CC66FF;
      case 40 -> 0x66AAFFFF;
      case 60 -> 0xCC66FFFF;
      default -> 0xFFFFFFFF;
    };
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
