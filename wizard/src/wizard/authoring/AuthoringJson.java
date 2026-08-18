package wizard.authoring;

import java.util.Objects;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/** Strict JSON parsing used at the private authoring-host boundary. */
final class AuthoringJson {
  static final ObjectMapper MAPPER =
      JsonMapper.builder(
              JsonFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build())
          .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
          .enable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)
          .build();

  private AuthoringJson() {}

  static JsonNode parse(final byte[] bytes) {
    Objects.requireNonNull(bytes, "bytes");
    try {
      JsonNode result = MAPPER.readTree(bytes);
      if (result == null) {
        throw new IllegalArgumentException("JSON body is empty");
      }
      return result;
    } catch (JacksonException exception) {
      throw new IllegalArgumentException("JSON body is invalid", exception);
    }
  }

  static byte[] encode(final Object value) {
    try {
      return MAPPER.writeValueAsBytes(value);
    } catch (JacksonException exception) {
      throw new IllegalArgumentException("Value cannot be serialized as JSON", exception);
    }
  }
}
