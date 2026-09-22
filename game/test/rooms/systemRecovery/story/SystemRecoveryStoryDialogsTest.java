package rooms.systemRecovery.story;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Verifies that every System Recovery story step uses its intended in-world speaker. */
class SystemRecoveryStoryDialogsTest {

  /** System instructions for riddles two and three are attributed to AXIOM. */
  @Test
  void attributesSystemInstructionsToAxiom() {
    assertEquals("axiom", SystemRecoveryStoryDialogs.MODULE_ARRAY.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.MODULE_VALUES.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.MODULE_ASSIGNMENT.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.GPU_FAULT.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.READ_MODULE_LENGTH.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.OPEN_SCANNER_DOOR.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.SCANNER_CODE.speakerKey());
    assertEquals("axiom", SystemRecoveryStoryDialogs.SCANNER_LEVER.speakerKey());
  }

  /** ECHO and the search robot keep their own distinct quest-log speaker labels. */
  @Test
  void preservesOtherStorySpeakers() {
    assertEquals("echo", SystemRecoveryStoryDialogs.MANUAL_SORTING.speakerKey());
    assertEquals("echo", SystemRecoveryStoryDialogs.CENTRAL_META.speakerKey());
    assertEquals("search-robot", SystemRecoveryStoryDialogs.SEARCH_ROBOT_START.speakerKey());
    assertEquals("search-robot", SystemRecoveryStoryDialogs.SEARCH_ROBOT_COMPLETE.speakerKey());
  }
}
