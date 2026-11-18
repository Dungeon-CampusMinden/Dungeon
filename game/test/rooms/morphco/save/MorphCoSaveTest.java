package rooms.morphco.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import feature.entities.CharacterClass;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
  void checkpointSurvivesWritingAndReading(@TempDir Path directory) {
    MorphCoSave.SaveData save =
        new MorphCoSave.SaveData(
            EnumSet.of(MorphCoMilestone.ALARM_OFF, MorphCoMilestone.CAUSES_FOUND),
            Set.of("arrival", "shafts-filled"),
            EnumSet.of(CircumfixPuzzle.Circumfix.GE_E),
            3,
            2,
            UUID.randomUUID(),
            61_000,
            true,
            "Andre",
            Map.of("Kim", CharacterClass.values()[0]));
    Path file = directory.resolve("save.json");

    MorphCoSave.write(file, save);

    assertEquals(Optional.of(save), MorphCoSave.read(file));
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
