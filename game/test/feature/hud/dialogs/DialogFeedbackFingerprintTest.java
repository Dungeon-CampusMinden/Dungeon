package feature.hud.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/** Verifies that asynchronous dialog responses can be correlated without exposing source text. */
class DialogFeedbackFingerprintTest {

  @Test
  void sameSourceHasSameFingerprint() {
    assertEquals(
        DialogFeedbackFingerprint.of("int[] energie = new int[5];"),
        DialogFeedbackFingerprint.of("int[] energie = new int[5];"));
  }

  @Test
  void differentSourceHasDifferentFingerprint() {
    assertNotEquals(
        DialogFeedbackFingerprint.of("int[] energie = new int[5];"),
        DialogFeedbackFingerprint.of("int[] energie = new int[4];"));
  }
}
