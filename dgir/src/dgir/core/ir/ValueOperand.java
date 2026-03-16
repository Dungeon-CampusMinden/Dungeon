package dgir.core.ir;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/** A reference to a dynamic {@link Value} used as an input to an {@link Operation}. */
public final class ValueOperand extends Operand<Value, ValueOperand> {

  // =========================================================================
  // Constructors
  // =========================================================================

  public ValueOperand(@NotNull Operation owner, @NotNull Value value) {
    super(owner, value);
  }

  @Override
  public int getIndex() {
    return getOwner().getOperands().indexOf(this);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  @JsonIgnore
  @Contract(pure = true)
  public @NotNull Optional<Type> getType() {
    return getValue().map(Value::getType);
  }
}
