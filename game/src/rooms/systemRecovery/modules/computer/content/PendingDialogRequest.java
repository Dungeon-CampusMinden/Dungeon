package rooms.systemRecovery.modules.computer.content;

import feature.hud.dialogs.DialogFeedbackFingerprint;
import java.util.Optional;

/** Correlates one in-flight client dialog request with its submitted source. */
final class PendingDialogRequest {

  private String source;
  private String fingerprint;

  boolean begin(String submittedSource) {
    if (pending()) return false;
    source = submittedSource == null ? "" : submittedSource;
    fingerprint = DialogFeedbackFingerprint.of(source);
    return true;
  }

  Optional<String> resolve(String responseFingerprint) {
    if (!matches(responseFingerprint)) return Optional.empty();
    String completedSource = source;
    source = null;
    fingerprint = null;
    return Optional.of(completedSource);
  }

  boolean pending() {
    return source != null;
  }

  Optional<String> sourceFor(String responseFingerprint) {
    return matches(responseFingerprint) ? Optional.of(source) : Optional.empty();
  }

  private boolean matches(String responseFingerprint) {
    return pending() && fingerprint.equals(responseFingerprint);
  }
}
