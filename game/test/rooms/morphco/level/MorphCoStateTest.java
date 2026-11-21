package rooms.morphco.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import feature.petrinet.PetriNetSystem;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.petrinet.MorphCoProgressNet;
import rooms.morphco.puzzles.CircumfixPuzzle.Circumfix;

/**
 * Checks the card, the corridor door and the lever of A-12 together with the shift lead's next
 * step.
 */
class MorphCoStateTest {

  @BeforeEach
  void setUp() {
    MorphCoProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
    Game.add(new PetriNetSystem());
    MorphCoProgressNet.initialize();
  }

  @AfterEach
  void tearDown() {
    MorphCoProgressNet.reset();
    Game.removeAllEntities();
    Game.removeAllSystems();
  }

  @Test
  void cardStepEndsWithTheLocker() {
    MorphCoState state = new MorphCoState();
    assertEquals(ShiftLead.Step.CARD, ShiftLead.hint(state));
    state.set(MorphCoState.CARD_TAKEN);
    assertEquals(ShiftLead.Step.ALARM, ShiftLead.hint(state));
  }

  @Test
  void corridorOpensWithTheFuseBoxAloneAndTheHallsDoNotWaitForTheComputer() {
    MorphCoState state = new MorphCoState();
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.ALARM_OFF));
    assertEquals(ShiftLead.Step.PC, ShiftLead.hint(state));
    state.set(MorphCoState.FUSE_BOX_WIRED);
    assertTrue(state.corridorOpen());
    assertEquals(ShiftLead.Step.REPAIR, ShiftLead.hint(state));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.HANDBOOK_READ));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.HATCH_UNLOCKED));

    // Computer first: the door still waits for the fuse box.
    MorphCoState computerFirst = new MorphCoState();
    assertTrue(MorphCoProgressNet.restore(EnumSet.of(MorphCoMilestone.ALARM_OFF)));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.CAUSES_FOUND));
    assertFalse(computerFirst.corridorOpen());
    assertEquals(ShiftLead.Step.FUSE_BOX, ShiftLead.hint(computerFirst));
  }

  @Test
  void leverStaysStuckUntilAllCircumfixesAreAssembled() {
    assertTrue(
        MorphCoProgressNet.restore(
            EnumSet.of(
                MorphCoMilestone.ALARM_OFF,
                MorphCoMilestone.CAUSES_FOUND,
                MorphCoMilestone.HATCH_UNLOCKED,
                MorphCoMilestone.AFFIXES_SORTED)));
    Set<String> sorted = Set.of(MorphCoState.FUSE_BOX_WIRED, MorphCoState.LOCK_SEEN);
    MorphCoState state = new MorphCoState();
    state.restore(sorted, Set.of(Circumfix.GE_E, Circumfix.BE_IG), 0, 0);
    state.set(MorphCoState.SHAFTS_FILLED);
    assertTrue(state.leverStuck());
    assertTrue(state.waitsForCircumfixes());
    assertEquals(ShiftLead.Step.CIRCUMFIX, ShiftLead.hint(state));
    assertTrue(state.assemble(Circumfix.GE_EN));
    assertFalse(state.leverStuck());
    assertEquals(ShiftLead.Step.LEVER, ShiftLead.hint(state));

    // Circumfixes before the shafts: once the shafts are filled, the lever moves at once.
    MorphCoState early = new MorphCoState();
    early.restore(sorted, EnumSet.allOf(Circumfix.class), 0, 0);
    assertTrue(early.leverStuck());
    assertEquals(ShiftLead.Step.SHAFTS, ShiftLead.hint(early));
    early.set(MorphCoState.SHAFTS_FILLED);
    assertFalse(early.waitsForCircumfixes());
    assertFalse(early.leverStuck());
    assertEquals(ShiftLead.Step.LEVER, ShiftLead.hint(early));
  }
}
