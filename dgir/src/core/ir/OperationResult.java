package core.ir;

import com.fasterxml.jackson.annotation.*;

/**
 * The single result value produced by an {@link Operation}.
 * Wraps a {@link Value} and enforces type-consistency when the value is replaced.
 */
public class OperationResult {

  // =========================================================================
  // Members
  // =========================================================================

  @JsonIdentityReference(alwaysAsId = false)
  @JsonValue
  private Value value;

  @JsonIgnore
  private final Operation parent;

  // =========================================================================
  // Constructors
  // =========================================================================

  public OperationResult(Operation parent, Value value) {
    this.parent = parent;
    assert value != null : "Cannot set null value as result";
    this.value = value;
  }

  public OperationResult(Operation parent, Type type) {
    this.parent = parent;
    this.value = new Value(type);
  }

  // =========================================================================
  // Functions
  // =========================================================================

  public Value getValue() {
    return value;
  }

  public void setValue(Value value) {
    assert value != null : "Cannot set null value as result";
    assert value.getType().equals(this.value.getType())
      : "Type mismatch while setting result value: " + value.getType() + " != " + this.value.getType();
    this.value = value;
  }

  public Type getType() {
    return value.getType();
  }

  public Operation getParent() {
    return parent;
  }
}
