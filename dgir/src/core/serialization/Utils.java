package core.serialization;

import core.Dialect;
import dialect.arith.Arith;
import dialect.builtin.Builtin;
import dialect.func.Func;
import dialect.io.IO;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

public class Utils {
  public static ObjectMapper getMapper(boolean prettyPrint) {
    var mapperBuilder = JsonMapper.builder();
    mapperBuilder.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
    if (prettyPrint) {
      mapperBuilder = mapperBuilder.enable(tools.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    ObjectMapper mapper = mapperBuilder.build();
    return mapper;
  }
}
