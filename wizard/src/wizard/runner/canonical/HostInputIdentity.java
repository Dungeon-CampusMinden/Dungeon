package wizard.runner.canonical;

import com.fasterxml.jackson.databind.JsonNode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/** Computes the deterministic identity of a complete validated DEER document. */
public final class HostInputIdentity {
  private HostInputIdentity() {}

  /**
   * Computes lowercase SHA-256 over the canonical host-input identity.
   *
   * <p>The identity is the RFC 8785 canonical form of the complete parsed DEER document.
   *
   * @param deer complete parsed and validated DEER document
   * @return lowercase SHA-256 of the canonical DEER document
   */
  public static String sha256(final JsonNode deer) {
    Objects.requireNonNull(deer, "deer");
    return digest(CanonicalJson.encode(deer).getBytes(StandardCharsets.UTF_8));
  }

  private static String digest(final byte[] bytes) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
