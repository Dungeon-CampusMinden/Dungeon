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
import feature.entities.MiscFactory;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.inventory.items.ItemKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;
import rooms.systemRecovery.modules.display.DoorLabelComponent;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.riddles.BubbleSortRiddle;
import rooms.systemRecovery.riddles.EnergyRiddle;
import rooms.systemRecovery.riddles.InventoryScannerRiddle;
import rooms.systemRecovery.riddles.ManualSortingRiddle;
import rooms.systemRecovery.riddles.ModuleStorageRiddle;
import rooms.systemRecovery.riddles.TransportStorageRiddle;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/**
 * Builds System Recovery in room order and owns one controller per riddle.
 *
 * <p>Terminal callbacks and snapshot exporters use the static forwarding methods below; mutable
 * puzzle state belongs to this level instance, never to static fields.
 */
public class SystemRecoveryLevel extends DungeonLevel {
  private static final String LEVEL_NAME = "system-recovery-1";
  private static final String TERMINAL_POINT = "terminal";
  private final EnergyRiddle energy = new EnergyRiddle(this);
  private final ModuleStorageRiddle moduleStorage = new ModuleStorageRiddle(this);
  private final InventoryScannerRiddle inventoryScanner = new InventoryScannerRiddle(this);
  private final TransportStorageRiddle transportStorage = new TransportStorageRiddle(this);
  private final BubbleSortRiddle bubbleSort = new BubbleSortRiddle(this, transportStorage);
  private final ManualSortingRiddle manualSorting = new ManualSortingRiddle(this, bubbleSort);
  private final List<Entity> doorLabels = new ArrayList<>();
  private final SystemRecoveryStoryDialogs storyDialogs = new SystemRecoveryStoryDialogs();

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
    SystemRecoveryQuestLogUtil.initializeQuestLog();
    setupTerminal();
    setupRoomLabel();
    closeDoors();
    setupArchiveDoorLock();
    energy.setup();
    moduleStorage.setup();
    inventoryScanner.setup();
    transportStorage.setup();
    manualSorting.setup();
    bubbleSort.setup();
  }

  @Override
  protected void onTick() {
    manualSorting.tick();
    storyDialogs.tick();
    if (!Game.isHeadless()) {
      doorLabels.forEach(
          label ->
              label
                  .fetch(DoorLabelComponent.class)
                  .ifPresent(
                      status ->
                          DoorLabelComponent.updateAppearance(
                              label, status.completed().getAsBoolean())));
    }
  }

  private static SystemRecoveryLevel active() {
    return (SystemRecoveryLevel) Game.currentLevel().orElseThrow();
  }

  /** Materializes the energy array for riddle 1, terminal step 1. */
  public static void spawnEnergyCrates() {
    active().energy.spawnEnergyCrates();
  }

  /** Runs terminal input with the submitting player attached to story callbacks. */
  public static boolean interpretTerminalInput(String source, int playerId) {
    return SystemRecoveryStoryDialogs.withTerminalPlayer(
        playerId, () -> TerminalInterpreter.instance().interpret(source));
  }

  /** Advances one terminal state in debug mode with the submitting player attached to the story. */
  public static boolean advanceTerminalStateForDebug(int playerId) {
    return SystemRecoveryStoryDialogs.withTerminalPlayer(
        playerId, () -> TerminalInterpreter.instance().advanceCurrentStateForDebug());
  }

  /** Announces one story instruction to the player who completed a terminal step. */
  public static void announceStoryForCurrentTerminalPlayer(
      SystemRecoveryStoryDialogs.StoryStep step) {
    int playerId = SystemRecoveryStoryDialogs.currentTerminalPlayer().orElse(-1);
    currentLevel()
        .ifPresent(
            level -> {
              if (playerId >= 0) level.storyDialogs.announceForPlayer(step, playerId);
              else level.storyDialogs.announceToAllPlayers(step);
            });
  }

  /** Announces one story instruction after a shared physical room sequence. */
  public static void announceStoryToAllPlayers(SystemRecoveryStoryDialogs.StoryStep step) {
    currentLevel().ifPresent(level -> level.storyDialogs.announceToAllPlayers(step));
  }

  /** Announces one story instruction to one player after a world interaction. */
  public static void announceStoryForPlayer(
      SystemRecoveryStoryDialogs.StoryStep step, int playerId) {
    currentLevel().ifPresent(level -> level.storyDialogs.announceForPlayer(step, playerId));
  }

  /** Announces the final system message after the last terminal riddle. */
  public static void announceStoryCompletion() {
    currentLevel().ifPresent(level -> level.storyDialogs.announceCompletionToAllPlayers());
  }

  private static java.util.Optional<SystemRecoveryLevel> currentLevel() {
    return Game.currentLevel()
        .filter(SystemRecoveryLevel.class::isInstance)
        .map(SystemRecoveryLevel.class::cast);
  }

  private void closeDoors() {
    Game.allTiles(LevelElement.DOOR)
        .forEach(
            tile -> {
              ((DoorTile) tile).close();
            });
  }

  /** Locks the archive door with the shared key-and-lock interaction. */
  private void setupArchiveDoorLock() {
    DoorTile archiveDoor = (DoorTile) tileAt(getPoint("door_datenarchiv")).orElseThrow();
    Game.add(MiscFactory.createDoorBlocker(archiveDoor, ItemKey.class));
  }

  private void setupRoomLabel() {
    addDoorLabel("label_modulspeicher", "Modulspeicher", "Raum: R2", moduleStorage::completed);
    addDoorLabel(
        "label_inventarscanner", "Inventarscanner", "Raum: R3", inventoryScanner::completed);
    addDoorLabel("label_transportlager", "Transportlager", "Raum: R4", transportStorage::completed);
    addDoorLabel("label_datenspeicher", "Datenspeicher", "Raum: R5", manualSorting::completed);
    addDoorLabel("label_sortmachine", "Die Bubble-Sort-Maschine", "Raum R6", bubbleSort::completed);
    addDoorLabel("label_archive", "Datenarchiv", "Raum: R7", () -> terminalReached(9));
    addDoorLabel(
        "label_speicher", "Zweidimensionale Speicher", "Raum R:8", () -> terminalReached(12));
    addDoorLabel("label_suchroboter", "Batterielager", "Raum R4.b", () -> terminalReached(13));
    addDoorLabel(
        "label_systemcore",
        "Zentrales Rechenzentrum",
        "Raum: Systemcore",
        () -> terminalReached(16));
  }

  /** Associates each destination label with its prerequisite, independent of its door lock. */
  private void addDoorLabel(String point, String text, String title, BooleanSupplier completed) {
    Entity label = EntityFactory.roomLabel(getPoint(point), text, title);
    label.name(point);
    label.add(new DoorLabelComponent(completed));
    DoorLabelComponent.updateAppearance(label, completed.getAsBoolean());
    doorLabels.add(label);
    Game.add(label);
  }

  private boolean terminalReached(int completedSteps) {
    return TerminalInterpreter.instance().currentState() >= completedSteps;
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

  /** Activates all module sockets after the module array is initialized. */
  public static void activateModuleSockets() {
    active().moduleStorage.activateModuleSockets();
  }

  /** Spawns the five module chips on their corresponding sockets. */
  public static void spawnModuleChips() {
    active().moduleStorage.spawnModuleChips();
  }

  /** Removes the defective GPU chip from the third socket. */
  public static void removeGpuChip() {
    active().moduleStorage.removeGpuChip();
  }

  /** Moves the module chips to the inventory scanner without moving their sockets. */
  public static void portModuleChipsToScanner() {
    active().moduleStorage.portModuleChipsToScanner();
  }

  /** Updates the room display with the module array length. */
  public static void showModuleArrayLength() {
    active().moduleStorage.showModuleArrayLength();
  }

  /** Enables the scanner lever after the inventory scanner code has been solved. */
  public static void completeScannerPuzzle() {
    active().inventoryScanner.completeScannerPuzzle();
  }

  /** Spawns the packages for transport riddle four exactly once. */
  public static void spawnTransportPackages() {
    active().transportStorage.spawnTransportPackages();
  }

  /** Starts the authoritative conveyor animation for transport riddle four. */
  public static void startTransportSequence() {
    active().transportStorage.startTransportSequence();
  }

  /**
   * Returns the server-authoritative entity IDs of the pair currently compared by the sorter.
   *
   * <p>The network snapshot translator uses these IDs to reproduce the cyan comparison highlight on
   * every connected client. The sorter state itself is intentionally not accepted from clients.
   *
   * @return two entity IDs, or {@code {-1, -1}} when no comparison is active
   */
  public static int[] currentSortComparisonEntityIds() {
    return active().manualSorting.currentSortComparisonEntityIds();
  }

  /**
   * Returns the IDs of the two packages and the scanner currently active on the conveyor.
   *
   * @return left package, right package and scanner IDs; {@code -1} when idle
   */
  public static int[] currentBeltSortEntityIds() {
    return active().bubbleSort.currentBeltSortEntityIds();
  }

  /** Returns all active conveyor package IDs paired with their authoritative weights. */
  public static String currentBeltPackageMetadata() {
    return active().bubbleSort.currentBeltPackageMetadata();
  }

  /** Enables the array lever after the energy puzzle has been solved. */
  public static void completeEnergyPuzzle() {
    active().energy.completeEnergyPuzzle();
  }
}
