package rooms.systemRecovery.level;

import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.sound.Sounds;
import engine.systems.DrawSystem;
import engine.utils.Point;
import engine.utils.Tuple;
import engine.utils.components.draw.DepthLayer;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.DecoComponent;
import feature.emote.Emote;
import feature.emote.EmoteFactory;
import feature.entities.MiscFactory;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.DialogUtils;
import feature.hud.dialogs.DialogFactory;
import feature.hints.HintSystem;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.inventory.items.ItemKey;
import feature.systems.LevelEditorSystem;
import feature.utils.EntityUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import rooms.lasthour.util.LastHourSounds;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.entities.EntityFactory;
import rooms.systemRecovery.modules.computer.SystemRecoveryComputerFactory;
import rooms.systemRecovery.modules.display.DoorLabelComponent;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.riddles.BubbleSortRiddle;
import rooms.systemRecovery.riddles.DataArchiveRiddle;
import rooms.systemRecovery.riddles.EnergyRiddle;
import rooms.systemRecovery.riddles.InventoryScannerRiddle;
import rooms.systemRecovery.riddles.ManualSortingRiddle;
import rooms.systemRecovery.riddles.ModuleStorageRiddle;
import rooms.systemRecovery.riddles.SearchRobotRiddle;
import rooms.systemRecovery.riddles.TransportStorageRiddle;
import rooms.systemRecovery.riddles.TwoDimensionalStorageRiddle;
import rooms.systemRecovery.story.SystemRecoveryDialogTriggers;
import rooms.systemRecovery.story.SystemRecoveryHintPhone;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.interpreter.InterpretationCallbacks;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;
import rooms.systemRecovery.util.shaders.SystemRecoveryAlarm;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEvents;

/**
 * Builds System Recovery in room order and owns one controller per riddle.
 *
 * <p>Terminal callbacks and snapshot exporters use the static forwarding methods below; mutable
 * puzzle state belongs to this level instance, never to static fields.
 */
