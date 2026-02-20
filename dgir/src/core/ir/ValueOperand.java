package core.ir;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Optional;

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
  public Optional<Type> getType() {
    return getValue().map(Value::getType);
  }
}
