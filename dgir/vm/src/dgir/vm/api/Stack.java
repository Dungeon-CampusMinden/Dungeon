package dgir.vm.api;

import dgir.core.ir.Operation;
import dgir.core.ir.Value;
import dgir.core.ir.ValueOperand;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A scoped, allocation-efficient store that maps {@link Value} objects to their runtime objects.
 *
 * <h2>Design</h2>
 *
 * <p>The store models two nested runtime concepts:
 *
 * <ol>
 *   <li><b>Call frames</b> represent traditional function-call stack frames.
 *   <li><b>Scopes</b> live inside call frames and represent lexical regions opened by operations.
 * </ol>
 *
 * <p>Each scope records the {@link Operation} that opened it so higher layers can associate scope
 * teardown with the operation that may receive an output value. Popping a call frame closes all of
 * its scopes in innermost-to-outermost order.
 *
 * <h2>Data structures</h2>
 *
 * <ul>
 *   <li><b>{@link IdentityHashMap}</b> as the central key→value map. {@link Value} has no custom
 *       {@code hashCode}/{@code equals}, so identity comparison is both semantically correct and
 *       significantly faster than a general {@link HashMap}.
 *   <li><b>Flat {@code Value[] frameKeys} array</b> — a write-log that records, in insertion order,
 *       which keys were first written while a scope was live. Each scope stores the write-log start
 *       index that belongs to it, so closing a scope is a forward scan followed by a write-head
 *       rewind.
 *   <li><b>Parallel primitive/object arrays</b> for call-frame and scope metadata — zero heap
 *       allocation per push/pop in the common case.
 * </ul>
 */
public class Stack {

  public record ClosedScope(@NotNull Operation opener, boolean isIsolatedFromAbove) {}

  public record ClosedCallFrame(@NotNull List<@NotNull ClosedScope> closedScopes) {
    public ClosedCallFrame {
      closedScopes = List.copyOf(closedScopes);
    }
  }

  // =========================================================================
  // Central value map
  // =========================================================================

  /**
   * Maps each live {@link Value} to its current runtime object. Uses {@link IdentityHashMap}
   * because {@link Value} has no custom {@code hashCode}/{@code equals} — identity comparison is
   * both correct and faster than a general {@link HashMap}.
   */
  private final @NotNull IdentityHashMap<Value, Object> values = new IdentityHashMap<>();

  // =========================================================================
  // Call-frame descriptor stack
  // =========================================================================

  /** {@code callFrameScopeStart[i]} is the scope depth at which call frame {@code i} begins. */
  private int[] callFrameScopeStart = new int[16];

  /** Number of currently live call frames. */
  private int callFrameTop = 0;

  // =========================================================================
  // Scope descriptor stack
  // =========================================================================

  /** {@code scopeWriteStart[i]} is the write-log start index for scope {@code i}. */
  private int[] scopeWriteStart = new int[16];

  /** Isolation flag for each live scope. */
  private boolean[] scopeIsolated = new boolean[16];

  /** Operation that opened each live scope. */
  private Operation[] scopeOpeners = new Operation[16];

  /** Number of currently live scopes. */
  private int scopeTop = 0;

  // =========================================================================
  // Write-log
  // =========================================================================

  /**
   * Flat array recording, in insertion order, which {@link Value} keys were first written while a
   * live scope was active. The slice {@code frameKeys[scopeWriteStart[s]..writeHead)} belongs to
   * scope {@code s} (the innermost scope when popping that scope).
   */
  private Value[] frameKeys = new Value[64];

  /** Next free slot in {@link #frameKeys}. */
  private int writeHead = 0;

  // =========================================================================
  // Call-frame management
  // =========================================================================

  /** Opens a new call frame. */
  public void pushCallFrame() {
    if (callFrameTop == callFrameScopeStart.length) {
      callFrameScopeStart = Arrays.copyOf(callFrameScopeStart, callFrameScopeStart.length * 2);
    }
    callFrameScopeStart[callFrameTop++] = scopeTop;
  }

  /**
   * Closes the innermost call frame and all scopes contained in it.
   *
   * @return metadata for the scopes closed by the frame pop, innermost scope first.
   */
  public @NotNull Optional<ClosedCallFrame> popCallFrame() {
    if (callFrameTop == 0) return Optional.empty();

    int scopeStart = callFrameScopeStart[callFrameTop - 1];
    List<ClosedScope> closedScopes = new ArrayList<>(scopeTop - scopeStart);
    while (scopeTop > scopeStart) {
      closedScopes.add(popScope().orElseThrow());
    }
    callFrameTop--;
    return Optional.of(new ClosedCallFrame(closedScopes));
  }

  /** Returns the number of currently live call frames. */
  public int frameDepth() {
    return callFrameTop;
  }

  // =========================================================================
  // Scope management
  // =========================================================================

