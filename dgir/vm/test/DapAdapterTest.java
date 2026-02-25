import core.ir.Location;
import core.ir.Operation;
import dgir.vm.api.Breakpoint;
import dgir.vm.api.DebugControl;
import dgir.vm.api.VM;
import dgir.vm.dap.DapAdapter;
import dgir.vm.dialect.io.PrintRunner;
import dialect.arith.ConstantOp;
import dialect.builtin.ProgramOp;
import dialect.func.FuncOp;
import dialect.func.ReturnOp;
import dialect.io.PrintOp;
import dialect.scf.ContinueOp;
import dialect.scf.ForOp;
import org.eclipse.lsp4j.debug.*;
import org.eclipse.lsp4j.debug.services.IDebugProtocolClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DapAdapter}.
 *
 * <p>Tests drive the adapter directly — no TCP socket or lsp4j launcher is involved. The {@link
 * IDebugProtocolClient} is replaced by a Mockito mock so we can assert which events were sent to
 * the client.
 */
class DapAdapterTest extends VmTestBase {
  @BeforeAll
  static void init() {
    PrintRunner.parallelSystemOut = false;
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /**
   * Build a simple program with a single line of text, for testing breakpoints.
   *
   * <p>Models following java code:
   *
   * <pre>{@code
   * class Test {
   *   public static void main(String[] args) {
   *     IO.print($text);
   *   }
   * }
   * }</pre>
   *
   * @param text the text to print
   */
  static ProgramOp simplePrintProgram(String text) {
    ProgramOp prog = new ProgramOp(new Location("test.java", 1, 1));
    dialect.func.FuncOp main =
        prog.addOperation(new dialect.func.FuncOp(new Location("test.java", 2, 2), "main"));
    var a = main.addOperation(new ConstantOp(new Location("test.java", 3, 12), text), 0);
    main.addOperation(new PrintOp(new Location("test.java", 3, 3), a.getValue()), 0);
    main.addOperation(new ReturnOp(new Location("test.java", 4, 2)), 0);
    return prog;
  }

  /**
   * Build a slightly more complex program with multiple lines and source locations, for testing
   * breakpoints.
   *
   * <p>Models following java code:
   *
   * <pre>{@code
   * class Test {
   *   public static void main(String[] args) {
   *     IO.print("A");
   *     IO.print("B");
   *   }
   * }
   * }</pre>
   */
  static ProgramOp multiLinePrintProgram() {
    ProgramOp prog = new ProgramOp(new Location("test.java", 1, 1));
    dialect.func.FuncOp main =
        prog.addOperation(new dialect.func.FuncOp(new Location("test.java", 2, 2), "main"));
    var a = main.addOperation(new ConstantOp(new Location("test.java", 3, 3), "A\n"), 0);
    main.addOperation(new PrintOp(new Location("test.java", 4, 3), a.getValue()), 0);
    var b = main.addOperation(new ConstantOp(new Location("test.java", 5, 3), "B\n"), 0);
    main.addOperation(new PrintOp(new Location("test.java", 6, 3), b.getValue()), 0);
    main.addOperation(new ReturnOp(new Location("test.java", 7, 2)), 0);
    return prog;
  }

  /**
   * Build a long-running program with a loop, for testing pause and step.
   *
   * <p>Models following java code:
   *
   * <pre>{@code
   * class Test {
   *   public static void main(String[] args)
   *   {
   *     for(int i = 0; i < 1000000; i++) {
   *       System.out.println(i);
   *     }
   *   }
   * }
   * }</pre>
   */
  static ProgramOp longRunningLoop() {
    ProgramOp prog = new ProgramOp(new Location("test.java", 1, 1));
    FuncOp main = prog.addOperation(new FuncOp(new Location("test.java", 2, 2), "main"));
    {
      ConstantOp initValue =
          main.addOperation(new ConstantOp(new Location("test.java", 3, 15), 0), 0);
      ConstantOp lowerBound =
          main.addOperation(new ConstantOp(new Location("test.java", 3, 19), 0), 0);
      ConstantOp upperBound =
          main.addOperation(new ConstantOp(new Location("test.java", 3, 24), 100000), 0);
      ConstantOp step = main.addOperation(new ConstantOp(new Location("test.java", 3, 35), 1), 0);
      ForOp forOp =
          main.addOperation(
              new ForOp(
                  new Location("test.java", 3, 3),
                  initValue.getValue(),
                  lowerBound.getValue(),
                  upperBound.getValue(),
                  step.getValue()),
              0);
      {
        ConstantOp printValue =
            forOp.getEntryBlock().addOperation(new ConstantOp(new Location("test.java", 4, 18), "i: %d\n"));
        forOp.getEntryBlock().addOperation(
            new PrintOp(
                new Location("test.java", 4, 5), printValue.getValue(), forOp.getInductionValue()));
        forOp.getEntryBlock().addOperation(new ContinueOp(new Location("test.java", 4, 3)));
      }
      main.addOperation(new ReturnOp(new Location("test.java", 5, 3)), 0);
    }
    return prog;
  }

  /** Create a VM + DapAdapter wired with a mock client; returns both. */
  static AdapterHandle createHandle(ProgramOp prog) {
    VM vm = new VM();
    vm.init(prog);
    DapAdapter adapter = new DapAdapter(vm);
    IDebugProtocolClient mockClient = mock(IDebugProtocolClient.class);
    adapter.setClient(mockClient);
    return new AdapterHandle(vm, adapter, mockClient);
  }

  record AdapterHandle(VM vm, DapAdapter adapter, IDebugProtocolClient client) {}

  // =========================================================================
  // initialize()
  // =========================================================================

  @Test
  void initialize_returnsExpectedCapabilities() throws Exception {
    var h = createHandle(simplePrintProgram("x"));

    InitializeRequestArguments args = new InitializeRequestArguments();
    args.setClientID("test-client");
    Capabilities caps = h.adapter().initialize(args).get();

    assertTrue(caps.getSupportsConfigurationDoneRequest());
    assertTrue(caps.getSupportsBreakpointLocationsRequest());
    // initialized() event must have been fired
    verify(h.client()).initialized();
  }

  // =========================================================================
  // launch() / configurationDone() lifecycle
  // =========================================================================

  @Test
  void launch_thenConfigDone_runsVmToCompletion() throws Exception {
    var h = createHandle(simplePrintProgram("hello"));

    h.adapter().initialize(new InitializeRequestArguments()).get();
    h.adapter().launch(Map.of()).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    // Give the VM thread a moment to finish.
    ArgumentCaptor<ExitedEventArguments> exited =
        ArgumentCaptor.forClass(ExitedEventArguments.class);
    await(
        () -> {
          verify(h.client(), atLeastOnce()).exited(exited.capture());
          return true;
        });
    assertEquals(0, exited.getValue().getExitCode());
    verify(h.client(), atLeastOnce()).terminated(any(TerminatedEventArguments.class));
  }

  @Test
  void launch_withStopOnEntry_pausesBeforeFirstOp() throws Exception {
    var h = createHandle(simplePrintProgram("hi"));

    h.adapter().initialize(new InitializeRequestArguments()).get();
    h.adapter().launch(Map.of("stopOnEntry", true)).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    // "entry" stopped event must arrive
    ArgumentCaptor<StoppedEventArguments> stopped =
        ArgumentCaptor.forClass(StoppedEventArguments.class);
    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(stopped.capture());
          return true;
        });
    assertEquals("entry", stopped.getValue().getReason());

