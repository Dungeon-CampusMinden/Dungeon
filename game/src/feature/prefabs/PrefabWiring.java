package feature.prefabs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Level-wide rules for the links between prefab instances.
 *
 * <p>Links are stored on the receiving instance (see {@link PrefabInstance#inputs()}), so most
 * questions about them, like "which actions does this event trigger", need all instances of a
 * level. The methods accept unnormalized instances and ignore unknown prefab types.
 */
public final class PrefabWiring {

  private PrefabWiring() {}

  /** Severity of a wiring problem. */
  public enum Severity {
    /** The link works, but probably not as intended. */
    WARNING,
    /** The link cannot work and is ignored at runtime. */
    ERROR
  }

  /**
   * One existing link.
   *
   * @param receiver instance storing the link
   * @param input input port of the receiver
   * @param link the link
   */
  public record Connection(PrefabInstance receiver, PrefabPort input, PrefabLink link) {}

  /**
   * A problem of one link.
   *
   * @param instance name of the receiving instance
   * @param port key of the receiving input port
   * @param link the affected link
   * @param severity problem severity
   * @param message editor-facing description
   */
  public record Problem(
      String instance, String port, PrefabLink link, Severity severity, String message) {}

  /**
   * Checks whether an output port can be linked to an input port.
   *
   * @param output output port
   * @param input input port
   * @return empty if compatible, otherwise the reason why not
   */
  public static Optional<String> incompatibility(PrefabPort output, PrefabPort input) {
    if (output.kind().isInput()) return Optional.of("'" + output.displayName() + "' is no output");
    if (!input.kind().isInput()) return Optional.of("'" + input.displayName() + "' is no input");
    if (output.kind().isSignal() != input.kind().isSignal()) {
      return Optional.of(
          output.kind().isSignal()
              ? "Events can only be linked to actions"
              : "Artifacts can only be linked to artifact slots");
    }
    if (output instanceof ArtifactOutput<?> artifact
        && input instanceof ArtifactSlot<?> slot
        && !artifact.type().convertibleTo(slot.type())) {
      return Optional.of(
          artifact.type().displayName()
              + " values cannot be used as "
              + slot.type().displayName()
              + " values");
    }
    return Optional.empty();
  }

  /**
   * Checks whether an output port can be linked to an input port.
   *
   * @param output output port
   * @param input input port
   * @return true if compatible
   */
  public static boolean compatible(PrefabPort output, PrefabPort input) {
    return incompatibility(output, input).isEmpty();
  }

  /**
   * Returns whether a link would make an event run an action that needs a player without one.
   *
   * @param output output port
   * @param input input port
   * @return true if the action would run for every player instead
   */
  public static boolean lacksPlayer(PrefabPort output, PrefabPort input) {
    return output instanceof PrefabEvent<?> event
        && input instanceof PrefabAction action
        && action.needsPlayer()
        && !event.carriesPlayer();
  }

  /**
   * Resolves the output port a link points at.
   *
   * @param instances instances of the level
   * @param link link to resolve
   * @return the output port, if the source instance exists and offers the port
   */
  public static Optional<PrefabPort> outputPort(
      Collection<PrefabInstance> instances, PrefabLink link) {
    for (PrefabInstance instance : instances) {
      if (instance.name().equals(link.source())) {
        return PrefabRegistry.find(instance.type())
            .flatMap(prefab -> prefab.port(instance, link.port()))
            .filter(port -> !port.kind().isInput());
      }
    }
    return Optional.empty();
  }

  /**
   * Returns every link pointing at an output of an instance.
   *
   * @param instances instances of the level
   * @param source name of the source instance
   * @return links of all receivers, in level order
   */
  public static List<Connection> outgoing(List<PrefabInstance> instances, String source) {
    List<Connection> result = new ArrayList<>();
    for (Connection connection : connections(instances)) {
      if (connection.link().source().equals(source)) result.add(connection);
    }
    return result;
  }

  /**
   * Returns every link pointing at one output port.
   *
   * @param instances instances of the level
   * @param source name of the source instance
   * @param port key of the output port
   * @return links of all receivers, in level order
   */
  public static List<Connection> outgoing(
      List<PrefabInstance> instances, String source, String port) {
    return outgoing(instances, source).stream()
        .filter(connection -> connection.link().port().equals(port))
        .toList();
  }

  /**
   * Returns every link stored on the instances whose input port is known and offered.
   *
   * @param instances instances of the level
   * @return all links, in level and port order
   */
  public static List<Connection> connections(List<PrefabInstance> instances) {
    List<Connection> result = new ArrayList<>();
    for (PrefabInstance receiver : instances) {
      Optional<Prefab> prefab = PrefabRegistry.find(receiver.type());
      if (prefab.isEmpty()) continue;
      for (Map.Entry<String, List<PrefabLink>> input : receiver.inputs().entrySet()) {
        Optional<PrefabPort> port = prefab.get().port(receiver, input.getKey());
        if (port.isEmpty() || !port.get().kind().isInput()) continue;
        for (PrefabLink link : input.getValue()) {
          result.add(new Connection(receiver, port.get(), link));
        }
      }
    }
    return result;
  }

  /**
   * Finds the problems of every link of a level.
   *
   * @param instances instances of the level
   * @return problems, in level and port order
   */
  public static List<Problem> problems(List<PrefabInstance> instances) {
    Map<String, PrefabInstance> byName = new HashMap<>();
    for (PrefabInstance instance : instances) byName.put(instance.name(), instance);
    Set<Connection> cyclic = cyclicArtifactLinks(instances);
    List<Problem> result = new ArrayList<>();
    for (Connection connection : connections(instances)) {
      PrefabLink link = connection.link();
      String receiver = connection.receiver().name();
      String port = connection.input().key();
      PrefabInstance source = byName.get(link.source());
      if (source == null) {
        result.add(
            new Problem(
                receiver,
                port,
                link,
                Severity.ERROR,
                "Linked prefab '" + link.source() + "' does not exist"));
        continue;
      }
      Optional<PrefabPort> output = outputPort(instances, link);
      if (output.isEmpty()) {
        result.add(
            new Problem(
                receiver,
                port,
                link,
                Severity.ERROR,
                "'" + link.source() + "' has no output '" + link.port() + "'"));
        continue;
      }
      Optional<String> incompatibility = incompatibility(output.get(), connection.input());
      if (incompatibility.isPresent()) {
        result.add(new Problem(receiver, port, link, Severity.ERROR, incompatibility.get()));
        continue;
      }
      if (cyclic.contains(connection)) {
        result.add(
            new Problem(receiver, port, link, Severity.ERROR, "Artifact links form a cycle"));
        continue;
      }
      if (lacksPlayer(output.get(), connection.input())) {
        result.add(
            new Problem(
                receiver,
                port,
                link,
                Severity.WARNING,
                "'"
                    + output.get().displayName()
                    + "' has no player, so '"
                    + connection.input().displayName()
                    + "' runs for every player"));
      }
      if (output.get().kind() == PrefabPort.Kind.ARTIFACT_OUTPUT && !source.active()) {
        result.add(
            new Problem(
                receiver,
                port,
                link,
                Severity.WARNING,
                "'"
                    + link.source()
                    + "' is inactive at start, so its value is missing until it is activated"));
      }
    }
    return result;
  }

  /**
   * Returns the links pointing at outputs of an instance that its type has, but the instance does
   * not offer, see {@link Prefab#isPortAvailable}.
   *
   * @param instances instances of the level
   * @param source name of the source instance
   * @return links to unavailable outputs, in level order
   */
  public static List<Connection> linksToUnavailableOutputs(
      List<PrefabInstance> instances, String source) {
    Optional<PrefabInstance> instance =
        instances.stream().filter(i -> i.name().equals(source)).findFirst();
    Optional<Prefab> prefab = instance.flatMap(i -> PrefabRegistry.find(i.type()));
    if (prefab.isEmpty()) return List.of();
    return outgoing(instances, source).stream()
        .filter(
            connection ->
                prefab.get().port(connection.link().port()).isPresent()
                    && prefab.get().port(instance.get(), connection.link().port()).isEmpty())
        .toList();
  }

  /**
   * Removes the links pointing at outputs an instance no longer offers, e.g. after the list the
   * outputs belong to was shortened.
   *
   * @param instances instances of the level
   * @param source name of the source instance
   * @return instances without these links, in the same order
   */
  public static List<PrefabInstance> withoutLinksToUnavailableOutputs(
      List<PrefabInstance> instances, String source) {
    Map<String, Set<PrefabLink>> removed = new HashMap<>();
    for (Connection connection : linksToUnavailableOutputs(instances, source)) {
      removed
          .computeIfAbsent(
              connection.receiver().name() + "\n" + connection.input().key(),
              ignored -> new HashSet<>())
          .add(connection.link());
    }
    if (removed.isEmpty()) return instances;
    List<PrefabInstance> result = new ArrayList<>(instances.size());
    for (PrefabInstance instance : instances) {
      Map<String, List<PrefabLink>> inputs = new LinkedHashMap<>();
      instance
          .inputs()
          .forEach(
              (port, links) -> {
                Set<PrefabLink> dropped =
                    removed.getOrDefault(instance.name() + "\n" + port, Set.of());
                inputs.put(port, links.stream().filter(link -> !dropped.contains(link)).toList());
              });
      result.add(instance.withInputs(inputs));
    }
    return result;
  }

  /**
   * Removes every link that cannot work, see {@link Severity#ERROR}.
   *
   * @param instances instances of the level
   * @param warnings receives a description of each removed link
   * @return instances without broken links, in the same order
   */
  public static List<PrefabInstance> withoutBrokenLinks(
      List<PrefabInstance> instances, Consumer<String> warnings) {
    Map<String, Set<PrefabLink>> broken = new HashMap<>();
    for (Problem problem : problems(instances)) {
      if (problem.severity() != Severity.ERROR) continue;
      warnings.accept(
          "Removing link "
              + problem.link()
              + " -> "
              + problem.instance()
              + "."
              + problem.port()
              + ": "
              + problem.message());
      broken
          .computeIfAbsent(problem.instance() + "\n" + problem.port(), ignored -> new HashSet<>())
          .add(problem.link());
    }
    if (broken.isEmpty()) return instances;
    List<PrefabInstance> result = new ArrayList<>(instances.size());
    for (PrefabInstance instance : instances) {
      Map<String, List<PrefabLink>> inputs = new LinkedHashMap<>();
      instance
          .inputs()
          .forEach(
              (port, links) -> {
                Set<PrefabLink> removed =
                    broken.getOrDefault(instance.name() + "\n" + port, Set.of());
                inputs.put(port, links.stream().filter(link -> !removed.contains(link)).toList());
              });
      result.add(instance.withInputs(inputs));
    }
    return result;
  }

  /**
   * Updates links after instances were renamed.
   *
   * @param instances instances of the level, already carrying their new names
   * @param renamed new names by old name
   * @return instances whose links point at the new names
   */
  public static List<PrefabInstance> renameSources(
      List<PrefabInstance> instances, Map<String, String> renamed) {
    if (renamed.isEmpty()) return instances;
    return mapLinks(
        instances,
        link -> {
          String newName = renamed.get(link.source());
          return Optional.of(newName == null ? link : link.withSource(newName));
        });
  }

  /**
   * Counts the links pointing at outputs of the given instances, ignoring links between them.
   *
   * @param instances instances of the level
   * @param sources names of the source instances
   * @return number of links stored on other instances
   */
  public static int countLinksTo(List<PrefabInstance> instances, Set<String> sources) {
    int count = 0;
    for (Connection connection : connections(instances)) {
      if (sources.contains(connection.link().source())
          && !sources.contains(connection.receiver().name())) {
        count++;
      }
    }
    return count;
  }

  /**
   * Removes every link pointing at outputs of the given instances.
   *
   * @param instances instances of the level
   * @param sources names of the source instances
   * @return instances without these links
   */
  public static List<PrefabInstance> removeLinksTo(
      List<PrefabInstance> instances, Set<String> sources) {
    return mapLinks(
        instances, link -> sources.contains(link.source()) ? Optional.empty() : Optional.of(link));
  }

  /**
   * Checks whether a new artifact link would make artifact values depend on themselves.
   *
   * @param instances instances of the level
   * @param source name of the producing instance
   * @param receiver name of the consuming instance
   * @return true if the link would close a cycle
   */
  public static boolean wouldCreateArtifactCycle(
      List<PrefabInstance> instances, String source, String receiver) {
    if (source.equals(receiver)) return true;
    Map<String, Set<String>> consumers = artifactConsumers(instances);
    Deque<String> open = new ArrayDeque<>(List.of(receiver));
    Set<String> seen = new HashSet<>();
    while (!open.isEmpty()) {
      String current = open.pop();
      if (current.equals(source)) return true;
      if (seen.add(current)) open.addAll(consumers.getOrDefault(current, Set.of()));
    }
    return false;
  }

  /**
   * Orders instances so that artifact producers come before their consumers, keeping the given
   * order otherwise. Instances in artifact cycles keep their relative order at the end.
   *
   * @param instances instances in load order
   * @return instances in spawn order
   */
  public static List<PrefabInstance> spawnOrder(List<PrefabInstance> instances) {
    Map<String, Set<String>> producers = new HashMap<>();
    Set<String> names = new HashSet<>();
    for (PrefabInstance instance : instances) names.add(instance.name());
    for (Connection connection : connections(instances)) {
      if (connection.input().kind() != PrefabPort.Kind.ARTIFACT_SLOT) continue;
      if (!names.contains(connection.link().source())) continue;
      producers
          .computeIfAbsent(connection.receiver().name(), ignored -> new HashSet<>())
          .add(connection.link().source());
    }
    if (producers.isEmpty()) return instances;
    List<PrefabInstance> remaining = new ArrayList<>(instances);
    List<PrefabInstance> result = new ArrayList<>(instances.size());
    Set<String> placed = new HashSet<>();
    boolean progress = true;
    while (!remaining.isEmpty() && progress) {
      progress = false;
      for (int index = 0; index < remaining.size(); index++) {
        PrefabInstance candidate = remaining.get(index);
        if (placed.containsAll(producers.getOrDefault(candidate.name(), Set.of()))) {
          result.add(candidate);
          placed.add(candidate.name());
          remaining.remove(index);
          progress = true;
          break;
        }
      }
    }
    result.addAll(remaining);
    return result;
  }

  /**
   * Derives the default alias of a value in a multi-value artifact slot from its source.
   *
   * @param source name of the source instance
   * @return alias made of the last name segment
   */
  public static String defaultAlias(String source) {
    String segment = source.substring(source.lastIndexOf('/') + 1);
    String alias = segment.replaceAll("[^A-Za-z0-9_-]", "_");
    return alias.isEmpty() ? "value" : alias;
  }

  /**
   * Makes an alias unique by appending a number.
   *
   * @param alias wanted alias
   * @param taken aliases already in use
   * @return the alias, or the alias with the lowest free suffix
   */
  public static String uniqueAlias(String alias, Set<String> taken) {
    if (!taken.contains(alias)) return alias;
    int suffix = 2;
    while (taken.contains(alias + "_" + suffix)) suffix++;
    return alias + "_" + suffix;
  }

  private static List<PrefabInstance> mapLinks(
      List<PrefabInstance> instances, Function<PrefabLink, Optional<PrefabLink>> mapping) {
    List<PrefabInstance> result = new ArrayList<>(instances.size());
    for (PrefabInstance instance : instances) {
      Map<String, List<PrefabLink>> inputs = new LinkedHashMap<>();
      instance
          .inputs()
          .forEach(
              (port, links) ->
                  inputs.put(port, links.stream().map(mapping).flatMap(Optional::stream).toList()));
      PrefabInstance mapped = instance.withInputs(inputs);
      result.add(mapped.equals(instance) ? instance : mapped);
    }
    return result;
  }

  private static Map<String, Set<String>> artifactConsumers(List<PrefabInstance> instances) {
    Map<String, Set<String>> consumers = new HashMap<>();
    for (Connection connection : connections(instances)) {
      if (connection.input().kind() == PrefabPort.Kind.ARTIFACT_SLOT) {
        consumers
            .computeIfAbsent(connection.link().source(), ignored -> new LinkedHashSet<>())
            .add(connection.receiver().name());
      }
    }
    return consumers;
  }

  private static Set<Connection> cyclicArtifactLinks(List<PrefabInstance> instances) {
    Set<Connection> result = new HashSet<>();
    Map<String, Set<String>> consumers = artifactConsumers(instances);
    for (Connection connection : connections(instances)) {
      if (connection.input().kind() != PrefabPort.Kind.ARTIFACT_SLOT) continue;
      String receiver = connection.receiver().name();
      String source = connection.link().source();
      // The link is part of a cycle if its source can be reached from its receiver.
      Deque<String> open = new ArrayDeque<>(List.of(receiver));
      Set<String> seen = new HashSet<>();
      while (!open.isEmpty()) {
        String current = open.pop();
        if (current.equals(source)) {
          result.add(connection);
          break;
        }
        if (seen.add(current)) open.addAll(consumers.getOrDefault(current, Set.of()));
      }
    }
    return result;
  }
}
