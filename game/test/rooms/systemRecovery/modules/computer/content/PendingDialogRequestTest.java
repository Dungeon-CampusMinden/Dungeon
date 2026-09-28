package rooms.systemRecovery.modules.computer.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feature.hud.dialogs.DialogFeedbackFingerprint;
import org.junit.jupiter.api.Test;

class PendingDialogRequestTest {

  @Test
  void allowsOnlyOneRequestAndKeepsItsSubmittedSourceFrozen() {
    PendingDialogRequest request = new PendingDialogRequest();
    String submitted = "int[] module = new int[5];";
    String fingerprint = DialogFeedbackFingerprint.of(submitted);

    assertTrue(request.begin(submitted));
    assertFalse(request.begin("int count = 0;"));
    assertEquals(submitted, request.sourceFor(fingerprint).orElseThrow());
    assertTrue(request.sourceFor("another-source").isEmpty());
    assertTrue(request.pending());
  }

  @Test
  void staleResponseDoesNotResolveCurrentRequestAndMatchingResponseClearsIt() {
    PendingDialogRequest request = new PendingDialogRequest();
    String submitted = "module[0] = \"CPU\";";
    String fingerprint = DialogFeedbackFingerprint.of(submitted);
    request.begin(submitted);

    assertTrue(request.resolve("stale-response").isEmpty());
    assertTrue(request.pending());
    assertEquals(submitted, request.resolve(fingerprint).orElseThrow());
    assertFalse(request.pending());
  }

  @Test
  void responseWithoutFingerprintCannotResolveThePendingRequest() {
    PendingDialogRequest request = new PendingDialogRequest();
    request.begin("source");

    assertTrue(request.resolve("").isEmpty());
    assertTrue(request.pending());
    assertEquals("source", request.resolve(DialogFeedbackFingerprint.of("source")).orElseThrow());
    assertTrue(request.resolve("").isEmpty());
  }
}
