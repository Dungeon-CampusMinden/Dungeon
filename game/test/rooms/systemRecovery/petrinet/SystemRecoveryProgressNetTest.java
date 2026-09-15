package rooms.systemRecovery.petrinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import feature.hints.HintSystem;
import feature.petrinet.PetriNetSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;

/** Regression tests for the server-side System Recovery progress and hint state. */
class SystemRecoveryProgressNetTest {

  private PetriNetSystem petriNet;

  @BeforeEach
  void setUp() {
    Game.removeAllEntities();
    Game.removeAllSystems();
    SystemRecoveryProgressNet.reset();

    petriNet = new PetriNetSystem();
    Game.add(petriNet);
    Game.add(new HintSystem());
    SystemRecoveryProgressNet.initialize();
  }

  @AfterEach
  void tearDown() {
    SystemRecoveryProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  /** Verifies that completing the physical Bubble Sort machine activates the archive hints. */
  @Test
  void bubbleSortCompletionActivatesArchiveHints() {
    SystemRecoveryProgressNet.openingCallFinished();
    tick();

    SystemRecoveryProgressNet.terminalSuccess(0);
    tick();
    SystemRecoveryProgressNet.terminalSuccess(1);
    tick();
    SystemRecoveryProgressNet.successfulInteraction(SystemRecoveryPuzzle.ENERGY, "battery-inserted");
    tick();

    SystemRecoveryProgressNet.terminalSuccess(2);
    tick();
    SystemRecoveryProgressNet.terminalSuccess(3);
    tick();
    SystemRecoveryProgressNet.terminalSuccess(4);
    tick();
    SystemRecoveryProgressNet.terminalSuccess(5);
    tick();
    SystemRecoveryProgressNet.successfulInteraction(
        SystemRecoveryPuzzle.MODULE_STORAGE, "inspect-length");
    tick();
    SystemRecoveryProgressNet.successfulInteraction(SystemRecoveryPuzzle.MODULE_STORAGE, "5");
    tick();
    SystemRecoveryProgressNet.terminalSuccess(6);
    tick();

    SystemRecoveryProgressNet.successfulInteraction(
        SystemRecoveryPuzzle.INVENTORY_SCANNER, "lever");
    tick();
    SystemRecoveryProgressNet.solved(SystemRecoveryPuzzle.INVENTORY_SCANNER);
    tick();
    SystemRecoveryProgressNet.successfulInteraction(SystemRecoveryPuzzle.INVENTORY_SCANNER, "4");
    tick();

    SystemRecoveryProgressNet.terminalSuccess(7);
    tick();
    SystemRecoveryProgressNet.terminalSuccess(8);
    tick();
    SystemRecoveryProgressNet.successfulInteraction(
        SystemRecoveryPuzzle.TRANSPORT_STORAGE, "all-packages-collected");
    tick();
    SystemRecoveryProgressNet.solved(SystemRecoveryPuzzle.MANUAL_SORTING);
    tick();

    SystemRecoveryProgressNet.successfulInteraction(SystemRecoveryPuzzle.BUBBLE_SORT, "insert");
    tick();
    SystemRecoveryProgressNet.successfulInteraction(SystemRecoveryPuzzle.BUBBLE_SORT, "source");
    tick();
    SystemRecoveryProgressNet.solved(SystemRecoveryPuzzle.BUBBLE_SORT);
    tick();

    SystemRecoveryProgressPlace activePlace = SystemRecoveryProgressNet.activePlace().orElseThrow();
    assertEquals(SystemRecoveryProgressPlace.R7_ARCHIVE, activePlace, "active place: " + activePlace);
    Game.system(
        HintSystem.class,
        hintSystem -> assertFalse(hintSystem.peekSharedHint().isEmpty()));
  }

  /** Verifies that the debug snapshot exposes occupied and empty places on the server. */
  @Test
  void debugSnapshotShowsTokenLocations() {
    String initialSnapshot = SystemRecoveryProgressNet.debugSnapshot();

    assertTrue(
        initialSnapshot.contains(
            SystemRecoveryText.text("computer.debug-petri-net-token", 1)
                + " OPENING_CALL_PENDING"));
    assertTrue(
        initialSnapshot.contains(
            SystemRecoveryText.text("computer.debug-petri-net-empty") + " R1_ARRAY"));
    assertTrue(
        initialSnapshot.contains(SystemRecoveryText.text("computer.debug-petri-net-events")));

    SystemRecoveryProgressNet.openingCallFinished();
    tick();

    String afterCallSnapshot = SystemRecoveryProgressNet.debugSnapshot();
    assertTrue(
        afterCallSnapshot.contains(
            SystemRecoveryText.text("computer.debug-petri-net-token", 1) + " R1_ARRAY"));
    assertTrue(
        afterCallSnapshot.contains(
            SystemRecoveryText.text("computer.debug-petri-net-empty")
                + " OPENING_CALL_PENDING"));
  }

  /** Verifies that debug terminal jumps do not leave the shared hint state behind. */
  @Test
  void debugTerminalJumpSynchronizesProgressPlace() {
    SystemRecoveryProgressNet.debugAdvanceAfterTerminal(8);

    assertEquals(
        SystemRecoveryProgressPlace.R5_COMPARE,
        SystemRecoveryProgressNet.activePlace().orElseThrow());
  }

  /**
   * Verifies the exact shared progression up to the inventory scanner.
   *
   * <p>This mirrors the authoritative callbacks instead of jumping between places. It protects
   * against the telephone retaining the initial energy hint after the player has entered the
   * scanner room.
   */
  @Test
  void exactProgressionActivatesScannerHints() {
    SystemRecoveryProgressNet.openingCallFinished();
    assertActive(SystemRecoveryProgressPlace.R1_ARRAY);

    SystemRecoveryProgressNet.terminalSuccess(0);
    assertActive(SystemRecoveryProgressPlace.R1_VALUES);
    SystemRecoveryProgressNet.terminalSuccess(1);
    assertActive(SystemRecoveryProgressPlace.R1_BATTERY);
    SystemRecoveryProgressNet.successfulInteraction(
        SystemRecoveryPuzzle.ENERGY, "battery-inserted");
    assertActive(SystemRecoveryProgressPlace.R2_ARRAY);

    SystemRecoveryProgressNet.terminalSuccess(2);
    assertActive(SystemRecoveryProgressPlace.R2_VALUES);
    SystemRecoveryProgressNet.terminalSuccess(3);
    assertActive(SystemRecoveryProgressPlace.R2_GPU);
    SystemRecoveryProgressNet.terminalSuccess(4);
    assertActive(SystemRecoveryProgressPlace.R2_LENGTH);
    SystemRecoveryProgressNet.terminalSuccess(5);
    assertActive(SystemRecoveryProgressPlace.R2_DISPLAY);
    SystemRecoveryProgressNet.successfulInteraction(
        SystemRecoveryPuzzle.MODULE_STORAGE, "inspect-length");
    assertActive(SystemRecoveryProgressPlace.R2_DOOR);
    SystemRecoveryProgressNet.successfulInteraction(SystemRecoveryPuzzle.MODULE_STORAGE, "5");
    assertActive(SystemRecoveryProgressPlace.R3_COUNT);

    SystemRecoveryProgressNet.terminalSuccess(6);
    assertActive(SystemRecoveryProgressPlace.R3_LEVER);
    SystemRecoveryProgressNet.successfulInteraction(SystemRecoveryPuzzle.INVENTORY_SCANNER, "lever");
    assertActive(SystemRecoveryProgressPlace.R3_SCAN);
    SystemRecoveryProgressNet.solved(SystemRecoveryPuzzle.INVENTORY_SCANNER);
    assertActive(SystemRecoveryProgressPlace.R3_DOOR);
    SystemRecoveryProgressNet.successfulInteraction(SystemRecoveryPuzzle.INVENTORY_SCANNER, "4");
    assertActive(SystemRecoveryProgressPlace.R4_ARRAY);
  }

  private void assertActive(SystemRecoveryProgressPlace expected) {
    assertEquals(expected, SystemRecoveryProgressNet.activePlace().orElseThrow());
  }

  private void tick() {
    petriNet.execute();
  }
}
