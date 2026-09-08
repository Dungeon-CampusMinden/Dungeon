package rooms.systemRecovery.util.interpreter;

import com.badlogic.gdx.graphics.Color;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.sound.SoundSpec;
import engine.utils.Point;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.path.SimpleIPath;
import feature.hud.DialogUtils;
import rooms.systemRecovery.level.SystemRecoveryLevel;

/** Applies room-side effects for terminal interpretation outcomes. */
public final class InterpretationCallbacks {

  private static final String SUCCESS_SOUND = "retro_event_correct";
  private static final String FAILURE_SOUND = "retro_event_wrong";

  private InterpretationCallbacks() {}

  /** Handles riddle 1 step 1: initialize the energy array. */
  public static void onRiddleOneStepOneEnergyArrayInitialized() {
    showCorrectTerminalInputDialog();
    spawnEnergieCrates();
  }

  /** Handles riddle 1 step 2: set all energy values. */
  public static void onRiddleOneStepTwoEnergyValuesSet() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.completeEnergyPuzzle();
    markEnergyCratesCorrect();
  }

  /** Handles riddle 2 step 1: initialize the module array. */
  public static void onRiddleTwoStepOneModuleArrayInitialized() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.activateModuleSockets();
  }

  /** Handles riddle 2 step 2: assign all modules. */
  public static void onRiddleTwoStepTwoModulesAssigned() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.spawnModuleChips();
  }

  /** Handles riddle 2 step 3: remove the GPU module. */
  public static void onRiddleTwoStepThreeGpuRemoved() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.removeGpuChip();
  }

  /** Handles riddle 2 step 4: read the module array length. */
  public static void onRiddleTwoStepFourModuleLengthRead() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.showModuleArrayLength();
    // port the modules to the inventory scanner
    DungeonLevel level = (DungeonLevel) Game.currentLevel().get();
    for (int i = 0; i < 5; i++) {
      Point target = level.getPoint("scanner" + i);
      Game.entityAtPoint(level.getPoint("s" + i))
          .forEach(
              entity -> entity.fetch(PositionComponent.class).ifPresent(pc -> pc.position(target)));
    }
  }

  /** Handles riddle 3 step 1: count all non-null modules. */
  public static void onRiddleThreeStepOneInventoryScannerCompleted() {
    showCorrectTerminalInputDialog();
    // TODO: Inventarscanner: Anzahl intakter Module an Scanner/UI uebergeben.
  }

  /** Handles riddle 4 step 1: create the packages array. */
  public static void onRiddleFourStepOnePackagesCreated() {
    showCorrectTerminalInputDialog();
    // TODO: Transportlager: Pakete im Lagerbereich vorbereiten.
  }

  /** Handles riddle 4 step 2: let the robot collect packages. */
  public static void onRiddleFourStepTwoPackagesCollected() {
    showCorrectTerminalInputDialog();
    // TODO: Transportlager: Roboterbewegung starten und Pakettransfer abschliessen.
  }

  /** Handles riddle 7 step 1: create all archive arrays. */
  public static void onRiddleSevenStepOneDataArchiveLoaded() {
    showCorrectTerminalInputDialog();
    // TODO: Datenarchiv: Archivdaten anzeigen/freischalten.
  }

  /** Handles riddle 8 step 1: create the two-dimensional storage array. */
  public static void onRiddleEightStepOneStorageCreated() {
    showCorrectTerminalInputDialog();
    // TODO: Zweidimensionales Lager: Raster sichtbar initialisieren.
  }

  /** Handles riddle 8 step 2: fill the two-dimensional storage array. */
  public static void onRiddleEightStepTwoStorageFilled() {
    showCorrectTerminalInputDialog();
    // TODO: Zweidimensionales Lager: Werte im Raster platzieren.
  }

  /** Handles riddle 8 step 3: read a two-dimensional storage cell. */
  public static void onRiddleEightStepThreeStorageRead() {
    showCorrectTerminalInputDialog();
    // TODO: Zweidimensionales Lager: gelesene Zelle hervorheben/auswerten.
  }

  /** Handles riddle 9 step 1: search the map and collect batteries. */
  public static void onRiddleNineStepOneSearchRobotCompleted() {
    showCorrectTerminalInputDialog();
    // TODO: Suchroboter: Batterien auf der Map einsammeln und ggf. Batterie-Items spawnen.
  }

  /** Handles riddle 10 step 1: bubble sort the central array. */
  public static void onRiddleTenStepOneBubbleSortCompleted() {
    showCorrectTerminalInputDialog();
    // TODO: Zentrales Rechenzentrum: sortierte Werte an Maschine/Rechenzentrum melden.
  }

  /** Handles riddle 10 step 2: count all non-null modules. */
  public static void onRiddleTenStepTwoModulesCounted() {
    showCorrectTerminalInputDialog();
    // TODO: Zentrales Rechenzentrum: Modulanzahl in Zentralstatus uebernehmen.
  }

  /** Handles riddle 10 step 3: search the map and collect batteries. */
  public static void onRiddleTenStepThreeBatteriesCollected() {
    showCorrectTerminalInputDialog();
    // TODO: Zentrales Rechenzentrum: finale Batterie-Suche bestaetigen und naechste Aktion
    // ausloesen.
  }

  /** Handles any terminal input that does not match the current requirement. */
  public static void onIncorrectTerminalInput() {
    showIncorrectTerminalInputDialog();
    // TODO: Falsche Eingabe: aktuelles Raetselobjekt rot/fehlerhaft markieren, falls vorhanden.
  }

  private static void spawnEnergieCrates() {
    DungeonLevel level = (DungeonLevel) Game.currentLevel().get();
    Game.add(rooms.systemRecovery.entities.EntityFactory.cryoBox(level.getPoint("a0"), false));
    Game.add(rooms.systemRecovery.entities.EntityFactory.cryoBox(level.getPoint("a1"), false));
    Game.add(rooms.systemRecovery.entities.EntityFactory.cryoBox(level.getPoint("a2"), false));
    Game.add(rooms.systemRecovery.entities.EntityFactory.cryoBox(level.getPoint("a3"), false));
    Game.add(rooms.systemRecovery.entities.EntityFactory.cryoBox(level.getPoint("a4"), false));
  }

  private static void markEnergyCratesCorrect() {

    DungeonLevel level = (DungeonLevel) Game.currentLevel().get();
    markEnergyCrateCorrect(
        level, "a0", Integer.valueOf(TerminalInterpreterSetup.ENERGIE_VALUE_0).floatValue() / 100);
    markEnergyCrateCorrect(
        level, "a1", Integer.valueOf(TerminalInterpreterSetup.ENERGIE_VALUE_1).floatValue() / 100);
    markEnergyCrateCorrect(
        level, "a2", Integer.valueOf(TerminalInterpreterSetup.ENERGIE_VALUE_2).floatValue() / 100);
    markEnergyCrateCorrect(
        level, "a3", Integer.valueOf(TerminalInterpreterSetup.ENERGIE_VALUE_3).floatValue() / 100);
    markEnergyCrateCorrect(
        level, "a4", Integer.valueOf(TerminalInterpreterSetup.ENERGIE_VALUE_4).floatValue() / 100);
  }

  private static void markEnergyCrateCorrect(
      DungeonLevel level, String pointName, float fillPercentage) {
    Game.entityAtPoint(level.getPoint(pointName))
        .findFirst()
        .flatMap(e -> e.fetch(DrawComponent.class))
        .ifPresent(
            dc ->
                dc.shaders()
                    .add(
                        "energieShader",
                        new EnergyFillShader(
                                fillPercentage,
                                Color.BLUE,
                                TextureMap.instance()
                                    .textureAt(new SimpleIPath("objects/tech/CryoBox.png")))
                            .animMagnitude(0)));
  }

  /** Shows feedback for a correct terminal input. */
  public static void showCorrectTerminalInputDialog() {
    DialogUtils.showTextPopup("War richtig", "Terminal");
    Game.audio().playGlobal(SoundSpec.builder(SUCCESS_SOUND));
  }

  /** Shows feedback for an incorrect terminal input. */
  public static void showIncorrectTerminalInputDialog() {
    DialogUtils.showTextPopup("war falsch", "Terminal");
    Game.audio().playGlobal(SoundSpec.builder(FAILURE_SOUND));
  }
}
