package feature.prefabs;

import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Finds the problems of the prefab instances of a level for the level editor.
 *
 * <p>Every instance is checked for invalid settings, positions outside of the level, unknown
 * placeholders, broken links and actions that need a player but are triggered without one. The
 * prefab types add their own problems, see {@link Prefab#problems}.
 */
public final class PrefabChecks {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(PrefabChecks.class);

  private PrefabChecks() {}

  /**
   * Finds the problems of all prefab instances of a level, the inactive ones included.
   *
   * @param level level to check
   * @return problems ordered by instance in load order, the problems of an instance itself before
   *     the ones of its links
   */
  public static List<PrefabProblem> check(ILevel level) {
    List<PrefabInstance> instances = level.prefabs();
    List<PrefabProblem> result = new ArrayList<>();
    for (PrefabInstance instance : instances) result.addAll(instanceProblems(level, instance));
    for (PrefabWiring.Problem problem : PrefabWiring.problems(instances)) {
      result.add(
          new PrefabProblem(
              problem.severity() == PrefabWiring.Severity.ERROR
                  ? PrefabProblem.Severity.ERROR
                  : PrefabProblem.Severity.WARNING,
              problem.instance(),
              problem.message(),
              problem.port(),
              problem.link()));
    }
    result.addAll(playerProblems(instances));

    Map<String, Integer> order = new HashMap<>();
    for (PrefabInstance instance : instances) order.putIfAbsent(instance.name(), order.size());
    // The sort is stable, so the problems of an instance keep their order otherwise.
    result.sort(
        Comparator.comparingInt(
                (PrefabProblem problem) -> order.getOrDefault(problem.instance(), order.size()))
            .thenComparing(PrefabProblem::isLinkProblem));
    return List.copyOf(result);
  }

  private static List<PrefabProblem> instanceProblems(ILevel level, PrefabInstance instance) {
    Optional<Prefab> prefab = PrefabRegistry.find(instance.type());
    if (prefab.isEmpty()) {
      return List.of(
          PrefabProblem.error(
              instance.name(), "Unknown prefab type '" + instance.type() + "', it is skipped"));
    }
    PrefabInstance normalized;
    try {
      normalized = prefab.get().normalize(instance);
    } catch (IllegalArgumentException invalid) {
      return List.of(PrefabProblem.error(instance.name(), "Invalid: " + invalid.getMessage()));
    }
    List<PrefabProblem> result = new ArrayList<>();
    result.addAll(outsideOfLevel(level, prefab.get(), normalized));
    result.addAll(unknownPlaceholders(prefab.get(), normalized));
    try {
      result.addAll(prefab.get().problems(level, normalized));
    } catch (RuntimeException failed) {
      LOGGER.warn("Checking prefab '" + instance.name() + "' failed", failed);
    }
    return result;
  }

  // A point on no tile at all cannot be reached, e.g. after the level was made smaller.
  private static List<PrefabProblem> outsideOfLevel(
      ILevel level, Prefab prefab, PrefabInstance instance) {
    List<PrefabProblem> result = new ArrayList<>();
    for (PrefabProperty<?> property : prefab.visibleProperties(instance)) {
      if (property.type() == PrefabPropertyType.POINT
          && level.tileAt((Point) property.get(instance)).isEmpty()) {
        result.add(
            PrefabProblem.error(
                instance.name(), "'" + property.displayName() + "' is outside of the level"));
      }
    }
    return result;
  }

  private static List<PrefabProblem> unknownPlaceholders(Prefab prefab, PrefabInstance instance) {
    List<PrefabProblem> result = new ArrayList<>();
    for (PrefabPort port : prefab.ports(instance, PrefabPort.Kind.ARTIFACT_SLOT)) {
      if (!(port instanceof ArtifactSlot<?> slot) || slot.placeholderProperty().isEmpty()) continue;
      PrefabProperty<String> text = slot.placeholderProperty().get();
      if (!text.isVisible(instance)) continue;
      Set<String> unknown = new LinkedHashSet<>(Placeholders.names(text.get(instance)));
      for (PrefabLink link : instance.links(slot)) unknown.remove(link.alias());
      if (unknown.isEmpty()) continue;
      result.add(
          PrefabProblem.warning(
              instance.name(),
              (unknown.size() == 1 ? "Placeholder " : "Placeholders ")
                  + String.join(" ", unknown.stream().map(name -> "{" + name + "}").toList())
                  + " in '"
                  + text.displayName()
                  + "' "
                  + (unknown.size() == 1 ? "has" : "have")
                  + " no linked value and "
                  + (unknown.size() == 1 ? "is" : "are")
                  + " shown as written"));
    }
    return result;
  }

  private static List<PrefabProblem> playerProblems(List<PrefabInstance> instances) {
    EventPlayers players = EventPlayers.of(instances);
    List<PrefabProblem> result = new ArrayList<>();
    for (PrefabWiring.Connection connection : PrefabWiring.connections(instances)) {
      if (!(connection.input() instanceof PrefabAction action)) continue;
      if (!action.needsPlayer(connection.receiver())) continue;
      PrefabLink link = connection.link();
      EventPlayers.Presence presence = players.of(link);
      if (presence != EventPlayers.Presence.NEVER && presence != EventPlayers.Presence.SOMETIMES) {
        continue;
      }
      String event =
          "'"
              + link.source()
              + " > "
              + PrefabWiring.outputPort(instances, link)
                  .map(PrefabPort::displayName)
                  .orElse(link.port())
              + "'";
      String runs = "'" + action.displayName() + "' runs for every player";
      result.add(
          presence == EventPlayers.Presence.NEVER
              ? new PrefabProblem(
                  PrefabProblem.Severity.ERROR,
                  connection.receiver().name(),
                  event + " never has a player, so " + runs,
                  action.key(),
                  link)
              : new PrefabProblem(
                  PrefabProblem.Severity.WARNING,
                  connection.receiver().name(),
                  event + " does not always have a player, then " + runs,
                  action.key(),
                  link));
    }
    return result;
  }
}
