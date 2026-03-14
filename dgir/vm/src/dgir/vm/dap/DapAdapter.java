package dgir.vm.dap;

import dgir.core.debug.Location;
import dgir.core.debug.ValueDebugInfo;
import dgir.core.ir.Block;
import dgir.core.ir.Operation;
import dgir.core.ir.Region;
import dgir.core.ir.Value;
import dgir.vm.api.DebugControl;
import dgir.vm.api.Debugger;
import dgir.vm.api.VM;
import org.eclipse.lsp4j.debug.*;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.eclipse.lsp4j.debug.services.IDebugProtocolServer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import static dgir.dialect.builtin.BuiltinOps.ProgramOp;
import static dgir.dialect.func.FuncOps.FuncOp;

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

  private static final Logger LOG = Logger.getLogger(DapAdapter.class.getName());

  /** Single logical thread ID exposed to the DAP client. */
  private static final int THREAD_ID = 1;

  private static final String THREAD_NAME = "main";

  private static final String VALUE_NAME_PREFIX = "%";

  private final @NotNull VM vm;

  // Stable per-session debug names keyed by JVM identity.
  private final @NotNull IdentityHashMap<Value, String> valueNames = new IdentityHashMap<>();
  private final @NotNull AtomicInteger nextValueId = new AtomicInteger(0);

  /**
   * The lsp4j-generated client proxy. Set by {@link DapServer} after the launcher is created,
   * before any requests arrive.
   */
  private @Nullable IDebugProtocolClient client;

  /**
   * Counts down to zero when {@code configurationDone} arrives, which unblocks the VM thread so
   * that all breakpoints set during client startup are already registered before execution begins.
   *
   * <p>Replaced with a fresh {@link CountDownLatch} on each {@link #reloadProgram} call so that the
   * next {@code configurationDone} request from the client unlocks the new VM thread.
   */
  private volatile @NotNull CountDownLatch configDone = new CountDownLatch(1);

  /**
   * Reference to the currently running (or most recently started) VM daemon thread. Used by {@link
   * #reloadProgram} to join the old thread before starting a new one.
   */
  private final @NotNull AtomicReference<Thread> vmThreadRef = new AtomicReference<>();

  /**
   * Set to {@code true} while {@link #reloadProgram} is executing. Suppresses the {@code
   * exited}/{@code terminated} events that the VM thread would normally fire so that the DAP client
   * does not see a "session ended" signal mid-reload.
   */
  private volatile boolean reloadInProgress = false;

  /**
   * Set to {@code true} the first time {@link #startVmThread()} is called. Used by {@link
   * #isVmFinished()} to distinguish "never started" from "started and already done".
   */
  private volatile boolean vmStarted = false;

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
   * <p>Also re-installs this adapter as the VM's debugger, since a previous {@link #onSessionEnded}
   * call may have cleared it.
   *
   * @param client the lsp4j-generated client proxy used to send events.
   */
  public void setClient(@NotNull IDebugProtocolClient client) {
    this.client = client;
    vm.setDebugger(this); // Re-install in case a prior disconnect() cleared it.
  }

  // =========================================================================
  // Getters
  // =========================================================================

  /**
   * Returns {@code true} once the initial entry-stop has been delivered to the client.
   *
   * <p>After this flag is set, the {@link #onStep} callback stops checking for the entry condition
   * and lets the normal step/pause logic take over.
   *
   * @return {@code true} if the {@code "entry"} stopped event has already been fired
   */
  @Override
  @Contract(pure = true)
  public boolean entryHit() {
    return entryHit;
  }

  /**
   * Returns {@code true} if a DAP client is currently attached to this adapter.
   *
   * <p>The {@link DapServer} uses this to reject second connections while one is already active.
   *
   * @return {@code true} when a client proxy is set
   */
  public boolean hasActiveClient() {
    return client != null;
  }

  /**
   * Returns {@code true} if the VM thread is currently alive (running or waiting on a debug pause).
   *
   * @return {@code true} when the VM daemon thread is alive
   */
  public boolean isVmRunning() {
    Thread t = vmThreadRef.get();
    return t != null && t.isAlive();
  }

  /**
   * Returns {@code true} if the VM was started at least once and its thread has since terminated.
   *
   * <p>The {@link DapServer} uses this to reject connections when the program has already finished
   * and no {@link DapServer#reloadProgram} has been issued yet.
   *
   * @return {@code true} when the VM has run and is no longer running
   */
  public boolean isVmFinished() {
    return vmStarted && !isVmRunning();
  }

  // =========================================================================
  // Session lifecycle helpers
  // =========================================================================

  /**
   * Called by {@link DapServer} when the client TCP session ends (either via an explicit {@code
   * disconnect} request or because the socket was closed).
   *
   * <p>Idempotent: if the client reference is already {@code null} (i.e. {@link
   * #disconnect(DisconnectArguments)} was already called) this method is a no-op.
   *
   * <p>Clears the client reference so that {@link #hasActiveClient()} returns {@code false} and a
   * future connection attempt can attach. Also resumes the VM (in case it is paused at a
   * breakpoint) and removes the debugger callback so the VM continues freely after the detach.
   */
  public void onSessionEnded() {
    IDebugProtocolClient c = client;
    if (c == null) return; // already handled (e.g. explicit disconnect request)
    client = null;
    vm.setDebugger(null);
    vm.clearBreakpoints();
    vm.resume();
    configDone.countDown(); // unblock in case launch/configDone was never sent
  }

  // =========================================================================
  // IDebugProtocolServer — lifecycle
  // =========================================================================

  /**
   * DAP initialize: announces adapter capabilities and signals that breakpoint setup may begin.
   *
   * <p>Also resets per-session debug state (step flags, debug-name map) so that a fresh DAP session
   * always starts from a clean slate, even when the adapter is reused across multiple client
   * connections without a {@link #reloadProgram} in between.
   *
   * <p>Important: the client relies on these flags to decide which requests it can send. For
   * example, hover uses {@code evaluate} with {@code context="hover"}, so we must advertise {@code
   * supportsEvaluateForHovers}.
   */
  @Override
  public CompletableFuture<Capabilities> initialize(InitializeRequestArguments args) {
    // Reset transient debug state for this new session.
    pauseRequested = false;
    stepPending = false;
    valueNames.clear();
    nextValueId.set(0);

    Capabilities caps = new Capabilities();
    caps.setSupportsConfigurationDoneRequest(true);
    caps.setSupportsBreakpointLocationsRequest(true);
    caps.setSupportsStepInTargetsRequest(true);
    caps.setSupportsStepBack(true);
    caps.setSupportsSingleThreadExecutionRequests(true);
    caps.setSupportsEvaluateForHovers(true);
    // Notify the client that we are ready for breakpoint configuration.
    if (client != null) client.initialized();
    return CompletableFuture.completedFuture(caps);
  }

  /**
   * DAP launch: starts the VM in debug mode (optionally stopping on entry).
   *
   * <p>If the VM is already running (e.g. because it was started headlessly via {@link
   * DapServer#reloadProgram} before this client connected) this request is a no-op: the live VM
   * continues executing and the client can observe it via breakpoints set during the {@code
   * initialize} handshake.
   */
  @Override
  public CompletableFuture<Void> launch(Map<String, Object> args) {
    if (!isVmRunning()) {
      Object soe = args != null ? args.get("stopOnEntry") : null;
      stopOnEntry = Boolean.TRUE.equals(soe);
      if (!stopOnEntry) {
        entryHit = true;
      }
      startVmThread();
    }
    return CompletableFuture.completedFuture(null);
  }

  /**
   * DAP attach: connects to the VM that is already running (or prepared) and starts it after
   * configuration.
   *
   * <p>If the VM is already running the request is a no-op: the VM continues and the client can
   * observe it via any breakpoints it sets during the handshake.
   */
  @Override
  public CompletableFuture<Void> attach(Map<String, Object> args) {
    if (!isVmRunning()) {
      startVmThread();
    }
    return CompletableFuture.completedFuture(null);
  }

  /** DAP configurationDone: unblocks the VM thread so the program can start. */
  @Override
  public CompletableFuture<Void> configurationDone(ConfigurationDoneArguments args) {
    configDone.countDown();
    return CompletableFuture.completedFuture(null);
  }

  /**
   * DAP disconnect: detach the debugger and let the VM continue running.
   *
   * <p>Clears the client reference (so {@link #hasActiveClient()} returns {@code false}), removes
   * the debugger callback, clears all breakpoints, and resumes the VM in case it is currently
   * paused at a breakpoint or step. The VM thread is <em>not</em> stopped — it keeps running until
   * the program finishes normally or until a subsequent {@link #reloadProgram} call replaces it.
   */
  @Override
  public CompletableFuture<Void> disconnect(DisconnectArguments args) {
    onSessionEnded();
    return CompletableFuture.completedFuture(null);
  }

  // =========================================================================
  // IDebugProtocolServer — breakpoints
  // =========================================================================

  /** DAP setExceptionBreakpoints: not supported by the VM; acknowledge request as no-op. */
  @Override
  public CompletableFuture<SetExceptionBreakpointsResponse> setExceptionBreakpoints(
      SetExceptionBreakpointsArguments args) {
    // The VM does not currently support exception breakpoints; acknowledge the request.
    return CompletableFuture.completedFuture(new SetExceptionBreakpointsResponse());
  }

  /**
   * DAP setBreakpoints: replaces breakpoints for a single source file.
   *
   * <p>DAP expects the adapter to return per-breakpoint verification. We always mark them as
   * verified because the VM accepts any line/column and the actual check happens at runtime.
   */
  @Override
  public CompletableFuture<SetBreakpointsResponse> setBreakpoints(SetBreakpointsArguments args) {
    // Clear only the breakpoints for this source file, then re-register.
    String path = args.getSource() != null ? args.getSource().getPath() : "";
    if (path == null) path = "";

    // Remove existing breakpoints for this file.
    for (Breakpoint bp : new ArrayList<>(vm.getBreakpoints())) {
      Source src = bp.getSource();
      String bpPath = src != null ? src.getPath() : null;
      if (path.equals(bpPath)) vm.removeBreakpoint(bp);
    }

    List<Breakpoint> verified = new ArrayList<>();
    SourceBreakpoint[] requested = args.getBreakpoints();
    if (requested != null) {
      for (SourceBreakpoint sb : requested) {
        var bp = new Breakpoint();
        bp.setVerified(true);
        bp.setLine(sb.getLine());
        bp.setColumn(sb.getColumn());
        bp.setSource(args.getSource());
        verified.add(bp);
        vm.addBreakpoint(bp);
      }
    }

    SetBreakpointsResponse resp = new SetBreakpointsResponse();
    resp.setBreakpoints(verified.toArray(Breakpoint[]::new));
    return CompletableFuture.completedFuture(resp);
  }

  /**
   * DAP breakpointLocations: returns all possible breakpoint lines for a source range.
   *
   * <p>This walks the IR and maps each operation location to a distinct (file, line) entry so the
   * UI can offer valid breakpoint positions.
   */
  @Override
  public CompletableFuture<BreakpointLocationsResponse> breakpointLocations(
      BreakpointLocationsArguments args) {

    String requestedPath = args.getSource() != null ? args.getSource().getPath() : null;
    int startLine = args.getLine() > 0 ? args.getLine() : 1;
    int endLine = startLine;
    if (args.getEndLine() != null) endLine = args.getEndLine() > 0 ? args.getEndLine() : startLine;

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
        for (Operation nested : block.getOperationsRaw()) {
          collectBreakpointLocations(nested, requestedPath, startLine, endLine, out);
        }
      }
    }
  }

  // =========================================================================
  // IDebugProtocolServer — execution control
  // =========================================================================

  /** DAP continue: resumes execution and clears any pending step mode. */
  @Override
  public CompletableFuture<ContinueResponse> continue_(ContinueArguments args) {
    ContinueResponse resp = new ContinueResponse();
    resp.setAllThreadsContinued(true);
    vm.resume();
    return CompletableFuture.completedFuture(resp);
  }

  /** DAP next (step over): request a line-granular step that does not enter calls. */
  @Override
  public CompletableFuture<Void> next(NextArguments args) {
    stepPending = true;
    vm.stepOver();
    return CompletableFuture.completedFuture(null);
  }

  /** DAP stepIn: request a line-granular step that may enter calls. */
  @Override
  public CompletableFuture<Void> stepIn(StepInArguments args) {
    stepPending = true;
    vm.stepIn();
    return CompletableFuture.completedFuture(null);
  }

  /** DAP stepOut: request a step that runs to the end of the current function and then pauses. */
  @Override
  public CompletableFuture<Void> stepOut(StepOutArguments args) {
    stepPending = true;
    vm.stepOut();
    return CompletableFuture.completedFuture(null);
  }

  /** DAP pause: request a suspend on the next safe point. */
  @Override
  public CompletableFuture<Void> pause(PauseArguments args) {
    pauseRequested = true;
    return CompletableFuture.completedFuture(null);
  }

  // =========================================================================
  // IDebugProtocolServer — inspection
  // =========================================================================

  /** DAP threads: the VM is single-threaded, so we expose exactly one thread. */
  @Override
  public CompletableFuture<ThreadsResponse> threads() {
    org.eclipse.lsp4j.debug.Thread t = new org.eclipse.lsp4j.debug.Thread();
    t.setId(THREAD_ID);
    t.setName(THREAD_NAME);
    ThreadsResponse resp = new ThreadsResponse();
    resp.setThreads(new org.eclipse.lsp4j.debug.Thread[] {t});
    return CompletableFuture.completedFuture(resp);
  }

  /**
   * DAP stackTrace: returns the current frame plus any call-stack frames reported by the VM.
   *
   * <p>Frame IDs are adapter-assigned opaque handles and are only meaningful while paused. The
   * client will echo these IDs back in {@code scopes}, {@code variables}, and {@code evaluate}.
   */
  @Override
  public CompletableFuture<StackTraceResponse> stackTrace(StackTraceArguments args) {
    List<Operation> callStack = vm.getState().orElseThrow().getCallStack().stream().toList();
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

  private static @NotNull StackFrame buildFrameFromLocation(@NotNull Location loc, int frameId) {
    Source src = new Source();
    src.setPath(loc.file());
    src.setName(
        loc.file().contains("/")
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
      @NotNull List<Operation> callStack, int i, int idBase) {
    Operation op = callStack.get(i);
    Location loc = op.getLocation();

    Source src = new Source();
    src.setPath(loc.file());
    src.setName(
        loc.file().contains("/")
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
        return current.as(FuncOp.class).map(func -> func.getFuncNameAttribute().getValue());
      }
      var parent = current.getParentOperation();
      if (parent.isEmpty()) return java.util.Optional.empty();
      current = parent.get();
    }
  }

  /**
   * DAP scopes: returns a single "Locals" scope. The {@code variablesReference} is an opaque handle
   * the client uses to request variables.
   */
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

  /**
   * DAP variables: exposes the VM's currently visible values as debugger variables.
   *
   * <p>Each entry is flattened into a name, value, and type string. Nested structures are not
   * expanded yet, so {@code variablesReference} is always 0.
   */
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

                if (!value.getDebugInfo().equals(ValueDebugInfo.UNKNOWN)) {
                  Variable v = new Variable();
                  v.setName(getValueDebugName(value));
                  v.setValue(formatValue(obj));
                  v.setType(value.getType().toString());
                  // No nested children for primitive/object values at the IR level.
                  v.setVariablesReference(0);
                  vars.add(v);
                }
              }
            });

    VariablesResponse resp = new VariablesResponse();
    resp.setVariables(vars.toArray(Variable[]::new));
    return CompletableFuture.completedFuture(resp);
  }

  /**
   * DAP evaluate: resolves an expression in the current scope.
   *
   * <p>Hover uses {@code context="hover"} with a simple identifier expression. We match that
   * identifier against the visible values and return the formatted result.
   */
  @Override
  public CompletableFuture<EvaluateResponse> evaluate(EvaluateArguments args) {
    EvaluateResponse resp = new EvaluateResponse();
    String expression = args != null ? args.getExpression() : null;
    if (expression == null || expression.isBlank()) {
      resp.setResult("<empty>");
      resp.setType("<error>");
      return CompletableFuture.completedFuture(resp);
    }

    var stateOpt = vm.getState();
    if (stateOpt.isEmpty()) {
      resp.setResult("<no state>");
      resp.setType("<error>");
      return CompletableFuture.completedFuture(resp);
    }

    Map<Value, Object> visible = stateOpt.get().getVisibleValues();
    for (Map.Entry<Value, Object> entry : visible.entrySet()) {
      Value value = entry.getKey();
      if (expression.equals(getValueDebugName(value))) {
        Object obj = entry.getValue();
        resp.setResult(formatValue(obj));
        resp.setType(value.getType().toString());
        resp.setVariablesReference(0);
        return CompletableFuture.completedFuture(resp);
      }
    }

    resp.setResult("<not found>");
    resp.setType("<error>");
    return CompletableFuture.completedFuture(resp);
  }

  // =========================================================================
  // Debugger — VM callbacks (called on the VM thread)
  // =========================================================================

  /**
   * Returns {@code true} if {@code operation} is the first operation inside the {@code main}
   * function — i.e. the one that should receive the {@code "entry"} stopped event when {@code
   * stopOnEntry} is enabled.
   *
   * <p>The heuristic used is: the operation's parent operation is at index {@code 0} and that
   * parent is a {@link dgir.dialect.func.FuncOps.FuncOp} whose name is {@code "main"}.
   *
   * @param operation the operation about to be executed
   * @return {@code true} if this is the entry point of the {@code main} function
   */
  private static boolean isEntryOperation(@NotNull Operation operation) {
    return operation
        .getParentOperation()
        .filter(op -> op.getIndex() == 0)
        .flatMap(op -> op.as(FuncOp.class))
        .map(func -> "main".equals(func.getFuncNameAttribute().getValue()))
        .orElse(false);
  }

  /**
   * VM callback before each operation. Decides whether to pause based on entry/step/pause flags.
   */
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

  /** VM callback when a breakpoint is about to be executed. */
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
   *
   * <p>The thread reference is stored in {@link #vmThreadRef} so that {@link #reloadProgram} can
   * join it before starting a replacement thread.
   *
   * <p>The {@code exited} and {@code terminated} events are suppressed when {@link
   * #reloadInProgress} is {@code true}, preventing the DAP client from treating the reload as a
   * session end.
   */
  private void startVmThread() {
    vmStarted = true;
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

              // During a reload the adapter intentionally kills the VM; do not report
              // exited/terminated to the client, as the reload will fire 'initialized' instead.
              if (client != null && !reloadInProgress) {
                ExitedEventArguments exited = new ExitedEventArguments();
                exited.setExitCode(ok ? 0 : 1);
                client.exited(exited);
                client.terminated(new TerminatedEventArguments());
              }
            },
            "dap-vm");
    t.setDaemon(true);
    t.start();
    vmThreadRef.set(t);
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

  // =========================================================================
  // Live reload
  // =========================================================================

  /**
   * Replaces the currently running (or pending) program with {@code newProgram} without closing the
   * DAP session.
   *
   * <h2>Protocol flow</h2>
   *
   * <ol>
   *   <li>Marks {@link #reloadInProgress} so that the dying VM thread does <em>not</em> fire {@code
   *       exited}/{@code terminated} events (which would close the VS Code session).
   *   <li>Releases the old {@link #configDone} latch in case {@code launch}/{@code attach} was
   *       never called (prevents the VM thread from blocking forever).
   *   <li>Calls {@link VM#stop()} to unblock any {@code waitForResume()} call and cause {@link
   *       VM#run()} to return on its next iteration.
   *   <li>Joins the old VM thread with a 5-second safety timeout.
   *   <li>Re-initialises the VM with {@code newProgram} and resets all adapter state flags.
   *   <li>Installs a fresh {@link CountDownLatch} so the next {@link #configurationDone} call will
   *       unblock the new VM thread.
   *   <li>Fires an {@code initialized} event back to the DAP client (if connected). VS Code
   *       responds by re-sending {@code setBreakpoints} for every open source file and then {@code
   *       configurationDone}, which starts the new VM thread.
   *   <li>If <em>no</em> client is connected, starts the VM thread immediately (headless run).
   * </ol>
   *
   * <h2>stopOnEntry behaviour</h2>
   *
   * Pass {@code stopOnEntry = true} to pause execution before the first operation of {@code main},
   * identical to the behaviour of the {@code launch} request.
   *
   * <h2>Thread safety</h2>
   *
   * This method is safe to call from any thread (e.g. the blockly backend thread). It must
   * <em>not</em> be called concurrently with itself.
   *
   * @param newProgram the DGIR program to load; must already be {@link
   *     dgir.dialect.builtin.BuiltinOps.ProgramOp} verified
   * @param stopOnEntry when {@code true} the VM pauses before the first operation of {@code main}
   * @throws InterruptedException if the join on the old VM thread is interrupted
   */
  public void reloadProgram(@NotNull ProgramOp newProgram, boolean stopOnEntry)
      throws InterruptedException {
    reloadInProgress = true;

    // Release the configDone latch in case the VM thread is still waiting on it
    // (i.e. launch/attach was never sent or configurationDone was never sent).
    configDone.countDown();

    // Stop the running VM and wait for its thread to finish.
    vm.stop();
    Thread oldThread = vmThreadRef.getAndSet(null);
    if (oldThread != null && oldThread.isAlive()) {
      // Interrupt the VM thread so that any CountDownLatch.await() calls inside op runners
      // (e.g. waiting for a HeroActionComponent to finish) are unblocked immediately.
      oldThread.interrupt();
      oldThread.join(5_000);
      if (oldThread.isAlive()) {
        LOG.warning("Old VM thread did not terminate within 5 s; continuing anyway.");
      }
    }

    reloadInProgress = false;

    // Re-initialise the VM with the new program.
    vm.init(newProgram);

    // Reset all per-run adapter state.
    this.stopOnEntry = stopOnEntry;
    this.entryHit = !stopOnEntry; // if no stopOnEntry, mark entry as already handled
    this.stepPending = false;
    this.pauseRequested = false;
    valueNames.clear();
    nextValueId.set(0);

    // Install a fresh latch: the next configurationDone call will count it down.
    configDone = new CountDownLatch(1);

    if (client != null) {
      // Signal the client that we are ready for a new round of breakpoint configuration.
      // VS Code responds with setBreakpoints (for each open file) + configurationDone,
      // which unblocks the new VM thread via the configurationDone() handler.
      client.initialized();
      // Start the VM thread now so it is ready to receive configurationDone.
      startVmThread();
    } else {
      // No debugger attached — start immediately without waiting for configurationDone.
      configDone.countDown();
      startVmThread();
    }
  }

  /**
   * Convenience overload that reloads the program without pausing on entry.
   *
   * @param newProgram the DGIR program to load
   * @throws InterruptedException if the join on the old VM thread is interrupted
   * @see #reloadProgram(ProgramOp, boolean)
   */
  public void reloadProgram(@NotNull ProgramOp newProgram) throws InterruptedException {
    reloadProgram(newProgram, false);
  }

  /**
   * Returns a stable, human-readable debug name for the given {@link Value}.
   *
   * <p>If the value carries an explicit source-level name (i.e. {@link Value#getName()} is
   * non-blank and not {@code "<unknown>"}), that name is returned as-is. Otherwise a synthetic name
   * of the form {@code %N} (where {@code N} is a session-scoped integer counter) is assigned on the
   * first call and reused on subsequent calls for the same value object.
   *
   * @param value the IR value to name
   * @return the display name used in {@code variables} and {@code evaluate} responses
   */
  private @NotNull String getValueDebugName(@NotNull Value value) {
    // Prefer source-level names; fallback to stable synthetic names when missing.
    String explicit = value.getName();
    if (!explicit.isBlank() && !"<unknown>".equals(explicit)) return explicit;
    return valueNames.computeIfAbsent(
        value, ignored -> VALUE_NAME_PREFIX + nextValueId.getAndIncrement());
  }

  /**
   * Formats a runtime value for display in the debugger UI.
   *
   * <p>Currently delegates to {@link Object#toString()}. {@code null} values are represented as the
   * literal string {@code "null"}. This method is intended as a single extension point for richer
   * formatting of structured types in the future.
   *
   * @param value the runtime object to format; may be {@code null}
   * @return a non-null string representation of {@code value}
   */
  private static @NotNull String formatValue(@Nullable Object value) {
    // Keep formatting minimal for now; extend for structured types later.
    return value != null ? value.toString() : "null";
  }
}
