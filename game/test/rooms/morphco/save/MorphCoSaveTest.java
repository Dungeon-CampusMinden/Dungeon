package rooms.morphco.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.puzzles.CircumfixPuzzle;

class MorphCoSaveTest {

  private static MorphCoSave.SaveData data(Set<MorphCoMilestone> milestones) {
    return new MorphCoSave.SaveData(
        milestones,
        Set.of("arrival"),
        EnumSet.of(CircumfixPuzzle.Circumfix.GE_E),
        2,
        1,
        UUID.randomUUID(),
        1000,
        null,
        "Andre",
        Map.of());
  }

  @Test
  void consistentCheckpointIsAccepted() {
    MorphCoSave.SaveData save =
        data(EnumSet.of(MorphCoMilestone.ALARM_OFF, MorphCoMilestone.CAUSES_FOUND));
    assertEquals(2, save.milestones().size());
    assertEquals(Set.of(CircumfixPuzzle.Circumfix.GE_E), save.assembled());
  }

  @Test
  void milestonesWithoutPrerequisitesAreRejected() {
    assertThrows(
        IllegalArgumentException.class, () -> data(EnumSet.of(MorphCoMilestone.CAUSES_FOUND)));
  }

  @Test
  void aFinishedRunCannotBeContinued() {
    assertThrows(IllegalArgumentException.class, () -> data(EnumSet.allOf(MorphCoMilestone.class)));
  }
}
