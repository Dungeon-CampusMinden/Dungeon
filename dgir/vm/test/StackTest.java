import dgir.core.debug.Location;
import dgir.core.ir.Operation;
import dgir.core.ir.Value;
import dgir.vm.api.Stack;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static dgir.dialect.builtin.BuiltinTypes.IntegerT.INT64;
import static org.junit.jupiter.api.Assertions.*;

public class StackTest {
  private static final Location LOC = Location.UNKNOWN;

  private static Operation scopeOpener() {
    return new dgir.dialect.scf.ScfOps.ScopeOp(LOC).getOperation();
  }

  private static Operation outputOpener() {
    Value input = new Value(INT64);
    return new dgir.dialect.builtin.BuiltinOps.IdOp(LOC, input).getOperation();
  }

  @Test
  void popCallFrame_closesAllNestedScopesInInnermostFirstOrder() {
    Stack stack = new Stack();
    Operation outerOp = scopeOpener();
    Operation innerOp = scopeOpener();
    Value outerValue = new Value(INT64);
    Value innerValue = new Value(INT64);

    stack.pushCallFrame();
    stack.pushScope(outerOp, false);
    stack.set(outerValue, 1L);
    stack.pushScope(innerOp, false);
    stack.set(innerValue, 2L);

    Stack.ClosedCallFrame closedFrame = stack.popCallFrame().orElseThrow();

    assertEquals(0, stack.frameDepth());
    assertEquals(0, stack.scopeDepth());
    assertEquals(2, closedFrame.closedScopes().size());
    assertSame(innerOp, closedFrame.closedScopes().get(0).opener());
    assertSame(outerOp, closedFrame.closedScopes().get(1).opener());
    assertThrows(IllegalStateException.class, () -> stack.getOrThrow(innerValue));
    assertThrows(IllegalStateException.class, () -> stack.getOrThrow(outerValue));
  }

  @Test
  void popScope_closesOnlyInnermostScope() {
    Stack stack = new Stack();
    Operation outerOp = scopeOpener();
    Operation innerOp = scopeOpener();
    Value outerValue = new Value(INT64);
    Value innerValue = new Value(INT64);

    stack.pushCallFrame();
    stack.pushScope(outerOp, false);
    stack.set(outerValue, 7L);
    stack.pushScope(innerOp, false);
    stack.set(innerValue, 9L);

    Stack.ClosedScope closedScope = stack.popScope().orElseThrow();

    assertSame(innerOp, closedScope.opener());
    assertEquals(1, stack.frameDepth());
    assertEquals(1, stack.scopeDepth());
    assertEquals(7L, stack.getOrThrow(outerValue));
    assertThrows(IllegalStateException.class, () -> stack.getOrThrow(innerValue));
  }

  @Test
  void getVisibleValues_stopsAtIsolationBoundary() {
    Stack stack = new Stack();
    Operation outerOp = scopeOpener();
    Operation isolatedOp = scopeOpener();
    Value outerValue = new Value(INT64);
    Value isolatedValue = new Value(INT64);

    stack.pushCallFrame();
    stack.pushScope(outerOp, false);
    stack.set(outerValue, 3L);
    stack.pushScope(isolatedOp, true);
    stack.set(isolatedValue, 4L);

    Map<Value, Object> visible = stack.getVisibleValues();

    assertEquals(1, visible.size());
    assertEquals(4L, visible.get(isolatedValue));
    assertFalse(visible.containsKey(outerValue));
  }

  @Test
  void closedScope_keepsOpeningOperationForOutputBinding() {
    Stack stack = new Stack();
    Operation opener = outputOpener();

    stack.pushCallFrame();
    stack.pushScope(opener, false);

    Stack.ClosedScope closedScope = stack.popScope().orElseThrow();

    assertSame(opener, closedScope.opener());
    assertTrue(closedScope.opener().getOutputValue().isPresent());
  }
}
