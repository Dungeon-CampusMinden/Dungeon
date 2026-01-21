package blockly.vm.dgir.core;

import blockly.vm.dgir.core.type.Type;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Value {
  private final String ident;
  private final Type type;

  private static int idCounter = 0;

  public Value() {
    this.ident = null;
    this.type = null;
  }

  @JsonCreator
  public Value(@JsonProperty("ident") String ident, @JsonProperty("type") Type type) {
    this.ident = ident;
    this.type = type;
  }

  public static Value createUnique(String identBase, Type type) {
    return new Value(getUniqueIdent(identBase), type);
  }

  public static String getUniqueIdent(String base) {
    return base + "_" + idCounter++;
  }

  public Type getType() {
    return type;
  }

  @JsonIgnore
  public Object getValue() {
    throw new UnsupportedOperationException();
  }

  public String getIdent() {
    return ident;
  }
}
