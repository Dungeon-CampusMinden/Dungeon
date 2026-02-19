package core.ir;

import core.IRObjectWithUseList;
import com.fasterxml.jackson.annotation.*;

import java.io.Serializable;

/**
 * A dynamic value produced by an {@link Operation} or introduced as a block/region argument.
 * Values carry a {@link Type} and maintain a use-list of all {@link ValueOperand}s that reference them.
 */
@JsonIdentityInfo(generator = ObjectIdGenerators.UUIDGenerator.class, property = "@id")
public final class Value extends IRObjectWithUseList<Value, ValueOperand> implements Serializable {

  // =========================================================================
  // Members
  // =========================================================================

  private final Type type;

  // =========================================================================
  // Constructors
  // =========================================================================

  @JsonCreator
  public Value(@JsonProperty("type") Type type) {
    this.type = type;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  public Type getType() {
    return type;
  }
}
