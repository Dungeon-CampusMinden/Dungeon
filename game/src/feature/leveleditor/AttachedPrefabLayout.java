package feature.leveleditor;

import engine.utils.Point;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabWiring;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

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
 */
final class AttachedPrefabLayout {

  private static final float OFFSET_X = 0.7f;
  private static final float INDENT = 0.5f;
  private static final float ROW_HEIGHT = 0.42f;
  private static final int TIERS = 4;

  /**
   * Position of one attached prefab.
   *
   * @param name instance name
   * @param host name of the instance it is attached to
   * @param root name of the world instance the chain of hosts starts at
   * @param position world position of its marker
   */
  record Chip(String name, String host, String root, Point position) {}

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
    Map<String, Point> world = new HashMap<>();
    List<String> attached = new ArrayList<>();
    for (PrefabInstance instance : instances) {
      order.put(instance.name(), order.size());
      Optional<Point> anchor = worldAnchor.apply(instance);
      if (anchor.isPresent()) world.put(instance.name(), anchor.get());
      else attached.add(instance.name());
    }
    if (attached.isEmpty()) return Map.of();

    Map<String, List<List<String>>> candidates = candidates(connections, attached);
    Map<String, String> hosts = new LinkedHashMap<>();
    for (int maxTier = 0; maxTier < TIERS; maxTier++) {
      boolean changed = true;
      while (changed) {
        changed = false;
        for (String name : attached) {
          if (hosts.containsKey(name)) continue;
          Optional<String> host = placedHost(candidates.get(name), maxTier, world, hosts);
          if (host.isEmpty()) continue;
          hosts.put(name, host.get());
          changed = true;
        }
      }
    }

    Map<String, List<String>> children = new HashMap<>();
    for (Map.Entry<String, String> entry : hosts.entrySet()) {
      children.computeIfAbsent(entry.getValue(), ignored -> new ArrayList<>()).add(entry.getKey());
    }
    for (List<String> list : children.values()) {
      list.sort((a, b) -> Integer.compare(order.get(a), order.get(b)));
    }
    Map<String, Chip> result = new LinkedHashMap<>();
    for (PrefabInstance instance : instances) {
      Point anchor = world.get(instance.name());
      if (anchor == null) continue;
      int[] row = {0};
      place(instance.name(), instance.name(), anchor, 0, row, children, result);
    }
    return result;
  }

  private static void place(
      String host,
      String root,
      Point anchor,
      int depth,
      int[] row,
      Map<String, List<String>> children,
      Map<String, Chip> result) {
    for (String child : children.getOrDefault(host, List.of())) {
      Point position = anchor.translate(OFFSET_X + depth * INDENT, -row[0] * ROW_HEIGHT);
      result.put(child, new Chip(child, host, root, position));
      row[0]++;
      place(child, root, anchor, depth + 1, row, children, result);
    }
  }

  private static Optional<String> placedHost(
      List<List<String>> tiers, int maxTier, Map<String, Point> world, Map<String, String> hosts) {
    for (int tier = 0; tier <= maxTier; tier++) {
      for (String candidate : tiers.get(tier)) {
        if (world.containsKey(candidate) || hosts.containsKey(candidate)) {
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
