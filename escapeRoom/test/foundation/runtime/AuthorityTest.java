package foundation.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import foundation.definition.CollectionInputDefinition;
import foundation.definition.ComposedRiddleDefinition;
import foundation.definition.DoorDefinition;
import foundation.definition.ExitDefinition;
import foundation.definition.HintDefinition;
import foundation.definition.HintSeverity;
import foundation.definition.InformationSourceDefinition;
import foundation.definition.NumericInputDefinition;
import foundation.definition.RoomDefinition;
import foundation.definition.RosterDefinition;
import foundation.definition.RosterSlotDefinition;
import foundation.definition.SectionDefinition;
import foundation.definition.TimerDefinition;
import foundation.definition.TimerMode;
import foundation.runtime.Projection.ProgressStatus;
import foundation.runtime.Projection.TimerState;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Production-semantics tests for deterministic composed Foundation authority. */
final class AuthorityTest {
  @Test
  void startsStickyWhenMinimumPlayersAreReadyAndActivatesOnlyFirstSection() {
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
    assertEquals(ProgressStatus.ACTIVE, section(authority, "section_one").status());
    assertEquals(ProgressStatus.LOCKED, section(authority, "section_two").status());
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
        CodeOutcome.INCORRECT,
        authority.attemptCode("fund_and_code", "fund_code", "0000").outcome());
    assertEquals(
        CodeOutcome.CORRECT, authority.attemptCode("fund_and_code", "fund_code", "3758").outcome());
    assertEquals(ProgressStatus.SOLVED, riddle(authority, "fund_and_code").status());
    assertEquals(
        CodeOutcome.NOT_EVALUATED,
        authority.attemptCode("fund_and_code", "fund_code", "3758").outcome());

    assertEquals(ProgressStatus.ACTIVE, section(authority, "section_one").status());
    assertEquals(
        CodeOutcome.CORRECT,
        authority.attemptCode("parallel_code", "parallel_input", "24").outcome());
    assertEquals(ProgressStatus.SOLVED, section(authority, "section_one").status());
    assertEquals(ProgressStatus.ACTIVE, section(authority, "section_two").status());
  }

  @Test
  void lockedInputsNeverPrebufferAndActivateOnlyAfterWholePreviousSection() {
    Authority authority = authority(TimerMode.HARD);
    ready(authority);

    assertEquals(
        OperationReason.RIDDLE_LOCKED,
        authority.attemptCode("final_code", "final_input", "9").operation().reason());
    assertEquals(
        OperationReason.RIDDLE_LOCKED,
        authority.interactSource("final_code", "later_source").reason());
    authority.interactSource("fund_and_code", "source");
    authority.attemptCode("fund_and_code", "fund_code", "3758");
    assertEquals(ProgressStatus.LOCKED, riddle(authority, "final_code").status());
    authority.attemptCode("parallel_code", "parallel_input", "24");

    assertEquals(ProgressStatus.ACTIVE, riddle(authority, "final_code").status());
    assertFalse(input(authority, "final_code", "later_collect").satisfied());
    assertFalse(input(authority, "final_code", "final_input").satisfied());
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
        authority.attemptCode("fund_and_code", "missing", "3758").operation().reason());
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
    authority.attemptCode("fund_and_code", "fund_code", "3758");
    assertEquals(
        List.of("hint_one", "hint_two"),
        riddle(authority, "fund_and_code").releasedHints().stream().map(ReleasedHint::id).toList());
  }

  @Test
  void hardTimerContinuesAcrossDisconnectsAndTerminatesExactlyAtLimit() {
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
        List.of(
            new SectionDefinition("section_one", List.of(fundAndCode, parallelCode)),
            new SectionDefinition("section_two", List.of(finalCode))),
        new TimerDefinition(1, mode),
        new DoorDefinition("door"),
        new ExitDefinition("exit", "door"));
  }

  private static void ready(final Authority authority) {
    for (String slot : List.of("slot_1", "slot_2")) {
      authority.connect(slot);
      authority.markSpawned(slot);
    }
  }

  private static void completeRoom(final Authority authority) {
    authority.interactSource("fund_and_code", "source");
    authority.attemptCode("fund_and_code", "fund_code", "3758");
    authority.attemptCode("parallel_code", "parallel_input", "24");
    authority.interactSource("final_code", "later_source");
    authority.attemptCode("final_code", "final_input", "9");
  }

  private static Projection.SectionView section(final Authority authority, final String id) {
    return authority.projection().sections().stream()
        .filter(section -> section.id().equals(id))
        .findFirst()
        .orElseThrow();
  }

  private static Projection.RiddleView riddle(final Authority authority, final String id) {
    return authority.projection().sections().stream()
        .flatMap(section -> section.riddles().stream())
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
