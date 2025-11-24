package core.network.server;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Utility class for generating, validating, and clearing session tokens. Session tokens are used
 * for secure identification of client sessions.
 */
public final class SessionTokenUtil {

  private static final SecureRandom RNG = new SecureRandom();

  /**
   * Generates a cryptographically secure random session token.
   *
   * @param lengthBytes The length of the token in bytes. Must be a positive integer.
   * @return A byte array containing the generated token.
   * @throws IllegalArgumentException if the specified length is not positive.
   */
  public static byte[] generate(int lengthBytes) {
    if (lengthBytes <= 0) throw new IllegalArgumentException("length must be positive");
    byte[] token = new byte[lengthBytes];
    RNG.nextBytes(token);
    return token;
  }

  /**
   * Validates a provided session token against the expected token. Uses a constant-time comparison
   * to prevent timing attacks.
   *
   * @param expectedToken The expected token to validate against.
   * @param providedToken The provided token to validate.
   * @return true if the tokens match, false otherwise.
   */
  public static boolean validate(byte[] expectedToken, byte[] providedToken) {
    if (expectedToken == null || providedToken == null) return false;
    // Constant-time comparison to avoid timing attacks
    return MessageDigest.isEqual(expectedToken, providedToken);
  }

  /**
   * Clears the contents of a token by overwriting it with zeros. This is useful for securely
   * removing sensitive data from memory.
   *
   * @param token The token to clear. If null, no action is taken.
   */
  public static void clear(byte[] token) {
    if (token != null) Arrays.fill(token, (byte) 0);
  }
}
