package blockly.vm.dgir.core.serialization;

import blockly.vm.dgir.core.DialectRegistry;
import blockly.vm.dgir.dialect.arith.Arith;
import blockly.vm.dgir.dialect.builtin.Builtin;
import blockly.vm.dgir.dialect.func.Func;
import blockly.vm.dgir.dialect.io.IO;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

public class Utility {
  public static ObjectMapper getMapper(boolean prettyPrint, boolean registerDialects) {
    if (registerDialects) {
      registerAllDialects();
    }

    var mapperBuilder = JsonMapper.builder();
    if (prettyPrint) {
      mapperBuilder = mapperBuilder.enable(tools.jackson.databind.SerializationFeature.INDENT_OUTPUT);
    }

    ObjectMapper mapper = mapperBuilder.build();
    return mapper;
  }

  public static void registerAllDialects(){
    DialectRegistry.registerDialect(Builtin.class);
    DialectRegistry.registerDialect(Func.class);
    DialectRegistry.registerDialect(IO.class);
    DialectRegistry.registerDialect(Arith.class);
  }
}
