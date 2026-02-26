package dgir.vm.dap;

import core.ir.*;
import dgir.vm.api.Breakpoint;
import dgir.vm.api.DebugControl;
import dgir.vm.api.Debugger;
import dgir.vm.api.VM;
import dialect.func.FuncOp;
import org.eclipse.lsp4j.debug.*;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * DAP adapter for the DGIR {@link VM}.
 *
 * <p>Implements both {@link IDebugProtocolServer} (lsp4j's generated DAP server interface, wired to
 * the client over a socket by {@link DapServer}) and {@link Debugger} (the VM's debug callback
 * interface).
 *
 * <h2>Lifecycle</h2>
 *
 * <ol>
 *   <li>{@link DapServer} creates an instance per client connection and passes it to {@code
 *       DSPLauncher.createServerLauncher()}.
 *   <li>lsp4j calls {@link #initialize} / {@link #launch} or {@link #attach} / breakpoint requests
 *       / {@link #configurationDone}.
 *   <li>After {@code configurationDone} the VM runs on a daemon thread, invoking {@link #onStep}
 *       and {@link #onBreakpointHit} before each operation.
 *   <li>Those callbacks fire {@code stopped} events back to the client and block until {@link
 *       #continue_(ContinueArguments)} or {@link #next(NextArguments)} unblocks them.
 * </ol>
 */
public class DapAdapter implements IDebugProtocolServer, Debugger {

  /** Single logical thread ID exposed to the DAP client. */
  private static final int THREAD_ID = 1;

  private static final String THREAD_NAME = "main";

  private final @NotNull VM vm;

  /**
   * The lsp4j-generated client proxy. Set by {@link DapServer} after the launcher is created,
   * before any requests arrive.
   */
  private @Nullable IDebugProtocolClient client;

  /**
   * Counts down to zero when {@code configurationDone} arrives, which unblocks the VM thread so
   * that all breakpoints set during client startup are already registered before execution begins.
   */
  private final @NotNull CountDownLatch configDone = new CountDownLatch(1);

  /** When {@code true}, pause before the very first operation (DAP {@code stopOnEntry}). */
  private volatile boolean stopOnEntry = false;

  /** Tracks whether the entry-stop has already been delivered. */
  private volatile boolean entryHit = false;

  /** Set to {@code true} by {@link #pause(PauseArguments)} to request a suspend. */
  private volatile boolean pauseRequested = false;

  /**
   * Set to {@code true} when a {@code next}/{@code stepIn}/{@code stepOut} command was issued.
   * Cleared and used by {@link #onStep} to fire a {@code "step"} stopped event instead of a generic
   * {@code "pause"} event after the step completes.
   */
  private volatile boolean stepPending = false;

  // =========================================================================
  // Construction
  // =========================================================================

  /**
   * Create an adapter for the given VM. The VM must already be {@link VM#init initialised}.
   *
   * @param vm the VM to debug.
   */
  public DapAdapter(@NotNull VM vm) {
    this.vm = vm;
    vm.setDebugger(this);
  }

  /**
   * Called by {@link DapServer} once the lsp4j launcher has created the remote proxy.
   *
   * @param client the lsp4j-generated client proxy used to send events.
   */
  public void setClient(@NotNull IDebugProtocolClient client) {
    this.client = client;
  }

  // =========================================================================
  // IDebugProtocolServer — lifecycle
  // =========================================================================

  @Override
  public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
    Capabilities caps = new Capabilities();
    caps.setSupportsConfigurationDoneRequest(true);
    caps.setSupportsBreakpointLocationsRequest(true);
    caps.setSupportsStepInTargetsRequest(true);
    caps.setSupportsSingleThreadExecutionRequests(true);
    // Notify the client that we are ready for breakpoint configuration.
    if (client != null) client.initialized();
    return CompletableFuture.completedFuture(caps);
  }

  @Override
  public CompletableFuture<Void> launch(Map<String, Object> args) {
    Object soe = args != null ? args.get("stopOnEntry") : null;
    stopOnEntry = Boolean.TRUE.equals(soe);
    startVmThread();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> attach(Map<String, Object> args) {
    startVmThread();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
    configDone.countDown();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> disconnect(DisconnectArguments args) {
    vm.setDebugger(null);
    vm.clearBreakpoints();
    vm.resume();
    configDone.countDown(); // unblock in case launch hasn't been called
    return CompletableFuture.completedFuture(null);
  }

  // =========================================================================
  // IDebugProtocolServer — breakpoints
  // =========================================================================

  @Override
  public CompletableFuture<SetExceptionBreakpointsResponse> setExceptionBreakpoints(
      SetExceptionBreakpointsArguments args) {
    // The VM does not currently support exception breakpoints; acknowledge the request.
    return CompletableFuture.completedFuture(new SetExceptionBreakpointsResponse());
  }

  @Override
  public CompletableFuture<SetBreakpointsResponse> setBreakpoints(SetBreakpointsArguments args) {
    // Clear only the breakpoints for this source file, then re-register.
    String path = args.getSource() != null ? args.getSource().getPath() : "";
    if (path == null) path = "";

    // Remove existing breakpoints for this file.
    for (Breakpoint bp : new ArrayList<>(vm.getBreakpoints())) {
      if (bp.file().equals(path)) vm.removeBreakpoint(bp);
    }

    List<org.eclipse.lsp4j.debug.Breakpoint> verified = new ArrayList<>();
    SourceBreakpoint[] requested = args.getBreakpoints();
    if (requested != null) {
      for (SourceBreakpoint sb : requested) {
        int line = sb.getLine();
        Integer col = sb.getColumn();
        vm.addBreakpoint(new Breakpoint(path, line, col != null ? col : 0));

        org.eclipse.lsp4j.debug.Breakpoint bp = new org.eclipse.lsp4j.debug.Breakpoint();
        bp.setVerified(true);
        bp.setLine(line);
        bp.setSource(args.getSource());
        verified.add(bp);
      }
    }

    SetBreakpointsResponse resp = new SetBreakpointsResponse();
    resp.setBreakpoints(verified.toArray(new org.eclipse.lsp4j.debug.Breakpoint[0]));
    return CompletableFuture.completedFuture(resp);
  }

  @Override
  public CompletableFuture<BreakpointLocationsResponse> breakpointLocations(
      BreakpointLocationsArguments args) {

    String requestedPath = args.getSource() != null ? args.getSource().getPath() : null;
    int startLine = args.getLine() > 0 ? args.getLine() : 1;
    int endLine = args.getEndLine() > 0 ? args.getEndLine() : startLine;

    List<BreakpointLocation> locations = new ArrayList<>();

    if (vm.getProgram() != null) {
      // Walk every operation in the IR tree and collect those whose source location falls
      // inside the requested file + line range.
      collectBreakpointLocations(
          vm.getProgram().getOperation(), requestedPath, startLine, endLine, locations);
    }

    BreakpointLocationsResponse resp = new BreakpointLocationsResponse();
    resp.setBreakpoints(locations.toArray(new BreakpointLocation[0]));
    return CompletableFuture.completedFuture(resp);
  }

  /**
   * Recursively walks the IR tree rooted at {@code op}, collecting one {@link BreakpointLocation}
   * per distinct (file, line) pair that matches the filter.
   *
   * @param op the root operation to walk
   * @param requestedPath source-file filter; {@code null} means accept any file
   * @param startLine first line of the range (inclusive)
   * @param endLine last line of the range (inclusive)
   * @param out accumulator list
   */
  private static void collectBreakpointLocations(
      @NotNull Operation op,
      @Nullable String requestedPath,
      int startLine,
      int endLine,
      @NotNull List<BreakpointLocation> out) {

    Location loc = op.getLocation();
    if (!loc.equals(Location.UNKNOWN)) {
      boolean fileMatches = requestedPath == null || requestedPath.equals(loc.file());
      boolean lineMatches = loc.line() >= startLine && loc.line() <= endLine;
      if (fileMatches && lineMatches) {
        // Deduplicate: only one entry per (file, line) pair.
        final int line = loc.line();
        final int col = Math.max(0, loc.column());
        boolean alreadyPresent = out.stream().anyMatch(l -> l.getLine() == line);
        if (!alreadyPresent) {
          BreakpointLocation bl = new BreakpointLocation();
          bl.setLine(line);
          bl.setColumn(col);
          out.add(bl);
        }
      }
    }

    // Recurse into nested regions → blocks → operations.
    for (Region region : op.getRegions()) {
      for (Block block : region.getBlocks()) {
        for (Operation nested : block.getOperations()) {
          collectBreakpointLocations(nested, requestedPath, startLine, endLine, out);
        }
      }
    }
  }

  // =========================================================================
  // IDebugProtocolServer — execution control
  // =========================================================================

  @Override
  public CompletableFuture<ContinueResponse> continue_(ContinueArguments args) {
    ContinueResponse resp = new ContinueResponse();
    resp.setAllThreadsContinued(true);
    vm.resume();
    return CompletableFuture.completedFuture(resp);
  }

  @Override
  public CompletableFuture<Void> next(NextArguments args) {
    stepPending = true;
    vm.stepOver();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> stepIn(StepInArguments args) {
    stepPending = true;
    vm.stepIn();
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public CompletableFuture<Void> pause(PauseArguments args) {
    pauseRequested = true;
    return CompletableFuture.completedFuture(null);
  }

  // =========================================================================
  // IDebugProtocolServer — inspection
  // =========================================================================

  @Override
  public CompletableFuture<ThreadsResponse> threads() {
    org.eclipse.lsp4j.debug.Thread t = new org.eclipse.lsp4j.debug.Thread();
    t.setId(THREAD_ID);
    t.setName(THREAD_NAME);
    ThreadsResponse resp = new ThreadsResponse();
    resp.setThreads(new org.eclipse.lsp4j.debug.Thread[] {t});
    return CompletableFuture.completedFuture(resp);
  }

  @Override
  public CompletableFuture<StackTraceResponse> stackTrace(StackTraceArguments args) {
    List<Operation> callStack = vm.getCallStack();
    List<StackFrame> frames = new ArrayList<>();

    Location currentLoc = vm.getCurrentLocation();
    if (!currentLoc.equals(Location.UNKNOWN)) {
      frames.add(buildFrameFromLocation(currentLoc, 1));
    }

    int idBase = frames.size() + 1;
    for (int i = 0; i < callStack.size(); i++) {
      StackFrame frame = getStackFrame(callStack, i, idBase);
      frames.add(frame);
    }

    if (frames.isEmpty()) {
      // VM hasn't started yet or has finished — return a synthetic unknown frame.
      Source src = new Source();
      src.setPath("<unknown>");
      src.setName("<unknown>");
      StackFrame frame = new StackFrame();
      frame.setId(1);
      frame.setName("<unknown>");
      frame.setSource(src);
      frame.setLine(0);
      frame.setColumn(0);
      frames.add(frame);
    }

    StackTraceResponse resp = new StackTraceResponse();
    resp.setStackFrames(frames.toArray(new StackFrame[0]));
    resp.setTotalFrames(frames.size());
    return CompletableFuture.completedFuture(resp);
  }

  private static @NotNull StackFrame buildFrameFromLocation(
      @NotNull Location loc,
      int frameId) {
    Source src = new Source();
    src.setPath(loc.file());
    src.setName(loc.file().contains("/")
        ? loc.file().substring(loc.file().lastIndexOf('/') + 1)
        : loc.file());

    StackFrame frame = new StackFrame();
    frame.setId(frameId);
    frame.setName(loc.toString());
    frame.setSource(src);
    frame.setLine(Math.max(0, loc.line()));
    frame.setColumn(Math.max(0, loc.column()));
    return frame;
  }

  private static @NotNull StackFrame getStackFrame(
      @NotNull List<Operation> callStack,
      int i,
      int idBase) {
    Operation op = callStack.get(i);
    Location loc = op.getLocation();

    Source src = new Source();
    src.setPath(loc.file());
    src.setName(loc.file().contains("/")
        ? loc.file().substring(loc.file().lastIndexOf('/') + 1)
        : loc.file());

    StackFrame frame = new StackFrame();
    // Use index+1 as frame ID so it is unique and non-zero.
    frame.setId(idBase + i);
    frame.setName(buildFrameName(op, loc));
    frame.setSource(src);
    frame.setLine(Math.max(0, loc.line()));
    frame.setColumn(Math.max(0, loc.column()));
    return frame;
  }

  private static @NotNull String buildFrameName(@NotNull Operation op, @NotNull Location loc) {
    String locationText = loc.equals(Location.UNKNOWN) ? op.getDetails().ident() : loc.toString();
    return getFunctionName(op)
        .map(funcName -> funcName + " — " + locationText)
        .orElse(locationText);
  }

  private static @NotNull java.util.Optional<String> getFunctionName(@NotNull Operation op) {
    Operation current = op;
    while (true) {
      if (current.isa(FuncOp.class)) {
        return current
            .as(FuncOp.class)
            .map(func -> func.getFuncNameAttribute().getValue());
      }
      var parent = current.getParentOperation();
      if (parent.isEmpty()) return java.util.Optional.empty();
      current = parent.get();
    }
  }

  @Override
  public CompletableFuture<ScopesResponse> scopes(ScopesArguments args) {
    Scope scope = new Scope();
    scope.setName("Locals");
    scope.setVariablesReference(1);
    scope.setExpensive(false);
    ScopesResponse resp = new ScopesResponse();
    resp.setScopes(new Scope[] {scope});
    return CompletableFuture.completedFuture(resp);
  }

  @Override
  public CompletableFuture<VariablesResponse> variables(VariablesArguments args) {
    List<Variable> vars = new ArrayList<>();

    vm.getState()
        .ifPresent(
            state -> {
              Map<Value, Object> visible = state.getVisibleValues();
              for (Map.Entry<Value, Object> entry : visible.entrySet()) {
                Value value = entry.getKey();
                Object obj = entry.getValue();

                Variable v = new Variable();
                // Use the Value's string representation as the name; fall back to its identity.
                v.setName(value.toString());
                v.setValue(obj != null ? obj.toString() : "null");
                v.setType(obj != null ? obj.getClass().getSimpleName() : "<null>");
                // No nested children for primitive/object values at the IR level.
                v.setVariablesReference(0);
                vars.add(v);
              }
            });

    VariablesResponse resp = new VariablesResponse();
    resp.setVariables(vars.toArray(Variable[]::new));
    return CompletableFuture.completedFuture(resp);
  }

  // =========================================================================
  // Debugger — VM callbacks (called on the VM thread)
  // =========================================================================

  private static boolean isEntryOperation(@NotNull Operation operation) {
    return operation
      .getParentOperation()
      .filter(op -> op.getIndex() == 0)
      .flatMap(op -> op.as(FuncOp.class))
      .map(func -> "main".equals(func.getFuncNameAttribute().getValue()))
      .orElse(false);
  }

  @Override
  public @NotNull DebugControl onStep(@NotNull Operation operation, @NotNull Location location) {

    if (stopOnEntry && !entryHit) {
      if (isEntryOperation(operation)) {
        entryHit = true;
        sendStopped("entry", location);
        return DebugControl.PAUSE;
      }
      return DebugControl.CONTINUE;
    }

    if (pauseRequested) {
      pauseRequested = false;
      sendStopped("pause", location);
      return DebugControl.PAUSE;
    }

    // The VM re-armed a pause after a stepOver/stepIn/stepOut completed.
    if (stepPending) {
      stepPending = false;
      sendStopped("step", location);
      return DebugControl.PAUSE;
    }

    return DebugControl.CONTINUE;
  }

  @Override
  public @NotNull DebugControl onBreakpointHit(
      @NotNull Operation operation, @NotNull Breakpoint breakpoint, @NotNull Location location) {
    sendStopped("breakpoint", location);
    return DebugControl.PAUSE;
  }

  // =========================================================================
  // Private helpers
  // =========================================================================

  /**
   * Launch the VM on a daemon thread. Waits for {@link #configDone} before calling {@link VM#run()}
   * so all breakpoints set during client startup are already registered.
   */
  private void startVmThread() {
    Thread t =
        new Thread(
            () -> {
              try {
                configDone.await();
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
              }

              boolean ok = vm.run();

              if (client != null) {
                ExitedEventArguments exited = new ExitedEventArguments();
                exited.setExitCode(ok ? 0 : 1);
                client.exited(exited);
                client.terminated(new TerminatedEventArguments());
              }
            },
            "dap-vm");
    t.setDaemon(true);
    t.start();
  }

  /**
   * Fire a {@code stopped} event to the client.
   *
   * @param reason one of {@code "entry"}, {@code "breakpoint"}, {@code "pause"}, {@code "step"}.
   * @param location the source location at which execution stopped.
   */
  private void sendStopped(@NotNull String reason, @NotNull Location location) {
    if (client == null) return;
    StoppedEventArguments args = new StoppedEventArguments();
    args.setReason(reason);
    args.setThreadId(THREAD_ID);
    args.setAllThreadsStopped(true);
    if (!location.equals(Location.UNKNOWN)) {
      args.setDescription(location.toString());
    }
    client.stopped(args);
  }
}
