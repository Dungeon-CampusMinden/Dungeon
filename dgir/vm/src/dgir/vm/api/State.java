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
   * @param isIsolatedFromAbove Whether this stack frame is isolated from the above stack frame. If
   *     true, then values defined in the above stack frame are not accessible in this stack frame.
   */
  public void pushStackFrame(boolean isIsolatedFromAbove) {
    stackFrames.push(Pair.of(new HashSet<>(), isIsolatedFromAbove));
  }

  /**
   * Check if a value if visible in the current stack frame. Stops at isolated from above frames.
   *
   * @param value The value to check.
   * @return True if the value is visible in the current stack frame, false otherwise.
   */
  public boolean isValueVisible(@NotNull Value value) {
    for (Pair<Set<Value>, Boolean> frame : stackFrames) {
      Set<Value> definedValues = frame.getLeft();
      boolean isIsolatedFromAbove = frame.getRight();
      if (definedValues != null && definedValues.contains(value)) {
        return true;
      }
      if (isIsolatedFromAbove) {
        break;
      }
    }
    return false;
  }

  /** Closes the current stack frame and removes all values defined in it from the state. */
  public void popStackFrame() {
    // Remove all values defined in the current stack frame from the state if they are not defined
    // in any other stack frame.
    var frame = stackFrames.pop();
    if (frame == null) return;
    var definedValues = frame.getLeft();
    // Make sure to not remove values defined in other stack frames as they are still accessible
    // there.
    for (Value value : definedValues) {
      // Since we popped the frame, we can just check if the value is still defined in the state.
      if (!isValueVisible(value)) {
        values.remove(value);
      }
    }
  }

  /**
   * Gets the object associated with the given value.
   *
   * @param value The value to get the object for.
   * @return The object associated with the given value or null.
   * @throws IllegalStateException If the value is not defined in the current stack frame.
   */
  public @NotNull Object getValue(@NotNull Value value) {
    if (!isValueVisible(value)) {
      throw new IllegalStateException(
          "Value " + value + " is not defined in the current stack frame.");
    }
    return values.get(value);
  }

  /**
   * Gets the object associated with the given operand.
   *
   * @param operand The operand to get the object for.
   * @return The object associated with the operand.
   * @throws IllegalStateException If the operand does not reference a value.
   */
  public @NotNull Object getValue(@NotNull ValueOperand operand) {
    return getValue(
        operand.getValue().orElseThrow(() -> new AssertionError("Operand value must be present")));
  }

  /**
   * Gets the object associated with the given value and casts it to the given class. Returns an
   * empty optional if the object is not an instance of the given class.
   *
   * @param value The value to get the object for.
   * @param clazz The class to cast the object to.
   * @param <T> The type of the class to cast the object to.
   * @return The object associated with the given value cast to the given class, or an empty
   *     optional if the object is not an instance of the given class.
   * @throws IllegalStateException If the value is not defined in the current stack frame.
   */
  public <T> @NotNull Optional<T> getValue(@NotNull Value value, @NotNull Class<T> clazz) {
    var obj = getValue(value);
    if (clazz.isInstance(obj)) return Optional.of(clazz.cast(obj));

    return Optional.empty();
  }

  /**
   * Gets the object associated with the given value and casts it to the given class. Returns an
   * empty optional if the object is not an instance of the given class.
   *
   * @param operand The operand to get the object for.
   * @param clazz The class to cast the object to.
   * @param <T> The type of the class to cast the object to.
   * @return The object associated with the given value cast to the given class, or an empty
   *     optional if the object is not an instance of the given class.
   * @throws IllegalStateException If the value is not defined in the current stack frame.
   */
  public <T> @NotNull Optional<T> getValue(@NotNull ValueOperand operand, @NotNull Class<T> clazz) {
    return getValue(operand.getValue().orElseThrow(), clazz);
  }

  /**
   * Sets the object associated with the given value.
   *
   * @param value The value to set the object for.
   * @param object The object to associate with the given value.
   */
  public void setValue(@NotNull Value value, @NotNull Object object) {
    values.put(value, object);
    // Add the value to the set of defined values in the current stack frame.
    var frame = stackFrames.peek();
    assert frame != null : "Cannot set a value outside of a stack frame.";
    // Add the value to the set of defined values in the current stack frame and the global values
    // map.
    frame.getLeft().add(value);
    values.put(value, object);
  }

  /**
   * Sets the value associated with the output of the given operation to the given object.
   *
   * @param operation The operation whose output value should be set.
   * @param object The object to associate with the output value.
   */
  public void setValueForOutput(@NotNull Operation operation, @NotNull Object object) {
    setValue(operation.getOutputValue().orElseThrow(), object);
  }

  /** Resets execution state after an abort. */
  public void reset() {
    values.clear();
    stackFrames.clear();
    instructionCount = 0;
  }

  /**
   * Returns all values that are visible in the current scope as an unmodifiable map from
   * {@link Value} to its bound object.  Values in isolated parent frames are excluded.
   *
   * <p>Intended for DAP {@code VariablesResponse} population; may be called from any thread
   * while the VM is paused.
   *
   * @return a snapshot map of visible value bindings, innermost scope first.
   */
  public @NotNull Map<Value, Object> getVisibleValues() {
    Map<Value, Object> result = new LinkedHashMap<>();
    for (Pair<Set<Value>, Boolean> frame : stackFrames) {
      Set<Value> defined = frame.getLeft();
      boolean isolated = frame.getRight();
      if (defined != null) {
        for (Value v : defined) {
          // Only add if not already shadowed by a deeper frame entry.
          result.putIfAbsent(v, values.get(v));
        }
      }
      if (isolated) break;
    }
    return Collections.unmodifiableMap(result);
  }
}
