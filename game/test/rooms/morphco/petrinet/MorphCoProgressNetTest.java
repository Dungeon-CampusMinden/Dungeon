package rooms.morphco.petrinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Game;
import feature.petrinet.PetriNetSystem;
import java.util.EnumSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MorphCoProgressNetTest {

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

  private static void openProduction() {
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.ALARM_OFF));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.CAUSES_FOUND));
  }

  private static void repairA12() {
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.HATCH_UNLOCKED));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.AFFIXES_SORTED));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.A12_REPAIRED));
  }

  @Test
  void computerNeedsTheAlarmToBeOff() {
    assertFalse(MorphCoProgressNet.complete(MorphCoMilestone.CAUSES_FOUND));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.ALARM_OFF));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.CAUSES_FOUND));
    assertFalse(MorphCoProgressNet.complete(MorphCoMilestone.CAUSES_FOUND));
  }

  @Test
  void a12CanBeRepairedBeforeF04() {
    openProduction();
    repairA12();
    assertFalse(MorphCoProgressNet.complete(MorphCoMilestone.F04_REPAIRED));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.HANDBOOK_READ));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.WORD_CLASSES_SORTED));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.F04_REPAIRED));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.PRODUCTION_RESTARTED));
  }

  @Test
  void inflectionMachineWaitsForTheAffixSorter() {
    openProduction();
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.HANDBOOK_READ));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.WORD_CLASSES_SORTED));
    assertFalse(MorphCoProgressNet.complete(MorphCoMilestone.F04_REPAIRED));
    assertFalse(MorphCoProgressNet.complete(MorphCoMilestone.PRODUCTION_RESTARTED));
    repairA12();
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.F04_REPAIRED));
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.PRODUCTION_RESTARTED));
    assertEquals(EnumSet.allOf(MorphCoMilestone.class), MorphCoProgressNet.completedMilestones());
  }

  @Test
  void restoreAcceptsOnlyConsistentMarkings() {
    assertFalse(MorphCoProgressNet.restore(EnumSet.of(MorphCoMilestone.CAUSES_FOUND)));
    EnumSet<MorphCoMilestone> saved =
        EnumSet.of(
            MorphCoMilestone.ALARM_OFF,
            MorphCoMilestone.CAUSES_FOUND,
            MorphCoMilestone.HATCH_UNLOCKED);
    assertTrue(MorphCoProgressNet.restore(saved));
    assertEquals(saved, MorphCoProgressNet.completedMilestones());
    assertTrue(MorphCoProgressNet.complete(MorphCoMilestone.AFFIXES_SORTED));
  }
}
