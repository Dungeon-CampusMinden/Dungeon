package feature.prefabs;

import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Finds the problems of the prefab instances of a level for the level editor.
 *
 * <p>Every instance is checked for invalid settings, points outside of the level, regions without
 * an area, unknown placeholders, broken links and actions that need a player but are triggered
 * without one. Warnings point out instances that never do anything: inactive ones nothing
 * activates, and ones whose actions nothing runs or whose outputs nothing uses, see {@link
 * Prefab#actionsToRun} and {@link Prefab#outputsToUse}. The prefab types add their own problems,
 * see {@link Prefab#problems}.
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
    Set<String> usedOutputs = new HashSet<>();
    for (PrefabWiring.Connection connection : PrefabWiring.connections(instances)) {
      usedOutputs.add(output(connection.link().source(), connection.link().port()));
    }
    List<PrefabProblem> result = new ArrayList<>();
    for (PrefabInstance instance : instances) {
      result.addAll(instanceProblems(level, instance, usedOutputs));
    }
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

  private static List<PrefabProblem> instanceProblems(
      ILevel level, PrefabInstance instance, Set<String> usedOutputs) {
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
    result.addAll(positionProblems(level, prefab.get(), normalized));
    result.addAll(unknownPlaceholders(prefab.get(), normalized));
    result.addAll(unusedProblems(prefab.get(), normalized, usedOutputs));
    try {
      result.addAll(prefab.get().problems(level, normalized));
    } catch (RuntimeException failed) {
      LOGGER.warn("Checking prefab '" + instance.name() + "' failed", failed);
    }
    return result;
  }

  /*
   * A point on no tile at all cannot be reached, e.g. after the level was made smaller, and a region
   * without an area covers nothing.
   */
  private static List<PrefabProblem> positionProblems(
      ILevel level, Prefab prefab, PrefabInstance instance) {
    List<PrefabProblem> result = new ArrayList<>();
    for (PrefabProperty<?> property : prefab.visibleProperties(instance)) {
      String name = "'" + property.displayName() + "'";
      if (property.type() == PrefabPropertyType.POINT
          && level.tileAt((Point) property.get(instance)).isEmpty()) {
        result.add(PrefabProblem.error(instance.name(), name + " is outside of the level"));
      } else if (property.type() == PrefabPropertyType.REGION
          && hasNoArea((Region) property.get(instance))) {
        result.add(PrefabProblem.error(instance.name(), name + " has no area"));
      }
    }
    return result;
  }

  private static boolean hasNoArea(Region region) {
    return region.bottomLeft().x() == region.topRight().x()
        || region.bottomLeft().y() == region.topRight().y();
  }

  // Instances that never do anything.
  private static List<PrefabProblem> unusedProblems(
      Prefab prefab, PrefabInstance instance, Set<String> usedOutputs) {
    List<PrefabProblem> result = new ArrayList<>();
    if (!instance.active() && instance.links(Prefab.ACTIVATE).isEmpty()) {
      result.add(
          PrefabProblem.warning(instance.name(), "Inactive at the start and nothing activates it"));
    }
    List<PrefabAction> actions = prefab.actionsToRun(instance);
    if (!actions.isEmpty()
        && actions.stream().allMatch(action -> instance.links(action).isEmpty())) {
      result.add(
          PrefabProblem.warning(
              instance.name(), "Nothing runs " + portNames(prefab, instance, actions, "actions")));
    }
    List<PrefabPort> outputs = prefab.outputsToUse(instance);
    if (!outputs.isEmpty()
        && outputs.stream()
            .noneMatch(port -> usedOutputs.contains(output(instance.name(), port.key())))) {
      result.add(
          PrefabProblem.warning(
              instance.name(),
              "Nothing is linked to " + portNames(prefab, instance, outputs, "outputs")));
    }
    return result;
  }

  // The names of a few ports, like "'Open', 'Close' or 'Toggle'", else e.g. "any of its outputs".
  private static String portNames(
      Prefab prefab, PrefabInstance instance, List<? extends PrefabPort> ports, String kind) {
    if (ports.size() > 3) return "any of its " + kind;
    List<String> names =
        ports.stream().map(port -> "'" + prefab.portDisplayName(instance, port) + "'").toList();
    if (names.size() == 1) return names.getFirst();
    return String.join(", ", names.subList(0, names.size() - 1)) + " or " + names.getLast();
  }

  private static String output(String instance, String port) {
    return instance + "\n" + port;
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
