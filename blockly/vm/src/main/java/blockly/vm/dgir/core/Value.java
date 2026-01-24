package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * A dynamic value which has a type and can be supplied either by Operations or block arguments.
 */
public abstract class Value implements Serializable {
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

  private final Type type;
  private final Kind kind;

  protected Value(Type type, Kind kind) {
    this.type = type;
    this.kind = kind;
  }

  public Type getType() {
    return type;
  }

  @JsonIgnore
  public Kind getKind() {
    return kind;
  }
}
