package escaperoom.foundation.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import escaperoom.foundation.definition.AssignmentInputDefinition;
import escaperoom.foundation.definition.ChoiceInputDefinition;
import escaperoom.foundation.definition.CollectionInputDefinition;
import escaperoom.foundation.definition.ComposedRiddleDefinition;
import escaperoom.foundation.definition.DoorDefinition;
import escaperoom.foundation.definition.ExitDefinition;
import escaperoom.foundation.definition.HintDefinition;
import escaperoom.foundation.definition.HintSeverity;
import escaperoom.foundation.definition.InformationSourceDefinition;
import escaperoom.foundation.definition.NumericInputDefinition;
import escaperoom.foundation.definition.ProgressionDefinition;
import escaperoom.foundation.definition.ProgressionDefinition.Edge;
import escaperoom.foundation.definition.ProgressionDefinition.RiddleNode;
import escaperoom.foundation.definition.RoomDefinition;
import escaperoom.foundation.definition.RosterDefinition;
import escaperoom.foundation.definition.RosterSlotDefinition;
import escaperoom.foundation.definition.TimerDefinition;
import escaperoom.foundation.definition.TimerMode;
import escaperoom.foundation.runtime.Projection.ProgressStatus;
import escaperoom.foundation.runtime.Projection.TimerState;
import feature.tasks.AssignmentTask;
import feature.tasks.ChoiceTask;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Production-semantics tests for deterministic composed Foundation authority. */
final class AuthorityTest {
  @Test
  void startsStickyWhenMinimumPlayersAreReadyAndActivatesInitialRiddles() {
    Authority authority = authority(TimerMode.HARD);

    assertEquals(ProgressStatus.LOCKED, riddle(authority, "fund_and_code").status());
    assertEquals(ProgressStatus.LOCKED, riddle(authority, "final_code").status());
    assertEquals(TimerState.WAITING_FOR_READY, authority.projection().timer().state());
    assertEquals(
        OperationReason.SESSION_NOT_RUNNING,
        authority.interactSource("fund_and_code", "source").reason());

    assertEquals(OperationStatus.APPLIED, authority.connect("slot_1").status());
    assertEquals(OperationStatus.APPLIED, authority.markSpawned("slot_1").status());

    assertTrue(authority.projection().timer().started());
    assertEquals(TimerState.RUNNING, authority.projection().timer().state());
    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "fund_and_code").status());
    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "parallel_code").status());
    assertEquals(ProgressStatus.LOCKED, riddle(authority, "final_code").status());
  }

  @Test
  void composedRiddleRequiresAllInputsAndDuplicateActionsAreIdempotent() {
    Authority authority = authority(TimerMode.HARD);
    ready(authority);

    assertEquals(
        OperationStatus.APPLIED, authority.interactSource("fund_and_code", "source").status());
    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "fund_and_code").status());
    assertEquals(
        OperationReason.INPUT_ALREADY_SATISFIED,
        authority.interactSource("fund_and_code", "source").reason());
    assertEquals(
        AttemptOutcome.INCORRECT,
        authority
            .attempt("fund_and_code", "fund_code", NumericInputDefinition.class, "0000")
            .outcome());
    assertEquals(
        AttemptOutcome.CORRECT,
        authority
            .attempt("fund_and_code", "fund_code", NumericInputDefinition.class, "3758")
            .outcome());
    assertEquals(ProgressStatus.SOLVED, riddle(authority, "fund_and_code").status());
    assertEquals(
        AttemptOutcome.NOT_EVALUATED,
        authority
            .attempt("fund_and_code", "fund_code", NumericInputDefinition.class, "3758")
            .outcome());

    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "parallel_code").status());
    assertEquals(
        AttemptOutcome.CORRECT,
        authority
            .attempt("parallel_code", "parallel_input", NumericInputDefinition.class, "24")
            .outcome());
    assertEquals(ProgressStatus.SOLVED, riddle(authority, "parallel_code").status());
    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "final_code").status());
  }

  @Test
  void choiceAndAssignmentInputsAreEvaluatedByTheirTasks() {
    ChoiceTask question =
        new ChoiceTask(
            "Welche?",
            List.of(new ChoiceTask.Option("a", "A"), new ChoiceTask.Option("b", "B")),
            Set.of("a", "b"),
            true);
    AssignmentTask sorting =
        new AssignmentTask("", Set.of("food"), Set.of("bread", "cat"), Map.of("bread", "food"));
    ComposedRiddleDefinition riddle =
        new ComposedRiddleDefinition(
            "sort",
            List.of(),
            List.of(
                new ChoiceInputDefinition("question", "terminal", question),
                new AssignmentInputDefinition("boxes", "boxes_surface", sorting)),
            List.of());
    Authority authority =
        new Authority(
            new RoomDefinition(
                "task_room",
                1,
                new RosterDefinition(List.of(new RosterSlotDefinition("slot_1", 1))),
                new ProgressionDefinition(
                    "start",
                    "exit",
                    List.of(new RiddleNode("n_sort", riddle)),
                    List.of(new Edge("start", "n_sort"), new Edge("n_sort", "exit"))),
                new TimerDefinition(10, TimerMode.HARD),
                new DoorDefinition("door"),
                new ExitDefinition("exit", "door")));
    ready(authority);

    assertEquals(
        AttemptOutcome.INCORRECT,
        authority.attempt("sort", "question", ChoiceInputDefinition.class, Set.of("a")).outcome());
    assertEquals(
        AttemptOutcome.CORRECT,
        authority
            .attempt("sort", "question", ChoiceInputDefinition.class, Set.of("a", "b"))
            .outcome());
    assertEquals(
        AttemptOutcome.INCORRECT,
        authority
            .attempt(
                "sort",
                "boxes",
                AssignmentInputDefinition.class,
                Map.of("bread", "food", "cat", "food"))
            .outcome());
    assertEquals(
        AttemptOutcome.CORRECT,
        authority
            .attempt("sort", "boxes", AssignmentInputDefinition.class, Map.of("bread", "food"))
            .outcome());
    assertEquals(ProgressStatus.SOLVED, riddle(authority, "sort").status());
  }

  @Test
  void lockedInputsNeverPrebufferAndActivateOnlyAfterAllPredecessors() {
    Authority authority = authority(TimerMode.HARD);
    ready(authority);

    assertEquals(
        OperationReason.RIDDLE_LOCKED,
        authority
            .attempt("final_code", "final_input", NumericInputDefinition.class, "9")
            .operation()
            .reason());
    assertEquals(
        OperationReason.RIDDLE_LOCKED,
        authority.interactSource("final_code", "later_source").reason());
    authority.interactSource("fund_and_code", "source");
    authority.attempt("fund_and_code", "fund_code", NumericInputDefinition.class, "3758");
    assertEquals(ProgressStatus.LOCKED, riddle(authority, "final_code").status());
    authority.attempt("parallel_code", "parallel_input", NumericInputDefinition.class, "24");

    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "final_code").status());
    assertFalse(input(authority, "final_code", "later_collect").satisfied());
    assertFalse(input(authority, "final_code", "final_input").satisfied());
  }

  @Test
  void executesExactStaggeredAndDagDependencies() {
    Authority authority = new Authority(staggeredDefinition());
    authority.connect("slot_1");
    authority.markSpawned("slot_1");

    assertEquals(
        List.of("recover", "ventilation", "storage", "unlock"),
        authority.projection().riddles().stream().map(Projection.RiddleView::id).toList());
    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "recover").status());
    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "ventilation").status());
    assertEquals(ProgressStatus.LOCKED, riddle(authority, "storage").status());
    assertEquals(ProgressStatus.LOCKED, riddle(authority, "unlock").status());

    authority.attempt("recover", "input_recover", NumericInputDefinition.class, "1");
    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "storage").status());
    assertEquals(ProgressStatus.LOCKED, riddle(authority, "unlock").status());
    authority.attempt("storage", "input_storage", NumericInputDefinition.class, "1");
    assertEquals(ProgressStatus.LOCKED, riddle(authority, "unlock").status());
    assertFalse(authority.projection().doorOpen());

    authority.attempt("ventilation", "input_ventilation", NumericInputDefinition.class, "1");
    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "unlock").status());
    assertFalse(authority.projection().doorOpen());
    authority.attempt("unlock", "input_unlock", NumericInputDefinition.class, "1");
    assertTrue(authority.projection().doorOpen());
  }

  @Test
  void endWaitsForEveryDirectRiddlePredecessor() {
    Authority authority = new Authority(directEndPredecessorsDefinition());
    authority.connect("slot_1");
    authority.markSpawned("slot_1");

    authority.attempt("left", "input_left", NumericInputDefinition.class, "1");
    assertFalse(authority.projection().doorOpen());

    authority.attempt("right", "input_right", NumericInputDefinition.class, "1");
    assertTrue(authority.projection().doorOpen());
  }

  @Test
  void progressionRejectsInvalidTopologyAndDefensivelyCopiesLists() {
    RiddleNode only = new RiddleNode("n_only", simpleRiddle("only"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ProgressionDefinition(
                "start", "exit", List.of(only), List.of(new Edge("start", "unknown"))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ProgressionDefinition(
                "start",
                "exit",
                List.of(only),
                List.of(
                    new Edge("start", "n_only"),
                    new Edge("n_only", "n_only"),
                    new Edge("n_only", "exit"))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ProgressionDefinition(
                "start",
                "exit",
                List.of(only),
                List.of(
                    new Edge("start", "n_only"),
                    new Edge("start", "n_only"),
                    new Edge("n_only", "exit"))));

    RiddleNode first = new RiddleNode("n_first", simpleRiddle("first"));
    RiddleNode second = new RiddleNode("n_second", simpleRiddle("second"));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ProgressionDefinition(
                "start",
                "exit",
                List.of(first, second),
                List.of(
                    new Edge("start", "n_first"),
                    new Edge("n_first", "n_second"),
                    new Edge("n_second", "n_first"),
                    new Edge("n_second", "exit"))));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ProgressionDefinition(
                "start",
                "exit",
                List.of(first, second),
                List.of(
                    new Edge("start", "n_first"),
                    new Edge("n_first", "exit"),
                    new Edge("n_second", "exit"))));

    List<RiddleNode> nodes = new ArrayList<>(List.of(only));
    List<Edge> edges =
        new ArrayList<>(List.of(new Edge("start", "n_only"), new Edge("n_only", "exit")));
    ProgressionDefinition progression = new ProgressionDefinition("start", "exit", nodes, edges);
    nodes.clear();
    edges.clear();

    assertEquals(List.of(only), progression.riddleNodes());
    assertEquals(
        List.of(new Edge("start", "n_only"), new Edge("n_only", "exit")), progression.edges());
  }

  @Test
  void invalidCommandsAreRejectedWithoutProgress() {
    Authority authority = authority(TimerMode.HARD);
    ready(authority);

    assertEquals(OperationReason.UNKNOWN_SLOT, authority.connect("missing").reason());
    assertEquals(
        OperationReason.UNKNOWN_RIDDLE, authority.interactSource("missing", "source").reason());
    assertEquals(
        OperationReason.UNKNOWN_INFORMATION_SOURCE,
        authority.interactSource("fund_and_code", "missing").reason());
    assertEquals(
        OperationReason.UNKNOWN_INPUT,
        authority
            .attempt("fund_and_code", "missing", NumericInputDefinition.class, "3758")
            .operation()
            .reason());
    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "fund_and_code").status());
  }

  @Test
  void hintsReleaseInAuthoredOrderOnlyWhileActiveAndRemainProjected() {
    Authority authority = authority(TimerMode.HARD);
    assertTrue(authority.previewHint("fund_and_code").isEmpty());
    ready(authority);

    HintPreview firstPreview = authority.previewHint("fund_and_code").orElseThrow();
    assertEquals(HintSeverity.ORIENTATION, firstPreview.severity());
    assertTrue(riddle(authority, "fund_and_code").releasedHints().isEmpty());
    HintRevealResult first = authority.revealHint("fund_and_code", firstPreview.id());
    HintPreview secondPreview = authority.previewHint("fund_and_code").orElseThrow();
    assertEquals(HintSeverity.APPROACH, secondPreview.severity());
    HintRevealResult stale = authority.revealHint("fund_and_code", firstPreview.id());
    HintRevealResult second = authority.revealHint("fund_and_code", secondPreview.id());
    HintRevealResult exhausted = authority.revealHint("fund_and_code", secondPreview.id());

    assertEquals("hint_one", first.hint().orElseThrow().id());
    assertEquals(OperationReason.HINT_PREVIEW_STALE, stale.operation().reason());
    assertEquals("hint_two", second.hint().orElseThrow().id());
    assertEquals(OperationReason.HINTS_EXHAUSTED, exhausted.operation().reason());
    assertEquals(
        OperationReason.RIDDLE_LOCKED,
        authority.revealHint("final_code", "final_hint").operation().reason());
    authority.interactSource("fund_and_code", "source");
    authority.attempt("fund_and_code", "fund_code", NumericInputDefinition.class, "3758");
    assertEquals(
        List.of("hint_one", "hint_two"),
        riddle(authority, "fund_and_code").releasedHints().stream().map(ReleasedHint::id).toList());
  }

  @Test
  void hardTimerConsumesSuppliedActiveTimeAndTerminatesExactlyAtLimit() {
    Authority authority = authority(TimerMode.HARD);
    ready(authority);
    Projection.TimerView initialTimer = authority.projection().timer();
    authority.advance(Duration.ofMillis(999));
    assertEquals(initialTimer, authority.projection().timer());
    authority.advance(Duration.ofMillis(19_001));
    authority.disconnect("slot_2");
    assertEquals(TimerState.RUNNING, authority.projection().timer().state());
    authority.advance(Duration.ofSeconds(39));
    assertTrue(authority.projection().terminal().isEmpty());

    authority.advance(Duration.ofSeconds(1));

    assertEquals(TerminalResult.HARD_TIMEOUT, authority.projection().terminal().orElseThrow());
    assertEquals(Duration.ofMinutes(1), authority.projection().timer().elapsed());
    assertEquals(
        OperationReason.SESSION_TERMINAL, authority.advance(Duration.ofSeconds(1)).reason());
  }

  @Test
  void softTimerMarksOvertimeWithoutTerminating() {
    Authority authority = authority(TimerMode.SOFT);
    ready(authority);
    authority.advance(Duration.ofMinutes(1));

    assertTrue(authority.projection().timer().overtime());
    assertEquals(Duration.ZERO, authority.projection().timer().remaining());
    assertTrue(authority.projection().terminal().isEmpty());
    assertEquals(OperationStatus.APPLIED, authority.advance(Duration.ofMinutes(2)).status());
    assertEquals(Duration.ofMinutes(3), authority.projection().timer().elapsed());
  }

  @Test
  void successUsesCurrentlyReadyPlayersAndExitOperationsAreIdempotent() {
    Authority authority = authority(TimerMode.HARD);
    ready(authority);
    assertEquals(OperationReason.DOOR_CLOSED, authority.enterExit("slot_1").reason());
    completeRoom(authority);
    assertTrue(authority.projection().doorOpen());

    assertEquals(OperationStatus.APPLIED, authority.enterExit("slot_1").status());
    assertEquals(OperationReason.ALREADY_IN_EXIT, authority.enterExit("slot_1").reason());
    assertEquals(OperationStatus.APPLIED, authority.leaveExit("slot_1").status());
    assertEquals(OperationReason.NOT_IN_EXIT, authority.leaveExit("slot_1").reason());
    authority.enterExit("slot_1");
    authority.disconnect("slot_2");

    assertEquals(TerminalResult.SUCCESS, authority.projection().terminal().orElseThrow());
    assertEquals(OperationReason.SESSION_TERMINAL, authority.leaveExit("slot_1").reason());
  }

  @Test
  void abortAndTerminalResultsAreImmutableAndIdempotent() {
    Authority authority = authority(TimerMode.HARD);

    assertEquals(OperationStatus.APPLIED, authority.abort().status());
    assertEquals(TerminalResult.ABORTED, authority.projection().terminal().orElseThrow());
    assertFalse(authority.projection().timer().started());
    assertEquals(TimerState.TERMINAL, authority.projection().timer().state());
    assertEquals(OperationReason.SESSION_TERMINAL, authority.abort().reason());
    assertEquals(OperationReason.SESSION_TERMINAL, authority.connect("slot_1").reason());
  }

  @Test
  void projectionContainsPersistentInputStateButNoAnswers() {
    Authority authority = authority(TimerMode.HARD);
    ready(authority);
    HintPreview preview = authority.previewHint("fund_and_code").orElseThrow();
    authority.revealHint("fund_and_code", preview.id());
    authority.interactSource("fund_and_code", "source");

    assertEquals(
        List.of("hint_one"),
        riddle(authority, "fund_and_code").releasedHints().stream().map(ReleasedHint::id).toList());
    assertTrue(input(authority, "fund_and_code", "collect").satisfied());
    assertFalse(input(authority, "fund_and_code", "fund_code").satisfied());
    assertEquals(
        4, input(authority, "fund_and_code", "fund_code").visibleDigitCount().orElseThrow());
    assertTrue(input(authority, "final_code", "final_input").visibleDigitCount().isEmpty());
  }

  private static Authority authority(final TimerMode mode) {
    return new Authority(definition(mode));
  }

  private static RoomDefinition definition(final TimerMode mode) {
    ComposedRiddleDefinition fundAndCode =
        new ComposedRiddleDefinition(
            "fund_and_code",
            List.of(
                new InformationSourceDefinition(
                    "source", "surface_source", List.of("resource_a", "resource_b"))),
            List.of(
                new CollectionInputDefinition("collect", "source"),
                new NumericInputDefinition("fund_code", "surface_fund_code", "3758", true)),
            List.of(
                new HintDefinition("hint_one", "One", "released one", HintSeverity.ORIENTATION),
                new HintDefinition("hint_two", "Two", "released two", HintSeverity.APPROACH)));
    ComposedRiddleDefinition parallelCode =
        new ComposedRiddleDefinition(
            "parallel_code",
            List.of(),
            List.of(new NumericInputDefinition("parallel_input", "surface_parallel", "24", true)),
            List.of());
    ComposedRiddleDefinition finalCode =
        new ComposedRiddleDefinition(
            "final_code",
            List.of(
                new InformationSourceDefinition(
                    "later_source", "surface_later", List.of("later_resource"))),
            List.of(
                new CollectionInputDefinition("later_collect", "later_source"),
                new NumericInputDefinition("final_input", "surface_final", "9", false)),
            List.of(
                new HintDefinition("final_hint", "Final", "final hint", HintSeverity.SOLUTION)));
    return new RoomDefinition(
        "room",
        1,
        new RosterDefinition(
            List.of(new RosterSlotDefinition("slot_1", 1), new RosterSlotDefinition("slot_2", 2))),
        new ProgressionDefinition(
            "start",
            "exit",
            List.of(
                new RiddleNode("n_fund", fundAndCode),
                new RiddleNode("n_parallel", parallelCode),
                new RiddleNode("n_final", finalCode)),
            List.of(
                new Edge("start", "n_fund"),
                new Edge("start", "n_parallel"),
                new Edge("n_fund", "n_final"),
                new Edge("n_parallel", "n_final"),
                new Edge("n_final", "exit"))),
        new TimerDefinition(1, mode),
        new DoorDefinition("door"),
        new ExitDefinition("exit", "door"));
  }

  private static RoomDefinition staggeredDefinition() {
    ComposedRiddleDefinition recover = simpleRiddle("recover");
    ComposedRiddleDefinition ventilation = simpleRiddle("ventilation");
    ComposedRiddleDefinition storage = simpleRiddle("storage");
    ComposedRiddleDefinition unlock = simpleRiddle("unlock");
    return new RoomDefinition(
        "staggered_room",
        1,
        new RosterDefinition(List.of(new RosterSlotDefinition("slot_1", 1))),
        new ProgressionDefinition(
            "start",
            "exit",
            List.of(
                new RiddleNode("n_recover", recover),
                new RiddleNode("n_ventilation", ventilation),
                new RiddleNode("n_storage", storage),
                new RiddleNode("n_unlock", unlock)),
            List.of(
                new Edge("start", "n_recover"),
                new Edge("start", "n_ventilation"),
                new Edge("n_recover", "n_storage"),
                new Edge("n_storage", "n_unlock"),
                new Edge("n_ventilation", "n_unlock"),
                new Edge("n_unlock", "exit"))),
        new TimerDefinition(10, TimerMode.HARD),
        new DoorDefinition("door"),
        new ExitDefinition("exit", "door"));
  }

  private static RoomDefinition directEndPredecessorsDefinition() {
    ComposedRiddleDefinition left = simpleRiddle("left");
    ComposedRiddleDefinition right = simpleRiddle("right");
    return new RoomDefinition(
        "direct_end_room",
        1,
        new RosterDefinition(List.of(new RosterSlotDefinition("slot_1", 1))),
        new ProgressionDefinition(
            "start",
            "exit",
            List.of(new RiddleNode("n_left", left), new RiddleNode("n_right", right)),
            List.of(
                new Edge("start", "n_left"),
                new Edge("start", "n_right"),
                new Edge("n_left", "exit"),
                new Edge("n_right", "exit"))),
        new TimerDefinition(10, TimerMode.HARD),
        new DoorDefinition("door"),
        new ExitDefinition("exit", "door"));
  }

  private static ComposedRiddleDefinition simpleRiddle(final String id) {
    return new ComposedRiddleDefinition(
        id,
        List.of(),
        List.of(new NumericInputDefinition("input_" + id, "surface_" + id, "1", false)),
        List.of());
  }

  private static void ready(final Authority authority) {
    for (String slot : List.of("slot_1", "slot_2")) {
      authority.connect(slot);
      authority.markSpawned(slot);
    }
  }

  private static void completeRoom(final Authority authority) {
    authority.interactSource("fund_and_code", "source");
    authority.attempt("fund_and_code", "fund_code", NumericInputDefinition.class, "3758");
    authority.attempt("parallel_code", "parallel_input", NumericInputDefinition.class, "24");
    authority.interactSource("final_code", "later_source");
    authority.attempt("final_code", "final_input", NumericInputDefinition.class, "9");
  }

  private static Projection.RiddleView riddle(final Authority authority, final String id) {
    return authority.projection().riddles().stream()
        .filter(riddle -> riddle.id().equals(id))
        .findFirst()
        .orElseThrow();
  }

  private static Projection.InputView input(
      final Authority authority, final String riddleId, final String inputId) {
    return riddle(authority, riddleId).inputs().stream()
        .filter(input -> input.id().equals(inputId))
        .findFirst()
        .orElseThrow();
  }
}
