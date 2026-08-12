package escaperoom.foundation.definition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Complete immutable definition consumed by reusable Foundation authority.
 *
 * @param id stable room identifier
 * @param minimumPlayers technically ready players required to start
 * @param roster ordered player capacity
 * @param progression exact mandatory AND-DAG progression
 * @param timer shared timer behavior
 * @param door the one controlled common-exit door
 * @param exit the one common exit
 */
public record RoomDefinition(
    String id,
    int minimumPlayers,
    RosterDefinition roster,
    ProgressionDefinition progression,
    TimerDefinition timer,
    DoorDefinition door,
    ExitDefinition exit) {
  /** Creates a complete Foundation room definition and checks aggregate identity invariants. */
  public RoomDefinition {
    id = DefinitionChecks.requireId(id, "room id");
    Objects.requireNonNull(roster, "roster");
    if (minimumPlayers < 1 || minimumPlayers > roster.slots().size()) {
      throw new IllegalArgumentException("minimum players must be within the roster capacity");
    }
    Objects.requireNonNull(progression, "progression");
    Objects.requireNonNull(timer, "timer");
    Objects.requireNonNull(door, "door");
    Objects.requireNonNull(exit, "exit");
    if (!exit.doorId().equals(door.id())) {
      throw new IllegalArgumentException("common exit must reference the room door");
    }
    if (!exit.id().equals(progression.endNodeId())) {
      throw new IllegalArgumentException("common exit id must equal the progression end node id");
    }
    validateEntityIds(progression, door, exit);
  }

  private static void validateEntityIds(
      final ProgressionDefinition progression,
      final DoorDefinition door,
      final ExitDefinition exit) {
    Map<String, String> owners = new LinkedHashMap<>();
    for (ProgressionDefinition.RiddleNode node : progression.riddleNodes()) {
      ComposedRiddleDefinition riddle = node.riddle();
      register(owners, riddle.id(), "riddle");
      riddle.hints().forEach(hint -> register(owners, hint.id(), "escapeRoom/hint"));
      riddle
          .informationSources()
          .forEach(
              source -> {
                register(owners, source.id(), "information source");
                source.resourceIds().forEach(resource -> register(owners, resource, "resource"));
              });
      riddle.inputs().forEach(input -> register(owners, input.id(), "input"));
    }
    register(owners, door.id(), "door");
    register(owners, exit.id(), "exit");
  }

  private static void register(
      final Map<String, String> owners, final String id, final String owner) {
    String previous = owners.putIfAbsent(id, owner);
    if (previous != null) {
      throw new IllegalArgumentException(
          "definition id '" + id + "' is shared by " + previous + " and " + owner);
    }
  }
}
