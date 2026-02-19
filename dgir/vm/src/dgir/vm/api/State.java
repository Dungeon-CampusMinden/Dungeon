package dgir.vm.api;

import core.ir.Operation;
import core.ir.Value;
import core.ir.ValueOperand;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class State {
  private final @NotNull Map<Value, Object> values = new HashMap<>();
  public long instructionCount = 0;

  public final @NotNull Deque<Pair<Set<Value>, Boolean>> stackFrames = new ArrayDeque<>();

  /**
   * Opens a new stack frame.
   *
   * @param isIsolatedFromAbove Whether this stack frame is isolated from the above stack frame.
   *                            If true, then values defined in the above stack frame are not accessible in this stack frame.
   */
  public void pushStackFrame(boolean isIsolatedFromAbove) {
    stackFrames.push(Pair.of(new HashSet<>(), isIsolatedFromAbove));
  }

  /**
   * Closes the current stack frame and removes all values defined in it from the state.
   */
  public void popStackFrame() {
    // Remove all values defined in the current stack frame from the state if they are not defined in any other stack frame.
    var frame = stackFrames.pop();
    if (frame == null)
      return;
    var definedValues = frame.getLeft();
    // Make sure to not remove values defined in other stack frames as they are still accessible there.
    for (Value value : definedValues) {
      // Since we popped the frame, we can just check if the value is still defined in the state.
      if (getValue(value).isEmpty()) {
        values.remove(value);
      }
    }
  }

  /**
   * Gets the object associated with the given value.
   *
   * @param value The value to get the object for.
   * @return The object associated with the given value or null.
   */
  public @NotNull Optional<Object> getValue(@NotNull Value value) {
    // Go over all stack frames in reverse order and check if the value is defined in any of the visible stack frames.
    // If it is, return the associated object.
    for (Pair<Set<Value>, Boolean> frame : stackFrames) {
      // The values defined in the current stack frame
      Set<Value> definedValues = frame.getLeft();
      // Whether the current stack frame is isolated from the above stack frame
      boolean isIsolatedFromAbove = frame.getRight();
      // Check if the value is defined in the current stack frame and return the associated object if it is.
      if (definedValues != null && definedValues.contains(value)) {
        return Optional.of(values.get(value));
      }
      if (isIsolatedFromAbove) {
        break;
      }
    }
    // If the value is not defined in any of the visible stack frames, return null.
    return Optional.empty();
  }

  public @NotNull Optional<Object> getValue(@NotNull ValueOperand operand) {
    return getValue(operand.getValue());
  }

  public <T> @NotNull Optional<T> getValue(@NotNull Value value, @NotNull Class<T> clazz) {
    var obj = getValue(value);
    if (obj.isEmpty())
      return Optional.empty();

    if (clazz.isInstance(obj.get()))
      return Optional.of(clazz.cast(obj.get()));

    return Optional.empty();
  }

  /**
   * Sets the object associated with the given value.
   *
   * @param value  The value to set the object for.
   * @param object The object to associate with the given value.
   */
  public void setValue(@NotNull Value value, @NotNull Object object) {
    values.put(value, object);
    // Add the value to the set of defined values in the current stack frame.
    var frame = stackFrames.peek();
    assert frame != null : "Cannot set a value outside of a stack frame.";
    // Add the value to the set of defined values in the current stack frame and the global values map.
    frame.getLeft().add(value);
    values.put(value, object);
  }

  /**
   * Sets the value associated with the output of the given operation to the given object.
   * @param operation The operation whose output value should be set.
   * @param object The object to associate with the output value.
   */
  public void setValueForOutput(@NotNull Operation operation, @NotNull Object object) {
    setValue(operation.getOutputValue().orElseThrow(), object);
  }

  /**
   * Resets execution state after an abort.
   */
  public void reset() {
    values.clear();
    stackFrames.clear();
    instructionCount = 0;
  }
}
