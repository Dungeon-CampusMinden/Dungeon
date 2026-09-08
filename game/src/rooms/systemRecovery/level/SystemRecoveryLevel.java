package rooms.systemRecovery.level;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import feature.components.DecoComponent;
import feature.entities.LeverFactory;
import feature.entities.WorldItemBuilder;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.interaction.keypad.KeypadFactory;
import feature.utils.ICommand;
import java.util.List;
import java.util.Map;
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.items.BatteryItem;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/** Minimal server-side level for System Recovery. */
public class SystemRecoveryLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "system-recovery-1";
  private static final String TERMINAL_POINT = "terminal";
  private static boolean energyPuzzleSolved = false;
  private static boolean batterySpawned = false;
  private static final Entity[] MODULE_SOCKETS = new Entity[5];
  private static final Entity[] MODULE_CHIPS = new Entity[5];
  private static final Point[] MODULE_SOCKET_POINTS = new Point[5];
  private static String moduleDisplayText = "Array length: Noch nicht bestimmt";

  /**
   * Creates the System Recovery level.
   *
   * @param layout the tile layout loaded from the level asset
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level asset
   * @param decorations static decorations loaded from the level asset
   */
  public SystemRecoveryLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }

  /**
   * Creates the System Recovery level.
   *
   * @param layout the tile layout loaded from the level asset
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level asset
   */
  public SystemRecoveryLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, LEVEL_NAME);
  }

  @Override
  protected void onFirstTick() {
    setupTerminal();
    setupRoomLabel();
    closeDoors();
    setupModuleStorageRoom();
    setupRoomThreeKeypad();

    Entity arrayLever =
        LeverFactory.createLever(
            getPoint("array_lever"),
            new ICommand() {
              @Override
              public void execute() {
                if (!energyPuzzleSolved || batterySpawned) {
                  return;
                }

                batterySpawned = true;

                Game.add(
                    WorldItemBuilder.buildWorldItem(
                        new BatteryItem(), getPoint("array_item_spawn")));
              }

              @Override
              public void undo() {}
            });
    Game.add(arrayLever);
    Game.add(
        EntityFactory.batteryBox(
            getPoint("batteriebox_modul"),
            new Runnable() {
              @Override
              public void run() {
                ((DoorTile) (Game.tileAt(getPoint("door_modulspeicher")).get())).open();
              }
            }));
  }

  private void closeDoors() {
    Game.allTiles(LevelElement.DOOR)
        .forEach(
            tile -> {
              ((DoorTile) tile).close();
            });
  }

  private void setupModuleStorageRoom() {
    for (int index = 0; index < 5; index++) {
      MODULE_SOCKET_POINTS[index] = getPoint("s" + index);
      MODULE_SOCKETS[index] = EntityFactory.moduleSocket(MODULE_SOCKET_POINTS[index]);
      Game.add(MODULE_SOCKETS[index]);
    }

    Game.add(EntityFactory.moduleDisplay(getPoint("display_room2"), () -> moduleDisplayText));
  }

  private void setupRoomThreeKeypad() {
    Game.add(
        KeypadFactory.createKeypad(
            getPoint("room3_keypad"),
            List.of(5),
            () -> ((DoorTile) Game.tileAt(getPoint("door_inventarscanner")).get()).open(),
            true));
  }

  /** Activates all module sockets after the module array is initialized. */
  public static void activateModuleSockets() {
    for (Entity socket : MODULE_SOCKETS) {
      EntityFactory.activateModuleSocket(socket);
    }
  }

  /** Spawns the five module chips on their corresponding sockets. */
  public static void spawnModuleChips() {
    String[] modules = {"CPU", "RAM", "GPU", "SSD", "NETWORK"};
    for (int index = 0; index < modules.length; index++) {
      MODULE_CHIPS[index] = EntityFactory.moduleChip(MODULE_SOCKET_POINTS[index], modules[index]);
      Game.add(MODULE_CHIPS[index]);
    }
  }

  /** Removes the defective GPU chip from the third socket. */
  public static void removeGpuChip() {
    if (MODULE_CHIPS[2] != null) {
      Game.remove(MODULE_CHIPS[2]);
      MODULE_CHIPS[2] = null;
    }
  }

  /** Updates the room display with the module array length. */
  public static void showModuleArrayLength() {
    moduleDisplayText = "Array length: 5";
  }

  private void setupRoomLabel() {
    Game.add(EntityFactory.roomLabel(getPoint("label_modulspeicher"), "Modulspeicher", "Raum: R2"));
    Game.add(
        EntityFactory.roomLabel(getPoint("label_inventarscanner"), "Inventarscanner", "Raum: R3"));
    Game.add(
        EntityFactory.roomLabel(getPoint("label_transportlager"), "Transportlager", "Raum: R4"));
    Game.add(EntityFactory.roomLabel(getPoint("label_datenspeicher"), "Datenspeicher", "Raum: R5"));
    Game.add(
        EntityFactory.roomLabel(
            getPoint("label_sortmachine"), "Die Bubble-Sort-Maschine", "Raum R6"));
    Game.add(EntityFactory.roomLabel(getPoint("label_archive"), "Datenarchiv", "Raum: R7"));
    Game.add(
        EntityFactory.roomLabel(
            getPoint("label_speicher"), "Zweidimensionale Speicher", "Raum R:8"));
    Game.add(EntityFactory.roomLabel(getPoint("label_suchroboter"), "Baterielager", "Raum R4.b"));
    Game.add(
        EntityFactory.roomLabel(
            getPoint("label_systemcore"), "Zentrale Rechenzentrum", "Raum: Systemcore"));
  }

  private void setupTerminal() {
    TerminalInterpreter.instance().reset();
    TerminalInterpreterSetup.setupRoomStates();
    Entity terminal = DecoFactory.createDeco(getPoint(TERMINAL_POINT), Deco.DeskWithPC1);
    terminal.name(TERMINAL_POINT);
    terminal.remove(DecoComponent.class);
    SystemRecoveryComputerFactory.attachComputerDialog(terminal);
    Game.add(terminal);
  }

  /** Enables the array lever after the energy puzzle has been solved. */
  public static void completeEnergyPuzzle() {
    energyPuzzleSolved = true;
  }
}
