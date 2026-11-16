package feature.leveleditor;

import engine.utils.Point;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabWiring;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Places the prefabs without a world position, e.g. dialogs or sound effects, next to the prefabs
 * they are linked to.
 *
 * <p>Such a prefab is attached to a host: preferably a prefab whose event runs one of its actions,
 * otherwise a prefab it sends events to, then a prefab it feeds artifacts to, and finally a prefab
 * feeding artifacts into it. Hosts can be attached prefabs themselves, so chains like Trigger →
 * Delay → Sound Effect are shown as an indented list next to the world prefab they start at.
 * Prefabs that are not linked to any prefab in the world, directly or through other attached
 * prefabs, are not placed.
 *
 * <p>The layout only decides the structure: which world prefab a chain hangs from, and the indent
 * and row of every attached prefab in it. The editor turns that into screen positions, so the list
 * keeps its size in pixels at every zoom level.
 *
 * <p>The prefab list of the editor nests attached prefabs below their hosts the same way, see
 * {@link #hosts}.
 */
final class AttachedPrefabLayout {

  private static final int TIERS = 4;

  /**
   * Place of one attached prefab in the list next to its world prefab.
   *
   * @param name instance name
   * @param host name of the instance it is attached to
   * @param root name of the world instance the chain of hosts starts at
   * @param depth indent level, 0 for prefabs attached to the world instance itself
   * @param row row in the list of the world instance, starting at 0
   */
  record Chip(String name, String host, String root, int depth, int row) {}

  /**
   * Hosts of all attached prefabs, also of the ones not linked to a prefab in the world.
   *
   * @param hosts host by instance name, missing for world prefabs and for attached prefabs that
   *     start a chain without a world prefab
   * @param users the prefabs using an attached prefab, by its name: the prefabs whose events run
   *     one of its actions and the prefabs it feeds artifacts to, in load order
   */
  record Hosts(Map<String, String> hosts, Map<String, List<String>> users) {}

  private AttachedPrefabLayout() {}

  /**
   * Places the instances without a world position.
   *
   * @param instances instances of the level, in load order
   * @param connections links between the instances, see {@link PrefabWiring#connections}
   * @param worldAnchor returns the link anchor of an instance with a world position, or empty for
   *     an instance without one
   * @return placed instances by name, in placement order
   */
  static Map<String, Chip> layout(
      List<PrefabInstance> instances,
      List<PrefabWiring.Connection> connections,
      Function<PrefabInstance, Optional<Point>> worldAnchor) {
    Map<String, Integer> order = new HashMap<>();
    Set<String> world = new HashSet<>();
    List<String> attached = new ArrayList<>();
    for (PrefabInstance instance : instances) {
      order.put(instance.name(), order.size());
      Optional<Point> anchor = worldAnchor.apply(instance);
      if (anchor.isPresent()) world.add(instance.name());
      else attached.add(instance.name());
    }
    if (attached.isEmpty()) return Map.of();

    Map<String, String> hosts = new LinkedHashMap<>();
    attach(attached, candidates(connections, attached), world, hosts);

    Map<String, List<String>> children = new HashMap<>();
    for (Map.Entry<String, String> entry : hosts.entrySet()) {
      children.computeIfAbsent(entry.getValue(), ignored -> new ArrayList<>()).add(entry.getKey());
    }
    for (List<String> list : children.values()) {
      list.sort((a, b) -> Integer.compare(order.get(a), order.get(b)));
    }
    Map<String, Chip> result = new LinkedHashMap<>();
    for (PrefabInstance instance : instances) {
      if (!world.contains(instance.name())) continue;
      int[] row = {0};
      place(instance.name(), instance.name(), 0, row, children, result);
    }
    return result;
  }

  private static void place(
      String host,
      String root,
      int depth,
      int[] row,
      Map<String, List<String>> children,
      Map<String, Chip> result) {
    for (String child : children.getOrDefault(host, List.of())) {
      result.put(child, new Chip(child, host, root, depth, row[0]));
      row[0]++;
      place(child, root, depth + 1, row, children, result);
    }
  }

  /**
   * Finds the hosts of all attached prefabs, like {@link #layout} does for the ones linked to a
   * prefab in the world. The other attached prefabs form chains of their own: a prefab that no
   * other attached prefab runs an action of starts a chain, or, in a loop of such prefabs, the
   * first one in load order. Their chains are built like the ones of the world prefabs.
   *
   * @param instances instances of the level, in load order
   * @param connections links between the instances, see {@link PrefabWiring#connections}
   * @param inWorld whether an instance has a world position
   * @return hosts and users of the attached prefabs
   */
  static Hosts hosts(
      List<PrefabInstance> instances,
      List<PrefabWiring.Connection> connections,
      Predicate<PrefabInstance> inWorld) {
    Set<String> placed = new HashSet<>();
    List<String> attached = new ArrayList<>();
    for (PrefabInstance instance : instances) {
      if (inWorld.test(instance)) placed.add(instance.name());
      else attached.add(instance.name());
    }
    Map<String, List<List<String>>> candidates = candidates(connections, attached);
    Map<String, String> hosts = new LinkedHashMap<>();
    attach(attached, candidates, placed, hosts);
    while (true) {
      List<String> remaining =
          attached.stream()
              .filter(name -> !hosts.containsKey(name) && !placed.contains(name))
              .toList();
      if (remaining.isEmpty()) break;
      placed.add(
          remaining.stream()
              .filter(name -> candidates.get(name).getFirst().isEmpty())
              .findFirst()
              .orElse(remaining.getFirst()));
      attach(attached, candidates, placed, hosts);
    }

    Map<String, List<String>> users = new HashMap<>();
    for (String name : attached) {
      List<List<String>> tiers = candidates.get(name);
      List<String> using = new ArrayList<>(tiers.get(0));
      using.addAll(tiers.get(2));
      if (!using.isEmpty()) users.put(name, using.stream().distinct().toList());
    }
    return new Hosts(hosts, users);
  }

  /**
   * Gives every attached prefab without a host the best placed candidate as its host, tier by tier,
   * until no more hosts are found.
   *
   * @param attached attached prefabs in load order
   * @param candidates possible hosts by tier, see {@link #candidates}
   * @param placed prefabs that start a chain, e.g. the world prefabs
   * @param hosts found hosts, extended in place
   */
  private static void attach(
      List<String> attached,
      Map<String, List<List<String>>> candidates,
      Set<String> placed,
      Map<String, String> hosts) {
    for (int maxTier = 0; maxTier < TIERS; maxTier++) {
      boolean changed = true;
      while (changed) {
        changed = false;
        for (String name : attached) {
          if (hosts.containsKey(name) || placed.contains(name)) continue;
          Optional<String> host = placedHost(candidates.get(name), maxTier, placed, hosts);
          if (host.isEmpty()) continue;
          hosts.put(name, host.get());
          changed = true;
        }
      }
    }
  }

  private static Optional<String> placedHost(
      List<List<String>> tiers, int maxTier, Set<String> placed, Map<String, String> hosts) {
    for (int tier = 0; tier <= maxTier; tier++) {
      for (String candidate : tiers.get(tier)) {
        if (placed.contains(candidate) || hosts.containsKey(candidate)) {
          return Optional.of(candidate);
        }
      }
    }
    return Optional.empty();
  }

  /* Lists the possible hosts of every attached instance, by preference tier. */
  private static Map<String, List<List<String>>> candidates(
      List<PrefabWiring.Connection> connections, List<String> attached) {
    Map<String, List<List<String>>> result = new HashMap<>();
    for (String name : attached) {
      List<List<String>> tiers = new ArrayList<>(TIERS);
      for (int tier = 0; tier < TIERS; tier++) tiers.add(new ArrayList<>());
      result.put(name, tiers);
    }
    for (PrefabWiring.Connection connection : connections) {
      String source = connection.link().source();
      String receiver = connection.receiver().name();
      if (source.equals(receiver)) continue;
      boolean signal = connection.input().kind().isSignal();
      List<List<String>> receiverTiers = result.get(receiver);
      if (receiverTiers != null) receiverTiers.get(signal ? 0 : 3).add(source);
      List<List<String>> sourceTiers = result.get(source);
      if (sourceTiers != null) sourceTiers.get(signal ? 1 : 2).add(receiver);
    }
    return result;
  }
}
