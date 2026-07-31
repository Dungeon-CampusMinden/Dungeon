package wizard.runner.canonical;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Objects;
import org.erdtman.jcs.JsonCanonicalizer;

/** RFC 8785 canonical JSON encoding for deterministic runner comparisons and hashes. */
public final class CanonicalJson {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private CanonicalJson() {}

  /**
   * Encodes a Jackson-serializable value as RFC 8785 canonical JSON.
   *
   * @param value value containing only supported JSON data
   * @return canonical JSON text
   * @throws IllegalArgumentException if the value cannot be represented as canonical JSON
   */
  public static String encode(final Object value) {
    Objects.requireNonNull(value, "value");
    try {
      byte[] json = MAPPER.writeValueAsBytes(value);
      return new JsonCanonicalizer(json).getEncodedString();
    } catch (JsonProcessingException exception) {
      throw new IllegalArgumentException("Value cannot be serialized as JSON", exception);
    } catch (IOException exception) {
      throw new IllegalArgumentException("Value cannot be canonicalized as JSON", exception);
    }
  }
}
