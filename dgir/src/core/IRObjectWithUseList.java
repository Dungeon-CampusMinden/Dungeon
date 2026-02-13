package core;

import core.ir.Operand;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashSet;
import java.util.Set;

public class IRObjectWithUseList<
  // The value type that extends this class
  DerivedValueT extends IRObjectWithUseList<DerivedValueT, OperandT>,
  // The operand type that references the value
  OperandT extends Operand<DerivedValueT, OperandT>
  > {

  /**
   * The operands that use this value.
   */
  private final Set<OperandT> uses = new HashSet<>();

  public IRObjectWithUseList() {
  }

  /**
   * Check if this value has any uses.
   *
   * @return True if this value has any uses, false otherwise.
   */
  public boolean hasUse() {
    return !uses.isEmpty();
  }

  /**
   * Check if this value has exactly one use.
   *
   * @return True if this value has exactly one use, false otherwise.
   */
  public boolean hasOneUse() {
    return uses.size() == 1;
  }

  /**
   * Replace all uses of this value with the given new value.
   *
   * @param newValue The new value to replace this value with.
   */
  public void replaceAllUsesWith(DerivedValueT newValue) {
    assert newValue != this : "Cannot replace all uses with self.";
    // Move all uses to new value
    newValue.getUses().addAll(uses);
    // We need to iterate over the use list like this since setValue modifies the use list while iterating over it
    // This is because setValue removes the operand from the old value's use list and adds it to the new value's use list
    while (!uses.isEmpty())
      uses.stream().findFirst().ifPresent(operandT -> operandT.setValue(newValue));
  }

  /**
   * Get the set of operands that use this value.
   *
   * @return The set of operands that use this value.
   */
  @JsonIgnore
  public Set<OperandT> getUses() {
    return uses;
  }
}

