package blockly.vm.dgir.core;

import com.fasterxml.jackson.annotation.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * A dynamic value which has a type and can be supplied either by Operations or block arguments.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public final class Value extends IRObjectWithUseList<Value, ValueOperand> implements Serializable {
  private final Type type;

  public Value(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}