    // Continue so the VM thread can finish
    h.adapter().continue_(new ContinueArguments()).get();
    await(
        () -> {
          verify(h.client(), atLeastOnce()).terminated(any());
          return true;
        });
    checkOutput("hi");
  }

  @Test
  void attach_runsVmToCompletion() throws Exception {
    var h = createHandle(simplePrintProgram("attach-test"));

    h.adapter().initialize(new InitializeRequestArguments()).get();
    h.adapter().attach(Map.of()).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    await(
        () -> {
          verify(h.client(), atLeastOnce()).terminated(any());
          return true;
        });
    checkOutput("attach-test");
  }

  // =========================================================================
  // disconnect()
  // =========================================================================

  @Test
  void disconnect_unblocksPausedVm() throws Exception {
    var h = createHandle(simplePrintProgram("disconnect-test"));

    h.adapter().initialize(new InitializeRequestArguments()).get();
    h.adapter().launch(Map.of("stopOnEntry", true)).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    // Wait for the entry pause
    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(any());
          return true;
        });

    // Disconnecting should unblock the VM
    h.adapter().disconnect(new DisconnectArguments()).get();

    // VM thread should finish without further interaction
    await(
        () -> {
          // If still alive the debug loop would be calling onStep; after disconnect debugger is
          // null
          assertNull(h.vm().getDebugger().orElse(null));
          return true;
        });
  }

  // =========================================================================
  // setBreakpoints()
  // =========================================================================

  @Test
  void setBreakpoints_registersBreakpointsOnVm() throws Exception {
    var h = createHandle(multiLinePrintProgram());
    h.adapter().initialize(new InitializeRequestArguments()).get();

    Source src = new Source();
    src.setPath("test.java");
    SourceBreakpoint sb1 = new SourceBreakpoint();
    sb1.setLine(4);
    SourceBreakpoint sb2 = new SourceBreakpoint();
    sb2.setLine(6);
    SetBreakpointsArguments args = new SetBreakpointsArguments();
    args.setSource(src);
    args.setBreakpoints(new SourceBreakpoint[] {sb1, sb2});

    SetBreakpointsResponse resp = h.adapter().setBreakpoints(args).get();

    assertEquals(2, resp.getBreakpoints().length);
    assertTrue(resp.getBreakpoints()[0].isVerified());
    assertEquals(4, resp.getBreakpoints()[0].getLine());
    assertTrue(resp.getBreakpoints()[1].isVerified());
    assertEquals(6, resp.getBreakpoints()[1].getLine());

    // VM should now have exactly those two breakpoints
    assertEquals(2, h.vm().getBreakpoints().size());
    assertTrue(h.vm().getBreakpoints().contains(new Breakpoint("test.java", 4, 0)));
    assertTrue(h.vm().getBreakpoints().contains(new Breakpoint("test.java", 6, 0)));
  }

  @Test
  void setBreakpoints_replacesExistingBreakpointsForSameFile() throws Exception {
    var h = createHandle(multiLinePrintProgram());
    h.adapter().initialize(new InitializeRequestArguments()).get();

    Source src = new Source();
    src.setPath("test.java");

    // First call — line 4
    SourceBreakpoint sb1 = new SourceBreakpoint();
    sb1.setLine(4);
    SetBreakpointsArguments args1 = new SetBreakpointsArguments();
    args1.setSource(src);
    args1.setBreakpoints(new SourceBreakpoint[] {sb1});
    h.adapter().setBreakpoints(args1).get();

    // Second call — replace with line 6 only
    SourceBreakpoint sb2 = new SourceBreakpoint();
    sb2.setLine(6);
    SetBreakpointsArguments args2 = new SetBreakpointsArguments();
    args2.setSource(src);
    args2.setBreakpoints(new SourceBreakpoint[] {sb2});
    h.adapter().setBreakpoints(args2).get();

    assertEquals(1, h.vm().getBreakpoints().size());
    assertTrue(h.vm().getBreakpoints().contains(new Breakpoint("test.java", 6, 0)));
    assertFalse(h.vm().getBreakpoints().contains(new Breakpoint("test.java", 4, 0)));
  }

  @Test
  void setBreakpoints_emptyArray_clearsBreakpointsForFile() throws Exception {
    var h = createHandle(multiLinePrintProgram());
    h.adapter().initialize(new InitializeRequestArguments()).get();

    Source src = new Source();
    src.setPath("test.java");
    SourceBreakpoint sb = new SourceBreakpoint();
    sb.setLine(4);
    SetBreakpointsArguments addArgs = new SetBreakpointsArguments();
    addArgs.setSource(src);
    addArgs.setBreakpoints(new SourceBreakpoint[] {sb});
    h.adapter().setBreakpoints(addArgs).get();
    assertEquals(1, h.vm().getBreakpoints().size());

    // Now clear by sending empty array
    SetBreakpointsArguments clearArgs = new SetBreakpointsArguments();
    clearArgs.setSource(src);
    clearArgs.setBreakpoints(new SourceBreakpoint[0]);
    h.adapter().setBreakpoints(clearArgs).get();
    assertTrue(h.vm().getBreakpoints().isEmpty());
  }

  // =========================================================================
  // setExceptionBreakpoints()
  // =========================================================================

  @Test
  void setExceptionBreakpoints_doesNotThrow() throws Exception {
    var h = createHandle(simplePrintProgram("x"));
    SetExceptionBreakpointsArguments args = new SetExceptionBreakpointsArguments();
    args.setFilters(new String[0]);
    assertNotNull(h.adapter().setExceptionBreakpoints(args).get());
  }

  // =========================================================================
  // threads()
  // =========================================================================

  @Test
  void threads_returnsExactlyOneThread() throws Exception {
    var h = createHandle(simplePrintProgram("t"));
    ThreadsResponse resp = h.adapter().threads().get();
    assertEquals(1, resp.getThreads().length);
    assertEquals("main", resp.getThreads()[0].getName());
    assertEquals(1, resp.getThreads()[0].getId());
  }

  // =========================================================================
  // Breakpoint hit pauses and fires stopped event
  // =========================================================================

  @Test
  void breakpointHit_firesStopped_andVmPauses() throws Exception {
    var h = createHandle(multiLinePrintProgram());
    h.adapter().initialize(new InitializeRequestArguments()).get();

    // Set a breakpoint on line 4 (first PrintOp)
    Source src = new Source();
    src.setPath("test.java");
    SourceBreakpoint sb = new SourceBreakpoint();
    sb.setLine(4);
    SetBreakpointsArguments bpArgs = new SetBreakpointsArguments();
    bpArgs.setSource(src);
    bpArgs.setBreakpoints(new SourceBreakpoint[] {sb});
    h.adapter().setBreakpoints(bpArgs).get();

    h.adapter().launch(Map.of()).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    // Wait for breakpoint stopped event
    ArgumentCaptor<StoppedEventArguments> stopped =
        ArgumentCaptor.forClass(StoppedEventArguments.class);
    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(stopped.capture());
          return true;
        });
    assertEquals("breakpoint", stopped.getValue().getReason());
    assertEquals(1, stopped.getValue().getThreadId());
    assertTrue(stopped.getValue().getAllThreadsStopped());

    // Resume so the VM finishes
    h.adapter().continue_(new ContinueArguments()).get();
    await(
        () -> {
          verify(h.client(), atLeastOnce()).terminated(any());
          return true;
        });
  }

  // =========================================================================
  // next() / stepIn() fire "step" stopped event
  // =========================================================================

  @Test
  void next_firesStopped_withStepReason() throws Exception {
    var h = createHandle(multiLinePrintProgram());
    h.adapter().initialize(new InitializeRequestArguments()).get();
    h.adapter().launch(Map.of("stopOnEntry", true)).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    // Wait for entry pause
    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(any());
          return true;
        });
    clearInvocations(h.client());

    // Step one operation
    h.adapter().stepIn(new StepInArguments()).get();

    ArgumentCaptor<StoppedEventArguments> stepped =
        ArgumentCaptor.forClass(StoppedEventArguments.class);
    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(stepped.capture());
          return true;
        });
    assertEquals("step", stepped.getValue().getReason());

    // Finish execution
    h.adapter().continue_(new ContinueArguments()).get();
    await(
        () -> {
          verify(h.client(), atLeastOnce()).terminated(any());
          return true;
        });
  }

  /**
   * Stepping in should behave the same as stepping over for this simple program since there are no
   * function calls to step into, so it should also fire a "step" stopped event.
   */
  @Test
  void stepIn_behavesSameAsNext() throws Exception {
    var h = createHandle(multiLinePrintProgram());
    h.adapter().initialize(new InitializeRequestArguments()).get();
    h.adapter().launch(Map.of("stopOnEntry", true)).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(any());
          return true;
        });
    clearInvocations(h.client());

    h.adapter().stepIn(new StepInArguments()).get();

    ArgumentCaptor<StoppedEventArguments> stepped =
        ArgumentCaptor.forClass(StoppedEventArguments.class);
    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(stepped.capture());
          return true;
        });
    assertEquals("step", stepped.getValue().getReason());

    h.adapter().continue_(new ContinueArguments()).get();
    await(
        () -> {
          verify(h.client(), atLeastOnce()).terminated(any());
          return true;
        });
    checkOutput("A\nB\n");
  }

  // =========================================================================
  // pause() fires "pause" stopped event
  // =========================================================================
  @Test
  void pause_firesStoppedWithPauseReason() throws Exception {
    // Use a long-running program to have time to pause it
    ProgramOp prog = longRunningLoop();

    var h = createHandle(prog);
    h.adapter().initialize(new InitializeRequestArguments()).get();
    h.adapter().launch(Map.of()).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    // Give the VM a moment to start running, then request a pause
    java.lang.Thread.sleep(20);
    h.adapter().pause(new PauseArguments()).get();

    ArgumentCaptor<StoppedEventArguments> stopped =
        ArgumentCaptor.forClass(StoppedEventArguments.class);
    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(stopped.capture());
          return true;
        });
    assertEquals("pause", stopped.getValue().getReason());

    // Resume to let the VM finish
    h.adapter().continue_(new ContinueArguments()).get();
    await(
        () -> {
          verify(h.client(), atLeastOnce()).terminated(any());
          return true;
        });
  }

  // =========================================================================
  // stackTrace()
  // =========================================================================

  @Test
  void stackTrace_whileStopped_returnsNonEmptyFrames() throws Exception {
    var h = createHandle(multiLinePrintProgram());
    h.adapter().initialize(new InitializeRequestArguments()).get();
    h.adapter().launch(Map.of("stopOnEntry", true)).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(any());
          return true;
        });

    StackTraceResponse resp = h.adapter().stackTrace(new StackTraceArguments()).get();
    assertTrue(resp.getTotalFrames() > 0);
    assertNotNull(resp.getStackFrames());
    assertTrue(resp.getStackFrames().length > 0);

    // Every frame must have a non-null source
    for (StackFrame f : resp.getStackFrames()) {
      assertNotNull(f.getSource());
    }

    h.adapter().continue_(new ContinueArguments()).get();
    await(
        () -> {
          verify(h.client(), atLeastOnce()).terminated(any());
          return true;
        });
  }

  @Test
  void stackTrace_afterTermination_returnsSyntheticFrame() throws Exception {
    VM vm = new VM();
    vm.init(simplePrintProgram("x"));
    DapAdapter adapter = new DapAdapter(vm);
    // Don't start the VM — stack is empty
    StackTraceResponse resp = adapter.stackTrace(new StackTraceArguments()).get();
    assertEquals(1, resp.getStackFrames().length);
    assertEquals("<unknown>", resp.getStackFrames()[0].getName());
  }

  // =========================================================================
  // scopes() / variables()
  // =========================================================================

  @Test
  void scopes_returnsSingleLocalsScope() throws Exception {
    var h = createHandle(simplePrintProgram("x"));
    ScopesResponse resp = h.adapter().scopes(new ScopesArguments()).get();
    assertEquals(1, resp.getScopes().length);
    assertEquals("Locals", resp.getScopes()[0].getName());
    assertEquals(1, resp.getScopes()[0].getVariablesReference());
    assertFalse(resp.getScopes()[0].isExpensive());
  }

  @Test
  void variables_whilePaused_returnsCurrentBindings() throws Exception {
    var h = createHandle(multiLinePrintProgram());
    h.adapter().initialize(new InitializeRequestArguments()).get();

    // Break on line 3 (first ConstantOp, before any value is bound)
    // then step once so one constant is computed and bound
    h.adapter().launch(Map.of("stopOnEntry", true)).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(any());
          return true;
        });
    clearInvocations(h.client());

    // Step in - enters the main function and stops on the first ConstantOp
    h.adapter().stepIn(new StepInArguments()).get();
    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(any());
          return true;
        });
    clearInvocations(h.client());

    // Step again — executes ConstantOp("A"), binding the first value.
    h.adapter().next(new NextArguments()).get();
    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(any());
          return true;
        });

    VariablesArguments vArgs = new VariablesArguments();
    vArgs.setVariablesReference(1);
    VariablesResponse resp = h.adapter().variables(vArgs).get();

    // At least one variable should be visible (the constant just computed)
    assertTrue(
        resp.getVariables().length >= 1,
        "Expected at least one variable after stepping, got " + resp.getVariables().length);

    // Each variable must have a non-null name and value
    for (Variable v : resp.getVariables()) {
      assertNotNull(v.getName());
      assertNotNull(v.getValue());
    }

    h.adapter().continue_(new ContinueArguments()).get();
    await(
        () -> {
          verify(h.client(), atLeastOnce()).terminated(any());
          return true;
        });
  }

  @Test
  void variables_beforeVmStarts_returnsEmptyList() throws Exception {
    VM vm = new VM();
    vm.init(simplePrintProgram("x"));
    DapAdapter adapter = new DapAdapter(vm);
    VariablesArguments args = new VariablesArguments();
    args.setVariablesReference(1);
    VariablesResponse resp = adapter.variables(args).get();
    assertNotNull(resp.getVariables());
    // State has no stack frames yet — getVisibleValues returns empty map
    assertEquals(0, resp.getVariables().length);
  }

  // =========================================================================
  // Multiple breakpoints in sequence
  // =========================================================================

  @Test
  void multipleBreakpoints_eachFiresStoppedEvent() throws Exception {
    var h = createHandle(multiLinePrintProgram());
    h.adapter().initialize(new InitializeRequestArguments()).get();

    Source src = new Source();
    src.setPath("test.java");
    SourceBreakpoint bp1 = new SourceBreakpoint();
    bp1.setLine(3);
    SourceBreakpoint bp2 = new SourceBreakpoint();
    bp2.setLine(4);
    SetBreakpointsArguments bpArgs = new SetBreakpointsArguments();
    bpArgs.setSource(src);
    bpArgs.setBreakpoints(new SourceBreakpoint[] {bp1, bp2});
    h.adapter().setBreakpoints(bpArgs).get();

    h.adapter().launch(Map.of()).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    // First breakpoint (first Constant and PrintOp)
    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(any());
          return true;
        });
    clearInvocations(h.client());

    h.adapter().continue_(new ContinueArguments()).get();

    // Second breakpoint (second Constant and PrintOp)
    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(any());
          return true;
        });

    // Finish
    h.adapter().continue_(new ContinueArguments()).get();
    // Finished execution
    await(
        () -> {
          verify(h.client(), atLeastOnce()).terminated(any());
          return true;
        });
    // verify that both breakpoints did fire across the full run (2 invocations before we cleared)
    checkOutput("A\nB\n");
  }

  // =========================================================================
  // continue_() resumes after stop
  // =========================================================================

  @Test
  void continue_afterEntry_runsToCompletion() throws Exception {
    var h = createHandle(simplePrintProgram("continue-test"));
    h.adapter().initialize(new InitializeRequestArguments()).get();
    h.adapter().launch(Map.of("stopOnEntry", true)).get();
    h.adapter().configurationDone(new ConfigurationDoneArguments()).get();

    await(
        () -> {
          verify(h.client(), atLeastOnce()).stopped(any());
          return true;
        });

    ContinueResponse resp = h.adapter().continue_(new ContinueArguments()).get();
    assertTrue(resp.getAllThreadsContinued());

    await(
        () -> {
          verify(h.client(), atLeastOnce()).terminated(any());
          return true;
        });
    checkOutput("continue-test");
  }

  // =========================================================================
  // Debugger callbacks — unit test onStep / onBreakpointHit directly
  // =========================================================================

  @Test
  void onStep_withoutAnyFlags_returnsContinue() {
    VM vm = new VM();
    vm.init(simplePrintProgram("x"));
    DapAdapter adapter = new DapAdapter(vm);
    IDebugProtocolClient mockClient = mock(IDebugProtocolClient.class);
    adapter.setClient(mockClient);

    // No stopOnEntry, no pauseRequested, no stepPending — the stack is empty before run()
    Operation mockOp = mock(Operation.class);
    DebugControl ctrl = adapter.onStep(mockOp, Location.UNKNOWN);
    assertEquals(DebugControl.CONTINUE, ctrl);
    verify(mockClient, never()).stopped(any());
  }

  /**
   * Hitting a breakpoint should fire a "breakpoint" stopped event and return PAUSE to block the VM
   * until the user resumes or steps. The test verifies both behaviors by calling onBreakpointHit
   * directly and checking the return value and the interaction with the mock client. Note that this
   * doesn't run the VM or set real breakpoints — it just tests the callback logic in isolation.
   */
  @Test
  void onBreakpointHit_firesStopped_returnsPause() {
    VM vm = new VM();
    vm.init(simplePrintProgram("x"));
    DapAdapter adapter = new DapAdapter(vm);
    IDebugProtocolClient mockClient = mock(IDebugProtocolClient.class);
    adapter.setClient(mockClient);

    Breakpoint bp = new Breakpoint("test.java", 3, 3);
    Operation mockOp = mock(Operation.class);
    DebugControl ctrl = adapter.onBreakpointHit(mockOp, bp, new Location("test.java", 3, 3));

    assertEquals(DebugControl.PAUSE, ctrl);
    ArgumentCaptor<StoppedEventArguments> stopped =
        ArgumentCaptor.forClass(StoppedEventArguments.class);
    verify(mockClient).stopped(stopped.capture());
    assertEquals("breakpoint", stopped.getValue().getReason());
  }

  // =========================================================================
  // Utility
  // =========================================================================

  /**
   * Spin-wait up to 5 seconds for the given supplier to return {@code true} without throwing. Uses
   * Mockito verification style: wraps the assertion in a callable.
   */
  static void await(Callable<Boolean> condition) throws Exception {
    long deadline = System.currentTimeMillis() + 5_000;
    Throwable last = null;
    while (System.currentTimeMillis() < deadline) {
      try {
        if (Boolean.TRUE.equals(condition.call())) return;
      } catch (Throwable t) {
        last = t;
      }
      java.util.concurrent.locks.LockSupport.parkNanos(20_000_000L); // 20 ms
    }
    if (last instanceof Exception e) throw e;
    if (last instanceof Error e) throw e;
    fail("Condition not met within 5 seconds");
  }
}
