package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.dialect.arith.Arith;
import blockly.vm.dgir.dialect.builtin.Builtin;
import blockly.vm.dgir.dialect.func.Func;
import blockly.vm.dgir.dialect.io.IO;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

public class Utils {
  public static ObjectMapper getMapper(boolean prettyPrint, boolean registerDialects) {
    if (registerDialects) {
      registerAllDialects();
    }

    var mapperBuilder = JsonMapper.builder();
    mapperBuilder.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
    if (prettyPrint) {
      mapperBuilder = mapperBuilder.enable(tools.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    ObjectMapper mapper = mapperBuilder.build();
    return mapper;
  }

  public static void registerAllDialects(){
    new Builtin().init();
    new IO().init();
    new Arith().init();
    new Func().init();
  }
}
