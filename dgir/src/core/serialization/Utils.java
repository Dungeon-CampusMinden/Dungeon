package core.serialization;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Factory for pre-configured Jackson {@link ObjectMapper} instances used across the DGIR
 * serialization layer.
 */
public class Utils {

  /**
   * Create an {@link ObjectMapper} suitable for DGIR IR serialization.
   *
   * <p>Empty JSON arrays are suppressed in all output. When {@code prettyPrint} is {@code true},
   * the output is indented for human readability.
   *
   * @param prettyPrint {@code true} to enable indented (pretty-print) output.
   * @return a configured {@link ObjectMapper}.
   */
  public static ObjectMapper getMapper(boolean prettyPrint) {
    var mapperBuilder = JsonMapper.builder();
    mapperBuilder.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
    if (prettyPrint) {
      mapperBuilder =
          mapperBuilder.enable(tools.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    ObjectMapper mapper = mapperBuilder.build();
    return mapper;
  }
}
