package rooms.soulweaver.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import rooms.soulweaver.modules.loops.LoopPuzzle;
import rooms.soulweaver.modules.loops.TerminalState;
import rooms.soulweaver.modules.methods.MethodsRoute.Action;
import rooms.soulweaver.modules.methods.MethodsWorkshop;
import rooms.soulweaver.modules.methods.MethodsWorkshop.Intent;
import rooms.soulweaver.modules.methods.MethodsWorkshop.Operation;
import rooms.soulweaver.modules.variables.BindingState;
import rooms.soulweaver.modules.variables.VariablePuzzle;
import rooms.soulweaver.state.VariablePuzzleStage;

/** Round trips the room's compact wire values, including a complete workshop execution. */
class SoulweaverStateCodecTest {
  @Test
  void bindingAndTerminalKeepAllValues() {
    var binding =
        new BindingState(
            VariablePuzzleStage.REVEAL,
            true,
            true,
            VariablePuzzle.vesselSolution(),
            VariablePuzzle.essenceSolution(),
            "Gefäß, Name und Wert bilden eine Variable.");
    assertEquals(binding, SoulweaverBinding.decode(SoulweaverBinding.encode(binding)));

    var terminal =
        new TerminalState(
            LoopPuzzle.runes().stream().map(rune -> rune.id()).toList(),
            4,
            165,
            3.25f,
            -1.5f,
            true,
            true,
            "heart-gate-for",
            "Räumauftrag läuft.");
    assertEquals(terminal, SoulweaverTerminal.decode(SoulweaverTerminal.encode(terminal)));

    assertEquals(List.of(), SoulweaverStateCodec.decode("[]", List.class));
  }

  @Test
  void workshopLayoutAndExecutionRoundTripWithoutRepeatedFieldNames() {
    var workshop = new MethodsWorkshop();
    assertWorkshopRoundTrip(workshop.state());
    assertTrue(workshop.apply(1, new Intent(0, Operation.CLAIM, "")));
    assertTrue(workshop.loadHelpSolution(1));
    assertWorkshopRoundTrip(workshop.state());
    assertTrue(workshop.execute(1, new Intent(workshop.state().revision(), Operation.EXECUTE, "")));
    int collected = 0;
    while (true) {
      var step = workshop.next();
      if (step.isEmpty()) break;
      assertTrue(workshop.state().busy());
      int value = step.orElseThrow().action() == Action.COLLECT ? (collected++ == 0 ? 3 : 5) : 0;
      workshop.actionResult(true, value, "");
      assertWorkshopRoundTrip(workshop.state());
    }
    workshop.finish(true, 0);
    assertTrue(workshop.state().completed());
    assertFalse(workshop.state().busy());
    assertWorkshopRoundTrip(workshop.state());
  }

  private static void assertWorkshopRoundTrip(MethodsWorkshop.State state) {
    String encoded = SoulweaverMethods.encode(state);
    assertEquals(state, SoulweaverMethods.decode(encoded));
    // Leave room for the golem's movement and help metadata in the same UDP delta.
    assertTrue(encoded.getBytes(StandardCharsets.UTF_8).length < 900);
  }
}
