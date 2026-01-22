package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonPropertyOrder({"type"})
public non-sealed abstract class Value implements ITypeLike, Serializable {
  /**
   * The kind of value, to draw a distinction between op results and block arguments.
   */
  public enum Kind {
    /**
     * This Value is the result of an operation.
     */
    OpResult,
    /**
     * This value is a block argument.
     */
    BlockArgument
  }

  private Type type;
  private Kind kind;

  protected Value(Type type, Kind kind) {
    this.type = type;
    this.kind = kind;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @JsonIgnore
  public Kind getKind() {
    return kind;
  }

  public void setKind(Kind kind) {
    this.kind = kind;
  }
}
