package dgir.vm.api;

import dgir.core.ir.Operation;
import dgir.core.ir.Value;
import dgir.core.ir.ValueOperand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;

public class State {

  // =========================================================================
  // Value store
  // =========================================================================

  /** Scoped, allocation-efficient store for value bindings. See {@link Stack}. */
  private final @NotNull Stack stack = new Stack();

  public long instructionCount = 0;

  // =========================================================================
  // Call stack
  // =========================================================================

  public final @NotNull Deque<Operation> callStack = new ArrayDeque<>();

  // =========================================================================
  // Frame management
  // =========================================================================

  /**
   * Opens a new stack frame.
   *
   * @param isIsolatedFromAbove Whether this stack frame is isolated from the frame above. If {@code
   *     true}, values in the enclosing frame are not accessible via {@link #getVisibleValues()}.
   */
  public void pushStackFrame(boolean isIsolatedFromAbove) {
    stack.pushFrame(isIsolatedFromAbove);
  }

  /**
   * Closes the current stack frame and removes all values defined in it from the store.
   *
   * @return the {@code isIsolatedFromAbove} flag of the popped frame, or {@link Optional#empty()}
   *     if the frame stack was already empty.
   */
  public Optional<Boolean> popStackFrame() {
    return stack.popFrame();
  }

  // =========================================================================
  // Call stack
  // =========================================================================

  public void pushCallStack(Operation op) {
    callStack.push(op);
  }

  public Optional<Operation> popCallStack() {
    return Optional.ofNullable(callStack.pop());
  }

  public Optional<Operation> peekCallStack() {
    return Optional.ofNullable(callStack.peek());
  }

  public @NotNull @Unmodifiable SequencedCollection<@NotNull Operation> getCallStack() {
    return Collections.unmodifiableSequencedCollection(callStack);
  }

  // =========================================================================
  // Value access — Optional variants (for nullable / conditional use)
  // =========================================================================

  /**
   * Gets the object associated with the given value and casts it to the given class. Returns an
   * empty optional if the object is not an instance of the given class.
   *
   * @param value The value to get the object for.
   * @param clazz The class to cast the object to.
   * @param <T> The type to cast to.
   * @return The typed value, or empty if the wrong type.
   * @throws IllegalStateException If the value is not defined in the current scope.
   */
  public <T> @NotNull Optional<T> getValue(@NotNull Value value, @NotNull Class<T> clazz) {
    var obj = stack.getOrThrow(value);
    if (clazz.isInstance(obj)) return Optional.of(clazz.cast(obj));
    return Optional.empty();
  }

  /**
   * Gets the object associated with the operand's value and casts it to the given class. Returns an
   * empty optional if the object is not an instance of the given class.
   *
   * @param operand The operand to get the object for.
   * @param clazz The class to cast the object to.
   * @param <T> The type to cast to.
   * @return The typed value, or empty if the wrong type.
   * @throws IllegalStateException If the value is not defined in the current scope.
   */
  public <T> @NotNull Optional<T> getValue(@NotNull ValueOperand operand, @NotNull Class<T> clazz) {
    return getValue(operand.getValueOrThrow(), clazz);
  }

  // =========================================================================
  // Value access — unchecked variants (no Optional allocation)
  // =========================================================================

  /**
   * Gets the object associated with the given value.
   *
   * @param value The value to get the object for.
   * @return The bound object, never {@code null}.
   * @throws IllegalStateException If the value is not defined in the current scope.
   */
  public @NotNull Object getValueOrThrow(@NotNull Value value) {
    return stack.getOrThrow(value);
  }

  /**
   * Gets the object associated with the given operand's value.
   *
   * @param operand The operand to get the object for.
   * @return The bound object, never {@code null}.
   * @throws AssertionError If the operand does not reference a value.
   * @throws IllegalStateException If the value is not defined in the current scope.
   */
  public @NotNull Object getValueOrThrow(@NotNull ValueOperand operand) {
    return stack.getOrThrow(operand);
  }

  /**
   * Gets the object associated with the given value, cast to {@code clazz}.
   *
   * @param value The value to get the object for.
   * @param clazz The expected runtime type.
   * @param <T> The expected type.
   * @return The bound object cast to {@code T}, never {@code null}.
   * @throws IllegalStateException If the value is not defined in the current scope.
   * @throws ClassCastException If the bound object is not an instance of {@code clazz}.
   */
  public <T> @NotNull T getValueAsOrThrow(@NotNull Value value, @NotNull Class<T> clazz) {
    return stack.getAsOrThrow(value, clazz);
  }

  /**
   * Gets the object associated with the given operand's value, cast to {@code clazz}.
   *
   * @param operand The operand to get the object for.
   * @param clazz The expected runtime type.
   * @param <T> The expected type.
   * @return The bound object cast to {@code T}, never {@code null}.
   * @throws AssertionError If the operand does not reference a value.
   * @throws IllegalStateException If the value is not defined in the current scope.
   * @throws ClassCastException If the bound object is not an instance of {@code clazz}.
   */
  public <T> @NotNull T getValueAsOrThrow(@NotNull ValueOperand operand, @NotNull Class<T> clazz) {
    return stack.getAsOrThrow(operand, clazz);
  }

  // =========================================================================
  // Value mutation
  // =========================================================================

  /**
   * Sets the object associated with the given value.
   *
   * @param value The value to bind.
   * @param object The runtime object to associate with the given value.
   */
  public void setValue(@NotNull Value value, @NotNull Object object) {
    if (!value.getType().validate(object)) {
      throw new IllegalArgumentException("Object " + object + " is not valid for value " + value);
    }
    stack.set(value, object);
  }

  /**
   * Sets the value associated with the output of the given operation to the given object.
   *
   * @param operation The operation whose output value should be set.
   * @param object The object to associate with the output value.
   * @throws NoSuchElementException If the operation has no output value.
   */
  public void setValueForOutput(@NotNull Operation operation, @NotNull Object object) {
    if (!operation.getOutputValueOrThrow().getType().validate(object)) {
      throw new IllegalArgumentException(
          "Object " + object + " is not valid for value " + operation.getOutputValueOrThrow());
    }
    stack.set(operation.getOutputValueOrThrow(), object);
  }

  // =========================================================================
  // Reset
  // =========================================================================

  /** Resets execution state after an abort or before re-running a program. */
  public void reset() {
    stack.reset();
    callStack.clear();
    instructionCount = 0;
  }

  // =========================================================================
  // Debug helpers
  // =========================================================================

  /**
   * Returns all values that are visible in the current scope as an unmodifiable map from {@link
   * Value} to its bound object. Values in isolated parent frames are excluded.
   *
   * <p>Intended for DAP {@code VariablesResponse} population; may be called from any thread while
   * the VM is paused.
   *
   * @return a snapshot map of visible value bindings, innermost scope first.
   */
  public @NotNull Map<Value, Object> getVisibleValues() {
    return stack.getVisibleValues();
  }
}
