package core;

import core.ir.Operand;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for any IR object that maintains a use-list of {@link Operand}s referencing it.
 * <p>
 * Concrete examples are {@link core.ir.Value} (referenced by {@link core.ir.ValueOperand}s)
 * and {@link core.ir.Block} (referenced by {@link core.ir.BlockOperand}s).
 *
 * @param <DerivedValueT> The concrete subclass extending this class (e.g. {@code Value}).
 * @param <OperandT>      The operand type that references {@code DerivedValueT}.
 */
public class IRObjectWithUseList<
  DerivedValueT extends IRObjectWithUseList<DerivedValueT, OperandT>,
  OperandT extends Operand<DerivedValueT, OperandT>
  > {

  // =========================================================================
  // Members
  // =========================================================================

  /**
   * All operands currently referencing this object.
   */
  private final Set<OperandT> uses = new HashSet<>();

  // =========================================================================
  // Constructors
  // =========================================================================

  public IRObjectWithUseList() {
  }

  // =========================================================================
  // Use-list
  // =========================================================================

  /**
   * Get the set of operands that reference this object.
   *
   * @return The live use-set.
   */
  @JsonIgnore
  public Set<OperandT> getUses() {
    return uses;
  }

  /**
   * Return {@code true} if at least one operand references this object.
   */
  public boolean hasUse() {
    return !uses.isEmpty();
  }

  /**
   * Return {@code true} if exactly one operand references this object.
   */
  public boolean hasOneUse() {
    return uses.size() == 1;
  }

  /**
   * Redirect every operand currently referencing this object to reference {@code newValue}
   * instead, and update the use-lists of both objects accordingly.
   *
   * @param newValue The replacement value. Must not be {@code this}.
   */
  public void replaceAllUsesWith(DerivedValueT newValue) {
    assert newValue != this : "Cannot replace all uses with self.";
    // Pre-add all uses to the new value's use-set so they are visible immediately
    newValue.getUses().addAll(uses);
    // Drain our own use-set via setValue, which handles the use-list bookkeeping per operand
    while (!uses.isEmpty())
      uses.stream().findFirst().ifPresent(operandT -> operandT.setValue(newValue));
  }
}
