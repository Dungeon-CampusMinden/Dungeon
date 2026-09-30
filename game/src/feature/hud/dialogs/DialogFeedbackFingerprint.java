package feature.hud.dialogs;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Creates stable, non-readable fingerprints for correlating asynchronous dialog responses. */
public final class DialogFeedbackFingerprint {

  private DialogFeedbackFingerprint() {}

  /**
   * Returns a SHA-256 fingerprint for submitted dialog content.
   *
   * @param source submitted content
   * @return lowercase hexadecimal fingerprint
   */
  public static String of(String source) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256")
              .digest((source == null ? "" : source).getBytes(StandardCharsets.UTF_8));
      StringBuilder result = new StringBuilder(digest.length * 2);
      for (byte value : digest) {
        result.append(String.format("%02x", value));
      }
      return result.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(
          "SHA-256 is required for dialog response correlation", exception);
    }
  }
}
