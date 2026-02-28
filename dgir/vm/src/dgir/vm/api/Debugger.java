package dgir.vm.api;

import core.ir.Operation;
import core.debug.Location;
import org.eclipse.lsp4j.debug.Breakpoint;
import org.jetbrains.annotations.NotNull;

/**
 * Callback interface the {@link VM} invokes during debug-mode execution to notify a DAP adapter
 * (or any other debugger front-end) about execution events.
 *
 * <p>Implement this interface and register it with {@link VM#setDebugger(Debugger)} to receive
 * callbacks before each operation is executed.  The callbacks are:
 * <ul>
 *   <li>{@link #onStep(Operation, Location)} — called immediately before every operation is
 *       dispatched.  The debugger can inspect the location, update the DAP client, and decide
 *       whether to proceed or pause.
 *   <li>{@link #onBreakpointHit(Operation, Breakpoint, Location)} — called when the current
 *       operation matches a registered {@link Breakpoint}.
 * </ul>
 *
 * <p>Both callbacks return a {@link DebugControl} value that tells the VM how to proceed:
 * <ul>
 *   <li>{@link DebugControl#CONTINUE} — execute the operation and carry on.
 *   <li>{@link DebugControl#PAUSE}    — block until the debugger calls {@link VM#resume()} or
 *       {@link VM#stepOver()}.
 * </ul>
 */
public interface Debugger {

  /**
   * Called immediately before every operation is executed.
   *
   * @param operation the operation about to be executed.
   * @param location  the source location carried by {@code operation}.
   * @return how the VM should proceed.
   */
  @NotNull DebugControl onStep(@NotNull Operation operation, @NotNull Location location);

  /**
   * Called when the current operation's source location matches a registered {@link Breakpoint}.
   * This is called in addition to (and before) {@link #onStep}.
   *
   * @param operation  the operation about to be executed.
   * @param breakpoint the breakpoint that was hit.
   * @param location   the source location carried by {@code operation}.
   * @return how the VM should proceed.
   */
  @NotNull DebugControl onBreakpointHit(
      @NotNull Operation operation,
      @NotNull Breakpoint breakpoint,
      @NotNull Location location);
}

