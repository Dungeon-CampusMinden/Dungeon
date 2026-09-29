package rooms.programming.modules.loops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/** Interpreter state must describe completed physical actions. */
class LoopExecutionTest {
  @Test
  void interruptedMovementDoesNotEnterReturnHistory() {
    LoopExecution execution =
        new LoopExecution(0, LoopPuzzle.rune("archive-long").orElseThrow(), true);
    var start = execution.cell();
    var step = execution.next().orElseThrow();
    assertNotEquals(start, step.to());
    assertEquals(start, execution.cell());
    assertEquals(java.util.List.of(start), execution.history());
  }
}
