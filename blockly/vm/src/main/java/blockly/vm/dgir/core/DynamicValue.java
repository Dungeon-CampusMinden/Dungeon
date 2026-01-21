package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DynamicValue implements IValue {
  private final String ident;
  private final Type type;

  private static int idCounter = 0;

  public DynamicValue() {
    this.ident = null;
    this.type = null;
  }

  @JsonCreator
  public DynamicValue(@JsonProperty("ident") String ident, @JsonProperty("type") Type type) {
    this.ident = ident;
    this.type = type;
  }

  /**
   * Creates a new DynamicValue with a unique ident based on a constant values type.
   * @param identBase The base ident for the unique ident.
   * @param constantValue The constant value to base the ident on.
   */
  public DynamicValue(String identBase, ConstantValue constantValue){
    this(getUniqueIdent(identBase), constantValue.getType());
  }

  public static DynamicValue createUnique(String identBase, Type type){
    return new DynamicValue(getUniqueIdent(identBase), type);
  }

  public static String getUniqueIdent(String base){
    return base + "_" + idCounter++;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public Object getValue() {
    throw new UnsupportedOperationException();
  }

  public String getIdent() {
    return ident;
  }
}
