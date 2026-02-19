package core.ir;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A reference to a dynamic {@link Value} used as an input to an {@link Operation}.
 */
public final class ValueOperand extends Operand<Value, ValueOperand> {

  // =========================================================================
  // Constructors
  // =========================================================================

  public ValueOperand(Operation owner, Value value) {
    super(owner, value);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @JsonIgnore
  public Type getType() {
    assert getValue() != null : "ValueOperand has no value assigned.";
    return getValue().getType();
  }
}
