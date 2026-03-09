package dgir.vm.api;

import dgir.core.ir.Value;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A scoped, allocation-efficient store that maps {@link Value} objects to their runtime objects.
 *
 * <h2>Design</h2>
 *
 * <p>The store is structured as a stack of <em>frames</em>. Each frame corresponds to a lexical
 * scope (region, function body, etc.). When a frame is pushed, new value bindings are recorded
 * inside it; when a frame is popped all bindings created in it are removed in O(n) time where n is
 * the number of values written in that frame — no iteration over the full map is needed.
 *
 * <h2>Data structures</h2>
 *
 * <ul>
 *   <li><b>{@link IdentityHashMap}</b> as the central key→value map. {@link Value} has no custom
 *       {@code hashCode}/{@code equals}, so identity comparison is both semantically correct and
 *       significantly faster than a general {@link HashMap} (uses {@link System#identityHashCode},
 *       which is cached in the object header).
 *   <li><b>Flat {@code Value[] frameKeys} array</b> — a write-log that records, in insertion order,
 *       which keys belong to which frame. The boundary between frames is tracked by a parallel
 *       {@code int[] frameStart} array. Popping a frame is a simple forward scan from {@code
 *       frameStart[f]} to {@code writeHead}, removing each key from the map, then resetting the
 *       write-head. No {@link java.util.HashSet} or {@link java.util.Iterator} objects are
 *       allocated per frame.
 *   <li><b>Parallel primitive arrays</b> ({@code int[] frameStart}, {@code boolean[]
 *       frameIsolated}) for frame metadata — zero heap allocation per push/pop.
 * </ul>
 *
 * <h2>Isolation</h2>
 *
 * <p>A frame can be marked <em>isolated</em> from the frame above it. This flag is used by {@link
 * #getVisibleValues()} to stop walking up the frame chain at an isolation boundary (e.g. when
 * crossing a function-call boundary where the caller's locals must not be visible).
 */
public class Stack {

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
  // Frame descriptor stack
  // =========================================================================

  /**
   * {@code frameStart[i]} is the index into {@link #frameKeys} where frame {@code i} begins. Grows
   * geometrically when full.
   */
  private int[] frameStart = new int[16];

  /**
   * {@code frameIsolated[i]} is the {@code isIsolatedFromAbove} flag for frame {@code i}. When
   * {@code true}, the frame above is not visible in {@link #getVisibleValues()}.
   */
  private boolean[] frameIsolated = new boolean[16];

  /** Number of currently live frames. */
  private int frameTop = 0;

  // =========================================================================
  // Write-log
  // =========================================================================

  /**
   * Flat array recording, in insertion order, which {@link Value} keys were first written in the
   * current or any prior frame. The slice {@code frameKeys[frameStart[f]..writeHead)} belongs to
   * frame {@code f} (the innermost frame). On pop, every key in that slice is removed from {@link
   * #values} and the write-head is reset to {@code frameStart[f]}.
   */
  private Value[] frameKeys = new Value[64];

  /** Next free slot in {@link #frameKeys}. */
  private int writeHead = 0;

  // =========================================================================
  // Frame management
  // =========================================================================

  /**
   * Opens a new scope frame.
   *
   * @param isIsolatedFromAbove when {@code true} the new frame is treated as opaque to its caller;
   *     values in the frame above will not appear in {@link #getVisibleValues()}.
   */
  public void pushFrame(boolean isIsolatedFromAbove) {
    if (frameTop == frameStart.length) {
      int newCap = frameStart.length * 2;
      frameStart = Arrays.copyOf(frameStart, newCap);
      frameIsolated = Arrays.copyOf(frameIsolated, newCap);
    }
    frameStart[frameTop] = writeHead;
    frameIsolated[frameTop] = isIsolatedFromAbove;
    frameTop++;
  }

  /**
   * Closes the innermost scope frame and removes all value bindings created inside it.
   *
   * @return the {@code isIsolatedFromAbove} flag of the popped frame, or {@link Optional#empty()}
   *     if the frame stack was already empty.
   */
  public Optional<Boolean> popFrame() {
    if (frameTop == 0) return Optional.empty();
    frameTop--;
    boolean isolated = frameIsolated[frameTop];
    int start = frameStart[frameTop];
    for (int i = start; i < writeHead; i++) {
      values.remove(frameKeys[i]);
      frameKeys[i] = null; // release reference for GC
    }
    writeHead = start;
    return Optional.of(isolated);
  }

  /** Returns the number of currently live frames. */
  public int frameDepth() {
    return frameTop;
  }

  // =========================================================================
  // Value access
  // =========================================================================

  /**
   * Binds {@code object} to {@code value} in the current frame.
   *
   * <p>If {@code value} was already bound in any live frame the binding is updated in-place; the
   * write-log is <em>not</em> updated again so the key is only removed once when the frame that
   * originally introduced it is popped.
   *
   * @param value the value to bind.
   * @param object the runtime object to associate with {@code value}.
   * @throws AssertionError if no frame is open.
   */
  public void set(@NotNull Value value, @NotNull Object object) {
    Object previous = values.put(value, object);
    if (previous == null) {
      // First write — record in the write-log so we know to remove it on pop.
      assert frameTop > 0 : "Cannot set a value outside of a stack frame.";
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
   * @throws IllegalStateException if {@code value} is not currently bound.
   */
  public @NotNull Object get(@NotNull Value value) {
    Object result = values.get(value);
    if (result == null) {
      throw new IllegalStateException("Value " + value + " is not defined in the current scope.");
    }
    return result;
  }

  // =========================================================================
  // Reset
  // =========================================================================

  /**
   * Clears all bindings and resets the frame stack. Called after an abort or before re-running a
   * program.
   */
  public void reset() {
    values.clear();
    Arrays.fill(frameKeys, 0, writeHead, null);
    writeHead = 0;
    frameTop = 0;
  }

  // =========================================================================
  // Debug / inspection
  // =========================================================================

  /**
   * Returns all value bindings that are visible in the current scope as an unmodifiable,
   * insertion-ordered map. Walks the frame chain from innermost to outermost; stops at the first
   * isolated frame boundary.
   *
   * <p>Intended for DAP {@code VariablesResponse} population and is safe to call from any thread
   * while the VM is paused.
   *
   * @return a snapshot of visible value bindings, innermost scope first.
   */
  public @NotNull Map<Value, Object> getVisibleValues() {
    Map<Value, Object> result = new LinkedHashMap<>();
    for (int f = frameTop - 1; f >= 0; f--) {
      int start = frameStart[f];
      int end = (f + 1 < frameTop) ? frameStart[f + 1] : writeHead;
      for (int i = start; i < end; i++) {
        Value v = frameKeys[i];
        if (v != null) result.putIfAbsent(v, values.get(v));
      }
      if (frameIsolated[f]) break;
    }
    return Collections.unmodifiableMap(result);
  }
}
