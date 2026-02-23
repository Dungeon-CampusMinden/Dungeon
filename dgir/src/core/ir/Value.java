package core.ir;

import com.fasterxml.jackson.annotation.*;
import core.IRObjectWithUseList;
import java.io.Serializable;

import core.serialization.ValueIdGenerator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A dynamic value produced by an {@link Operation} or introduced as a block/region argument. Values
 * carry a {@link Type} and maintain a use-list of all {@link ValueOperand}s that reference them.
 */
@JsonIdentityInfo(generator = ValueIdGenerator.class)
public final class Value extends IRObjectWithUseList<Value, ValueOperand> implements Serializable {

  // =========================================================================
  // Members
  // =========================================================================

  private final @NotNull Type type;

  // =========================================================================
  // Constructors
  // =========================================================================

  @JsonCreator
  public Value(@JsonProperty("type") @NotNull Type type) {
    this.type = type;
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @Contract(pure = true)
  public @NotNull Type getType() {
    return type;
  }

  // =========================================================================
  // Object
  // =========================================================================

  @Override
  public String toString() {
    return "Value{" + "type=" + type + '}';
  }
}
