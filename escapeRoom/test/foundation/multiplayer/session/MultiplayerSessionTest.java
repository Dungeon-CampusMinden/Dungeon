package foundation.multiplayer.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import foundation.multiplayer.session.MultiplayerSession.ClientObservation;
import foundation.presentation.GamePresentation;
import foundation.presentation.GamePresentation.ComposedPresentation;
import foundation.presentation.GamePresentation.InformationSourcePresentation;
import foundation.presentation.GamePresentation.NumericInputPresentation;
import foundation.presentation.GamePresentation.ResourcePresentation;
import foundation.runtime.CodeOutcome;
import foundation.runtime.Projection.TimerState;
import foundation.runtime.ReleasedHint;
import foundation.runtime.TerminalResult;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** Room-first multiplayer adapter contracts. */
final class MultiplayerSessionTest {
  @Test
  void startsAtMinimumAndDoesNotPauseAcrossDisconnectOrLateJoin() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RosterDefinition(
                List.of(
                    new RosterSlotDefinition("slot_1", 1), new RosterSlotDefinition("slot_3", 3))));

    MultiplayerSession session = session(2, 3);
    session.reconcileClients(List.of(observation(1)));
    assertTrue(session.introEligiblePlayerEntities().isEmpty());
    assertFalse(session.completeIntro((short) 1));
    session.reconcileClients(List.of(observation(1), observation(2), observation(3)));
    assertEquals(3, session.introEligiblePlayerEntities().size());

    session.completeIntro((short) 2);
    session.reconcileClients(List.of(observation(1), observation(2)));
    assertEquals(TimerState.WAITING_FOR_READY, session.projection().timer().state());
    session.completeIntro((short) 1);
    assertEquals(TimerState.RUNNING, session.projection().timer().state());
    session.advance(Duration.ofSeconds(5));
    session.reconcileClients(List.of(observation(1)));
    session.advance(Duration.ofSeconds(5));

    assertEquals(Duration.ofSeconds(10), session.projection().timer().elapsed());
    assertEquals(Map.of("slot_1", 100), session.readyPlayerEntities());
    session.reconcileClients(List.of(observation(1), observation(3)));
    assertTrue(session.readyClientId(300).isEmpty());
    session.completeIntro((short) 3);
    assertEquals(Map.of("slot_1", 100, "slot_3", 300), session.readyPlayerEntities());
  }

  @Test
  void lateJoinerIsTechnicalOnlyUntilIntroAndCannotAffectAnyInput() {
    MultiplayerSession session = session(1, 2);
    session.reconcileClients(List.of(observation(1)));
    session.completeIntro((short) 1);
    session.reconcileClients(List.of(observation(1), observation(2)));

    assertEquals(Map.of("slot_1", 100), session.readyPlayerEntities());
    assertEquals(Map.of("slot_1", 100, "slot_2", 200), session.technicalPlayerEntities());
    assertTrue(session.readyClientId(200).isEmpty());
    assertTrue(session.inspectSource((short) 2, "first", "source").isEmpty());
    assertTrue(session.enterNumericCode((short) 2, "first", "first_code", "12").isEmpty());
    assertFalse(inputSatisfied(session, "first", "collect"));
    assertFalse(inputSatisfied(session, "first", "first_code"));

    assertTrue(session.completeIntro((short) 2));
    assertEquals(Optional.of((short) 2), session.readyClientId(200));
    assertEquals(
        CodeOutcome.INCORRECT,
        session.enterNumericCode((short) 2, "first", "first_code", "1").orElseThrow().outcome());
  }

  @Test
  void replacementEntityMustCompleteIntroAgainBeforeGameplay() {
    MultiplayerSession session = session(1, 1);
    session.reconcileClients(List.of(observation(1)));
    session.completeIntro((short) 1);
    assertEquals(Optional.of((short) 1), session.readyClientId(100));

    session.reconcileClients(List.of(new ClientObservation((short) 1, 101)));

    assertTrue(session.readyPlayerEntities().isEmpty());
    assertTrue(session.readyClientId(101).isEmpty());
    assertTrue(session.inspectSource((short) 1, "first", "source").isEmpty());
    session.completeIntro((short) 1);
    assertEquals(Optional.of((short) 1), session.readyClientId(101));
  }

  @Test
  void sourcesReopenAllContentsAndLockedReadsNeverPrecreditCollection() {
    MultiplayerSession session = startedSession();

    var early = session.inspectSource((short) 1, "second", "later_source").orElseThrow();
    assertFalse(early.newlySatisfied());
    assertEquals(List.of("later_resource"), ids(early.resources()));
    assertFalse(inputSatisfied(session, "second", "later_collect"));

    var first = session.inspectSource((short) 1, "first", "source").orElseThrow();
    assertTrue(first.newlySatisfied());
    assertEquals(List.of("resource_a", "resource_b"), ids(first.resources()));
    var reopened = session.inspectSource((short) 1, "first", "source").orElseThrow();
    assertFalse(reopened.newlySatisfied());
    assertEquals(List.of("resource_a", "resource_b"), ids(reopened.resources()));
    var preview = session.previewNextHint((short) 1, "first").orElseThrow();
    assertEquals(HintSeverity.APPROACH, preview.severity());
    assertTrue(riddleReleasedHints(session, "first").isEmpty());
    assertEquals(
        "released text",
        session.confirmNextHint((short) 1, "first", preview.id()).orElseThrow().text());

    session.enterNumericCode((short) 1, "first", "first_code", "12");
    assertFalse(inputSatisfied(session, "second", "later_collect"));
    assertTrue(
        session.inspectSource((short) 1, "second", "later_source").orElseThrow().newlySatisfied());
  }

  @Test
  void answersStayOutOfProjectionAndExitSuccessUsesCurrentlyReadyPlayers() {
    MultiplayerSession session = session(2, 2);
    session.reconcileClients(List.of(observation(1), observation(2)));
    session.completeIntro((short) 1);
    session.completeIntro((short) 2);
    session.inspectSource((short) 1, "first", "source");
    assertEquals(
        CodeOutcome.CORRECT,
        session.enterNumericCode((short) 2, "first", "first_code", "12").orElseThrow().outcome());
    session.inspectSource((short) 1, "second", "later_source");
    session.enterNumericCode((short) 2, "second", "final_code", "2468");
    assertFalse(session.projection().toString().contains("2468"));
    assertTrue(session.projection().doorOpen());

    session.reconcileExitPresence(Set.of("slot_1"));
    session.reconcileClients(List.of(observation(1)));
    session.reconcileExitPresence(Set.of("slot_1"));
    assertEquals(Optional.of(TerminalResult.SUCCESS), session.projection().terminal());
  }

  @Test
  void rejectsPresentationWithDifferentAuthoredSurfaceIdentity() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new MultiplayerSession(definition(1, 1), presentation("wrong_surface")));
  }

  @Test
  void rejectsPresentationWithDifferentSourceResourceOrder() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new MultiplayerSession(
                definition(1, 1),
                presentation(
                    "surface_source", List.of(resource("resource_b"), resource("resource_a")))));
  }

  private static MultiplayerSession startedSession() {
    MultiplayerSession session = session(1, 2);
    session.reconcileClients(List.of(observation(1)));
    session.completeIntro((short) 1);
    return session;
  }

  private static MultiplayerSession session(final int minimum, final int maximum) {
    return new MultiplayerSession(definition(minimum, maximum), presentation());
  }

  private static ClientObservation observation(final int clientId) {
    return new ClientObservation((short) clientId, clientId * 100);
  }

  private static RoomDefinition definition(final int minimum, final int maximum) {
    List<RosterSlotDefinition> slots =
        IntStream.rangeClosed(1, maximum)
            .mapToObj(index -> new RosterSlotDefinition("slot_" + index, index))
            .toList();
    ComposedRiddleDefinition first =
        new ComposedRiddleDefinition(
            "first",
            List.of(
                new InformationSourceDefinition(
                    "source", "surface_source", List.of("resource_a", "resource_b"))),
            List.of(
                new CollectionInputDefinition("collect", "source"),
                new NumericInputDefinition("first_code", "surface_first_code", "12", true)),
            List.of(new HintDefinition("hint", "Hint", "released text", HintSeverity.APPROACH)));
    ComposedRiddleDefinition second =
        new ComposedRiddleDefinition(
            "second",
            List.of(
                new InformationSourceDefinition(
                    "later_source", "surface_later", List.of("later_resource"))),
            List.of(
                new CollectionInputDefinition("later_collect", "later_source"),
                new NumericInputDefinition("final_code", "surface_final_code", "2468", true)),
            List.of());
    return new RoomDefinition(
        "room",
        minimum,
        new RosterDefinition(slots),
        List.of(
            new SectionDefinition("first_section", List.of(first)),
            new SectionDefinition("second_section", List.of(second))),
        new TimerDefinition(10, TimerMode.HARD),
        new DoorDefinition("door"),
        new ExitDefinition("exit", "door"));
  }

  private static GamePresentation presentation() {
    return presentation("surface_source");
  }

  private static GamePresentation presentation(final String sourceSurfaceId) {
    return presentation(sourceSurfaceId, List.of(resource("resource_a"), resource("resource_b")));
  }

  private static GamePresentation presentation(
      final String sourceSurfaceId, final List<ResourcePresentation> sourceResources) {
    return new GamePresentation(
        List.of(
            new ComposedPresentation(
                "first",
                List.of(
                    new InformationSourcePresentation(
                        "source", sourceSurfaceId, "images/open-book.png", sourceResources)),
                List.of(
                    new NumericInputPresentation(
                        "first_code", "surface_first_code", "images/open-book.png"))),
            new ComposedPresentation(
                "second",
                List.of(
                    new InformationSourcePresentation(
                        "later_source",
                        "surface_later",
                        "images/open-book.png",
                        List.of(resource("later_resource")))),
                List.of(
                    new NumericInputPresentation(
                        "final_code", "surface_final_code", "images/open-book.png")))),
        List.of("Intro"),
        "Mission",
        List.of("Success"),
        Optional.of(List.of("Timeout")));
  }

  private static ResourcePresentation resource(final String id) {
    return new ResourcePresentation(
        id, "Resource " + id, "Text " + id, Optional.of("assets/custom/aaaaaaaaaaaa-room.png"));
  }

  private static List<String> ids(final List<ResourcePresentation> resources) {
    return resources.stream().map(ResourcePresentation::id).toList();
  }

  private static boolean inputSatisfied(
      final MultiplayerSession session, final String riddleId, final String inputId) {
    return session.projection().sections().stream()
        .flatMap(section -> section.riddles().stream())
        .filter(riddle -> riddle.id().equals(riddleId))
        .flatMap(riddle -> riddle.inputs().stream())
        .filter(input -> input.id().equals(inputId))
        .findFirst()
        .orElseThrow()
        .satisfied();
  }

  private static List<ReleasedHint> riddleReleasedHints(
      final MultiplayerSession session, final String riddleId) {
    return session.projection().sections().stream()
        .flatMap(section -> section.riddles().stream())
        .filter(riddle -> riddle.id().equals(riddleId))
        .findFirst()
        .orElseThrow()
        .releasedHints();
  }
}
