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
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import java.util.List;
import java.util.Map;
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/** Minimal server-side level for System Recovery. */
public class SystemRecoveryLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "system-recovery-1";
  private static final String TERMINAL_POINT = "terminal";

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
  }

  private void closeDoors() {
    Game.allTiles(LevelElement.DOOR)
        .forEach(
            tile -> {
              ((DoorTile) tile).close();
            });
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
}
