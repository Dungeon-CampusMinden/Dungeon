package rooms.programming.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.loops.TerminalState;
import rooms.programming.modules.methods.MethodsRoute.Action;
import rooms.programming.modules.methods.MethodsWorkshop;
import rooms.programming.modules.methods.MethodsWorkshop.Intent;
import rooms.programming.modules.methods.MethodsWorkshop.Operation;
import rooms.programming.modules.variables.BindingState;
import rooms.programming.modules.variables.VariablePuzzle;
import rooms.programming.state.VariablePuzzleStage;

/** Round trips the room's compact wire values, including a complete workshop execution. */
class ProgrammingStateCodecTest {
  @Test
  void bindingTerminalAndJournalKeepAllValues() {
    var binding =
        new BindingState(
            VariablePuzzleStage.REVEAL,
            true,
            true,
            VariablePuzzle.vesselSolution(),
            VariablePuzzle.essenceSolution(),
            "Gefäß, Name und Wert bilden eine Variable.");
    assertEquals(binding, ProgrammingBinding.decode(ProgrammingBinding.encode(binding)));

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
    assertEquals(terminal, ProgrammingTerminal.decode(ProgrammingTerminal.encode(terminal)));

    var journal = new ProgrammingProgress.JournalEntry("Hilfe", "Gefäß → Größe", 42, false, "");
    assertEquals(
        journal,
        ProgrammingStateCodec.decode(
            ProgrammingStateCodec.encode(journal), ProgrammingProgress.JournalEntry.class));
    assertEquals(List.of(), ProgrammingStateCodec.decode("[]", List.class));
  }

  @Test
  void workshopLayoutAndExecutionRoundTripWithoutRepeatedFieldNames() {
    var workshop = new MethodsWorkshop();
    assertWorkshopRoundTrip(workshop.state());
    assertTrue(workshop.apply(1, new Intent(0, 0, Operation.CLAIM, "")));
    assertTrue(workshop.loadHelpSolution(1));
    assertWorkshopRoundTrip(workshop.state());
    assertTrue(
        workshop.execute(1, new Intent(workshop.state().revision(), 0, Operation.EXECUTE, "")));
    int collected = 0;
    while (true) {
      var step = workshop.next();
      if (step.isEmpty()) break;
      int value = step.orElseThrow().action() == Action.COLLECT ? (collected++ == 0 ? 3 : 5) : 0;
      workshop.actionResult(true, value, "");
      assertWorkshopRoundTrip(workshop.state());
    }
    workshop.finish(true, 0);
    assertTrue(workshop.state().completed());
    assertWorkshopRoundTrip(workshop.state());
  }

  private static void assertWorkshopRoundTrip(MethodsWorkshop.State state) {
    String encoded = ProgrammingMethods.encode(state);
    assertEquals(state, ProgrammingMethods.decode(encoded));
    // Leave room for the golem's movement and help metadata in the same UDP delta.
    assertTrue(encoded.getBytes(StandardCharsets.UTF_8).length < 900);
  }
}