public class SystemRecoveryLevel extends DungeonLevel {
  private static final String LEVEL_NAME = "system-recovery-1";
  private static final String TERMINAL_POINT = "terminal";
  private final EnergyRiddle energy =
      new EnergyRiddle(this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.ENERGY));
  private final ModuleStorageRiddle moduleStorage =
      new ModuleStorageRiddle(
          this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.MODULE_STORAGE));
  private final InventoryScannerRiddle inventoryScanner =
      new InventoryScannerRiddle(
          this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.INVENTORY_SCANNER));
  private final TransportStorageRiddle transportStorage =
      new TransportStorageRiddle(
          this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.TRANSPORT_STORAGE));
  private final BubbleSortRiddle bubbleSort =
      new BubbleSortRiddle(
          this,
          transportStorage,
          SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.BUBBLE_SORT));
  private final ManualSortingRiddle manualSorting =
      new ManualSortingRiddle(
          this,
          bubbleSort,
          SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.MANUAL_SORTING));
  private final DataArchiveRiddle dataArchive =
      new DataArchiveRiddle(
          this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.DATA_ARCHIVE));
  private final TwoDimensionalStorageRiddle twoDimensionalStorage =
      new TwoDimensionalStorageRiddle(
          this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.TWO_DIMENSIONAL_STORAGE));
  private final SearchRobotRiddle searchRobot =
      new SearchRobotRiddle(
          this, SystemRecoveryPuzzleEvents.forPuzzle(SystemRecoveryPuzzle.SEARCH_ROBOT));
  private final List<Entity> doorLabels = new ArrayList<>();
  private final SystemRecoveryStoryDialogs storyDialogs = new SystemRecoveryStoryDialogs();
  private final Set<Integer> introShownPlayers = new HashSet<>();
  private final Set<Integer> controlsShownPlayers = new HashSet<>();
  private final Set<String> triggeredDialogPoints = new HashSet<>();
  private Entity phone;
  private Entity ringingPhoneEmote;
  private boolean openingPhoneCallTriggered;
  private boolean phoneRinging;
  private boolean terminalsUnlocked;
  private boolean systemCoreAccessGranted;
  private boolean systemCoreRiddleCompleted;

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
    SystemRecoveryAlarm.deactivate();
    SystemRecoveryQuestLogUtil.initializeQuestLog();
    Game.system(HintSystem.class, HintSystem::resetHintProgress);
    SystemRecoveryProgressNet.reset();
    SystemRecoveryProgressNet.initialize();
    setupTerminal();
    setupPhone();
    setupRoomLabel();
    closeDoors();
    setupArchiveDoorLock();
    energy.setup();
    moduleStorage.setup();
    inventoryScanner.setup();
    transportStorage.setup();
    manualSorting.setup();
    bubbleSort.setup();
    dataArchive.setup();
    twoDimensionalStorage.setup();
    searchRobot.setup();
  }

  @Override
  protected void onTick() {
    showIntroForNewPlayers();
    triggerDialogPoints();
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

  /** Shows the room's lore only on a normal client, never while the level editor is active. */
  private void showIntroForNewPlayers() {
    if (Game.isHeadless() || LevelEditorSystem.active()) return;
    Game.levelEntities(Set.of(PlayerComponent.class))
        .filter(player -> introShownPlayers.add(player.id()))
        .forEach(
            player ->
                BlackFadeCutscene.show(
                    rooms.systemRecovery.util.SystemRecoveryText.introPages(),
                    true,
                    true,
                    () -> finishIntroForPlayer(player.id()),
                    player.id()));
  }

  /** Shows the controls immediately after the intro, before the opening phone call starts. */
  private void finishIntroForPlayer(int playerId) {
    if (!controlsShownPlayers.add(playerId)) {
      triggerOpeningPhoneCall();
      return;
    }
    DialogFactory.showDialogDialog(
        SystemRecoveryText.controls(), this::triggerOpeningPhoneCall, playerId);
  }

  /** Spawns the phone and keeps it interactable after the opening call has been answered. */
  private void setupPhone() {
    phone = DecoFactory.createDeco(getPoint("phone"), Deco.Phone);
    phone.remove(DecoComponent.class);
    DrawSystem.getInstance().changeEntityDepth(phone, DepthLayer.AbovePlayer.depth());
    Game.add(phone);
    updatePhoneInteraction();
  }

  /** Starts the one opening call after the lore cutscene has finished. */
  private void triggerOpeningPhoneCall() {
    if (openingPhoneCallTriggered || phone == null) return;
    openingPhoneCallTriggered = true;
    phoneRinging = true;
    Sounds.play(LastHourSounds.PHONE_RINGING);
    updatePhoneInteraction();
    ringingPhoneEmote =
        EmoteFactory.createEmote(EntityUtils.getPosition(phone), Emote.EXCLAMATION, 60 * 60 * 1000);
    Game.add(ringingPhoneEmote);
  }

  /** Updates the phone interaction between the ringing and answered states. */
  private void updatePhoneInteraction() {
    if (phone == null) return;
    phone.remove(InteractionComponent.class);
    phone.add(
        new InteractionComponent(
            new Interaction(
                (_, who) -> {
                  if (phoneRinging) {
                    DialogFactory.showDialogDialog(
                        SystemRecoveryText.phoneCall("opening-call"),
                        "logo/cat_logo_64x64.png",
                        () -> finishOpeningPhoneCall(who.id()),
                        who.id());
                    return;
                  }
                  SystemRecoveryHintPhone.request(who);
                })));
  }

  /** Stops the ringing and records the first terminal task after the call is finished. */
  private void finishOpeningPhoneCall(int playerId) {
    if (terminalsUnlocked) return;
    terminalsUnlocked = true;
    phoneRinging = false;
    updatePhoneInteraction();
    if (ringingPhoneEmote != null) {
      Game.remove(ringingPhoneEmote);
      ringingPhoneEmote = null;
    }
    SystemRecoveryPuzzleEvents.started(SystemRecoveryPuzzle.ENERGY);
    SystemRecoveryPuzzleEvents.openingCallFinished();
    SystemRecoveryQuestLogUtil.addDialogEntry("riddle1", "array");
  }

  /** Starts the next instruction when a player reaches a configured room-entry trigger. */
  private void triggerDialogPoints() {
    Game.levelEntities(Set.of(PlayerComponent.class))
        .filter(player -> player.isPresent(engine.components.PositionComponent.class))
        .forEach(
            player ->
                SystemRecoveryDialogTriggers.ROOM_ENTRY.forEach(
                    trigger -> {
                      try {
                        Point triggerPoint = getPoint(trigger.pointName());
                        Point playerPoint =
                            player
                                .fetch(engine.components.PositionComponent.class)
                                .orElseThrow()
                                .position();
                        if (!isDialogTriggerEnabled(trigger)) return;
                        String triggerKey = trigger.pointName() + ":" + player.id();
                        if (playerPoint.distanceSquared(triggerPoint) <= 1.0
                            && triggeredDialogPoints.add(triggerKey)) {
                          storyDialogs.announceForPlayer(trigger.step(), player.id());
                        }
                      } catch (RuntimeException ignored) {
                        // A trigger point may be absent in an intermediate editor version.
                      }
                    }));
  }

  private boolean isDialogTriggerEnabled(SystemRecoveryDialogTriggers.DialogTrigger trigger) {
    if (SystemRecoveryDialogTriggers.MODULE_STORAGE.equals(trigger.pointName())) {
      return energy.batteryInserted();
    }
    if (SystemRecoveryDialogTriggers.INVENTORY_SCANNER.equals(trigger.pointName())) {
      return moduleStorage.lengthInspected();
    }
    if (SystemRecoveryDialogTriggers.TRANSPORT_STORAGE.equals(trigger.pointName())) {
      return inventoryScanner.completed();
    }
    if (SystemRecoveryDialogTriggers.MANUAL_SORTING.equals(trigger.pointName())) {
      return transportStorage.completed();
    }
    if (SystemRecoveryDialogTriggers.TWO_DIMENSIONAL_STORAGE.equals(trigger.pointName())) {
      return dataArchive.completed();
    }
    return true;
  }

  private static SystemRecoveryLevel active() {
    return (SystemRecoveryLevel) Game.currentLevel().orElseThrow();
  }

  /** Returns whether the opening call has unlocked the room's computer terminals. */
  public static boolean terminalsUnlocked() {
    return currentLevel().map(level -> level.terminalsUnlocked).orElse(false);
  }

  /** Materializes the energy array for riddle 1, terminal step 1. */
  public static void spawnEnergyCrates() {
    active().energy.spawnEnergyCrates();
  }

  /** Runs terminal input with the submitting player attached to story callbacks. */
  public static boolean interpretTerminalInput(String source, int playerId) {
    if (!terminalsUnlocked()) return false;
    int state = TerminalInterpreter.instance().currentState();
    if (TerminalInterpreter.instance().currentState()
        == TerminalInterpreterSetup.SEARCH_ROBOT_PROGRAM_STATE) {
      return SystemRecoveryStoryDialogs.withTerminalPlayer(
          playerId,
          () ->
              InterpretationCallbacks.withTerminalAttempt(
                  state,
                  source,
                  () -> {
                    InterpretationCallbacks.onIncorrectTerminalInput();
                    return false;
                  }));
    }
    return SystemRecoveryStoryDialogs.withTerminalPlayer(
        playerId,
        () ->
            InterpretationCallbacks.withTerminalAttempt(
                state, source, () -> TerminalInterpreter.instance().interpret(source)));
  }

  /** Advances one terminal state in debug mode with the submitting player attached to the story. */
  public static boolean advanceTerminalStateForDebug(int playerId) {
    if (!terminalsUnlocked()) return false;
    int state = TerminalInterpreter.instance().currentState();
    return SystemRecoveryStoryDialogs.withTerminalPlayer(
        playerId,
        () ->
            InterpretationCallbacks.withTerminalAttempt(
                state,
                "<debug-next-step>",
                () -> {
                  boolean advanced = TerminalInterpreter.instance().advanceCurrentStateForDebug();
                  if (advanced) SystemRecoveryProgressNet.debugAdvanceAfterTerminal(state);
                  return advanced;
                }));
  }

  /** Shows the authoritative token state of the System Recovery Petri net to one debug client. */
  public static void showPetriNetDebug(int playerId) {
    if (!SystemRecovery.DEBUG_MODE) return;
    DialogFactory.showTextDialog(
        SystemRecoveryProgressNet.debugSnapshot(),
        SystemRecoveryText.text("computer.debug-petri-net-title"),
        () -> {},
        SystemRecoveryText.text("computer.debug-close"),
        playerId);
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
    addDoorLabel(
        "label_modulspeicher",
        SystemRecoveryText.text("world.labels.module-storage"),
        SystemRecoveryText.text("world.labels.room", 2),
        moduleStorage::completed);
    addDoorLabel(
        "label_inventarscanner",
        SystemRecoveryText.text("world.labels.inventory-scanner"),
        SystemRecoveryText.text("world.labels.room", 3),
        inventoryScanner::completed);
    addDoorLabel(
        "label_transportlager",
        SystemRecoveryText.text("world.labels.transport-storage"),
        SystemRecoveryText.text("world.labels.room", 4),
        transportStorage::completed);
    addDoorLabel(
        "label_datenspeicher",
        SystemRecoveryText.text("world.labels.data-storage"),
        SystemRecoveryText.text("world.labels.room", 5),
        manualSorting::completed);
    addDoorLabel(
        "label_sortmachine",
        SystemRecoveryText.text("world.labels.bubble-sort"),
        SystemRecoveryText.text("world.labels.room", 6),
        bubbleSort::completed);
    addDoorLabel(
        "label_archive",
        SystemRecoveryText.text("world.labels.data-archive"),
        SystemRecoveryText.text("world.labels.room", 7),
        dataArchive::completed);
    addDoorLabel(
        "label_speicher",
        SystemRecoveryText.text("world.labels.two-dimensional-storage"),
        SystemRecoveryText.text("world.labels.room", 8),
        twoDimensionalStorage::completed);
    addDoorLabel(
        "label_suchroboter",
        SystemRecoveryText.text("world.labels.search-robot"),
        SystemRecoveryText.text("world.labels.room", 9),
        searchRobot::completed);
    addDoorLabel(
        "label_systemcore",
        SystemRecoveryText.text("world.labels.system-core"),
        SystemRecoveryText.text("world.labels.system-core-room"),
        () -> systemCoreRiddleCompleted);
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

  /** Shows the module names and their required indices on the room 2 display. */
  public static void showModuleAssignments() {
    active().moduleStorage.showModuleAssignments();
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

  /** Opens the next room after the data archive arrays have been accepted. */
  public static void completeDataArchive() {
    active().dataArchive.complete();
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

  /** Activates the twelve storage cells after the riddle 8 array declaration. */
  public static void activateStorageMatrix() {
    active().twoDimensionalStorage.activateMatrix();
  }

  /** Shows the three populated coordinates after the riddle 8 assignments. */
  public static void fillStorageMatrix() {
    active().twoDimensionalStorage.fillMatrix();
  }

  /** Completes riddle 8 and starts the authoritative chip delivery animation. */
  public static void completeStorageMatrix() {
    active().twoDimensionalStorage.complete();
  }

  /** Returns the server-authoritative state of one storage matrix cell. */
  public static String storageCellState(String entityName) {
    String[] parts = entityName.split("_");
    if (parts.length < 5) return "empty";
    try {
      return active()
          .twoDimensionalStorage
          .cellState(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]));
    } catch (NumberFormatException ignored) {
      return "empty";
    }
  }

  /** Returns whether the system-core access script has been accepted by the server. */
  public static boolean systemCoreAccessGranted() {
    return active().systemCoreAccessGranted;
  }

  /** Returns whether the final system-core terminal riddle has been solved. */
  public static boolean systemCoreRiddleCompleted() {
    return active().systemCoreRiddleCompleted;
  }

  /** Marks the final system-core terminal riddle as solved. */
  public static void completeSystemCoreRiddle() {
    SystemRecoveryLevel level = active();
    if (level.systemCoreRiddleCompleted) return;
    level.systemCoreRiddleCompleted = true;
    SystemRecoveryPuzzleEvents.solved(SystemRecoveryPuzzle.SYSTEM_CORE);
  }

  /** Opens the system-core door after the access module's script was executed. */
  public static void completeSystemCoreAccess(int playerId) {
    SystemRecoveryLevel level = active();
    if (level.systemCoreAccessGranted) return;
    level.systemCoreAccessGranted = true;
    ((DoorTile) level.tileAt(level.getPoint("door_systemcore")).orElseThrow()).open();
    SystemRecoveryAlarm.activate();
  }
}
