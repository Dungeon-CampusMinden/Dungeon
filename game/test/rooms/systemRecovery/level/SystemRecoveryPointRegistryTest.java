package rooms.systemRecovery.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import engine.level.DungeonLevel;
import engine.utils.Point;
import java.util.Map;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.story.SystemRecoveryDialogTriggers;

/** Verifies that the point registry keeps compatibility with the current level asset. */
class SystemRecoveryPointRegistryTest {

  /** The historical matrix-cell spelling resolves to the point currently stored in the level. */
  @Test
  void storageCellAliasResolves_riddle8() {
    DungeonLevel level = mock(DungeonLevel.class);
    when(level.getPoint(anyString())).thenReturn(new Point(0, 0));
    when(level.getPoint("storage_cell_2_2")).thenThrow(new java.util.NoSuchElementException());
    when(level.getPoint("storage_2_2")).thenReturn(new Point(7, 8));

    Map<String, Point> resolved = SystemRecoveryPointRegistry.resolve(level);

    assertEquals(new Point(7, 8), resolved.get("storage_cell_2_2"));
  }

  @Test
  void manualSortingStoryUsesItsDedicatedDialogTrigger() {
    assertEquals("dialog_trigger_manual_sorting", SystemRecoveryDialogTriggers.MANUAL_SORTING);
  }
}