  /**
   * Opens a new scope inside the current call frame.
   *
   * @param opener the operation that opened the scope.
   * @param isIsolatedFromAbove whether lookups should stop at this scope boundary when walking
   *     outward via {@link #getVisibleValues()}.
   */
  public void pushScope(@NotNull Operation opener, boolean isIsolatedFromAbove) {
    assert callFrameTop > 0 : "Cannot open a scope outside of a call frame.";
    if (scopeTop == scopeWriteStart.length) {
      int newCap = scopeWriteStart.length * 2;
      scopeWriteStart = Arrays.copyOf(scopeWriteStart, newCap);
      scopeIsolated = Arrays.copyOf(scopeIsolated, newCap);
      scopeOpeners = Arrays.copyOf(scopeOpeners, newCap);
    }
    scopeWriteStart[scopeTop] = writeHead;
    scopeIsolated[scopeTop] = isIsolatedFromAbove;
    scopeOpeners[scopeTop] = opener;
    scopeTop++;
  }

  /**
   * Closes the innermost scope and removes all value bindings created inside it.
   *
   * @return metadata for the closed scope, or {@link Optional#empty()} if no scope is open.
   */
  public @NotNull Optional<ClosedScope> popScope() {
    if (scopeTop == 0) return Optional.empty();
    scopeTop--;

    boolean isolated = scopeIsolated[scopeTop];
    Operation opener = scopeOpeners[scopeTop];
    int start = scopeWriteStart[scopeTop];
    for (int i = start; i < writeHead; i++) {
      values.remove(frameKeys[i]);
      frameKeys[i] = null; // release reference for GC
    }
    writeHead = start;
    scopeOpeners[scopeTop] = null;
    return Optional.of(new ClosedScope(opener, isolated));
  }

  /** Returns the number of currently live scopes. */
  public int scopeDepth() {
    return scopeTop;
  }

  // =========================================================================
  // Value access
  // =========================================================================

  /**
   * Binds {@code object} to {@code value} in the current scope.
   *
   * <p>If {@code value} was already bound in any live scope the binding is updated in-place; the
   * write-log is <em>not</em> updated again so the key is only removed once when the scope that
   * originally introduced it is popped.
   *
   * @param value the value to bind.
   * @param object the runtime object to associate with {@code value}.
   * @throws AssertionError if no scope is open.
   */
  public void set(@NotNull Value value, @NotNull Object object) {
    Object previous = values.put(value, object);
    if (previous == null) {
      assert scopeTop > 0 : "Cannot set a value outside of an open scope.";
      if (writeHead == frameKeys.length) {
        frameKeys = Arrays.copyOf(frameKeys, frameKeys.length * 2);
      }
      frameKeys[writeHead++] = value;
    }
  }

  /**
   * Returns the runtime object bound to {@code value}.
   *
   * @param value the value to look up.
   * @return the bound object.
   */
  public @NotNull Object getOrThrow(@NotNull Value value) {
    Object result = values.get(value);
    if (result == null) {
      throw new IllegalStateException("Value " + value + " is not defined in the current scope.");
    }
    return result;
  }

  /**
   * Returns the runtime object bound to the value referenced by {@code operand}.
   *
   * @param operand the operand to look up.
   * @return the bound object.
   */
  public @NotNull Object getOrThrow(@NotNull ValueOperand operand) {
    return getOrThrow(operand.getValueOrThrow());
  }

  /**
   * Returns the runtime object bound to {@code value} as an instance of {@code clazz}.
   *
   * @param value the value to look up.
   * @param clazz the expected runtime type of the bound object.
   * @return the bound object cast to {@code clazz}.
   * @param <T> the expected type.
   */
  public @NotNull <T> T getAsOrThrow(@NotNull Value value, @NotNull Class<T> clazz) {
    return clazz.cast(getOrThrow(value));
  }

  /**
   * Returns the runtime object bound to the value referenced by {@code operand} as an instance of
   * {@code clazz}.
   *
   * @param operand the operand to look up; must reference a value.
   * @param clazz the expected runtime type of the bound object.
   * @return the bound object cast to {@code clazz}.
   * @param <T> the expected type.
   */
  public @NotNull <T> T getAsOrThrow(@NotNull ValueOperand operand, @NotNull Class<T> clazz) {
    return clazz.cast(getOrThrow(operand));
  }

  // =========================================================================
  // Reset
  // =========================================================================

  /** Clears all bindings and resets the call-frame and scope stacks. */
  public void reset() {
    values.clear();
    Arrays.fill(frameKeys, 0, writeHead, null);
    Arrays.fill(scopeOpeners, 0, scopeTop, null);
    writeHead = 0;
    scopeTop = 0;
    callFrameTop = 0;
  }

  // =========================================================================
  // Debug / inspection
  // =========================================================================

  /**
   * Returns all value bindings that are visible in the current scope as an unmodifiable,
   * insertion-ordered map. Walks the scope chain from innermost to outermost and stops at the first
   * isolated scope boundary.
   *
   * <p>Intended for DAP {@code VariablesResponse} population and is safe to call from any thread
   * while the VM is paused.
   *
   * @return a snapshot of visible value bindings, innermost scope first.
   */
  public @NotNull Map<Value, Object> getVisibleValues() {
    Map<Value, Object> result = new LinkedHashMap<>();
    for (int s = scopeTop - 1; s >= 0; s--) {
      int start = scopeWriteStart[s];
      int end = (s + 1 < scopeTop) ? scopeWriteStart[s + 1] : writeHead;
      for (int i = start; i < end; i++) {
        Value v = frameKeys[i];
        if (v != null) result.putIfAbsent(v, values.get(v));
      }
      if (scopeIsolated[s]) break;
    }
    return Collections.unmodifiableMap(result);
  }
}
