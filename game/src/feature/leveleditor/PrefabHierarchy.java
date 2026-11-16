package feature.leveleditor;

import feature.prefabs.PrefabGroups;
import feature.prefabs.PrefabInstance;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Tree of the prefab list in the level editor: the groups of a level with their prefabs, and the
 * prefabs without a world position, e.g. dialogs or sound effects, nested below the prefab that
 * uses them.
 *
 * <p>An attached prefab is nested below its host, the same prefab it is listed next to in the
 * world, see {@link AttachedPrefabLayout#hosts}. Further prefabs using it list it as a reference.
 * Hosts never form a loop, so neither does the tree.
 *
 * <p>An attached prefab without a group of its own is in the group of its host. One in another
 * group than its host is listed in its own group, and its host lists it as a reference. Groups can
 * contain further groups, which are listed before the prefabs of the group.
 */
final class PrefabHierarchy {

  /**
   * A prefab in the list.
   *
   * @param instance the instance
   * @param children prefabs nested below it, in load order
   * @param references further prefabs it uses that are listed elsewhere, in load order
   */
  record Node(PrefabInstance instance, List<Node> children, List<PrefabInstance> references) {}

  private final List<String> groups;
  private final Map<String, List<String>> subgroups = new HashMap<>();
  private final Map<String, String> hosts;
  private final Map<String, List<Node>> rootsByGroup = new LinkedHashMap<>();
  private final List<Node> ungrouped = new ArrayList<>();
  private final Map<String, String> groupByName = new HashMap<>();
  private final Map<String, String> parentByName = new HashMap<>();
  private final Map<String, Node> nodes = new HashMap<>();

  private PrefabHierarchy(PrefabGroups groups, Map<String, String> hosts) {
    this.groups = List.copyOf(groups.roots());
    this.hosts = Map.copyOf(hosts);
    for (String group : groups.names()) {
      rootsByGroup.put(group, new ArrayList<>());
      subgroups.put(group, List.copyOf(groups.children(group)));
    }
  }

  /**
   * Returns a tree without prefabs and groups.
   *
   * @return the empty tree
   */
  static PrefabHierarchy empty() {
    return new PrefabHierarchy(new PrefabGroups(), Map.of());
  }

  /**
   * Builds the tree.
   *
   * @param instances instances of the level, in load order
   * @param hosts hosts and users of the attached prefabs
   * @param groups groups of the level
   * @param rootOrder order of the prefabs directly inside a group or outside of all groups
   * @return the tree
   */
  static PrefabHierarchy build(
      List<PrefabInstance> instances,
      AttachedPrefabLayout.Hosts hosts,
      PrefabGroups groups,
      Comparator<PrefabInstance> rootOrder) {
    PrefabHierarchy tree = new PrefabHierarchy(groups, hosts.hosts());
    Map<String, PrefabInstance> byName = new HashMap<>();
    Map<String, Integer> order = new HashMap<>();
    for (PrefabInstance instance : instances) {
      byName.put(instance.name(), instance);
      order.put(instance.name(), order.size());
    }
    for (PrefabInstance instance : instances) tree.group(instance.name(), hosts, groups);
    for (PrefabInstance instance : instances) {
      String host = hosts.hosts().get(instance.name());
      if (host != null
          && byName.containsKey(host)
          && Objects.equals(tree.groupByName.get(instance.name()), tree.groupByName.get(host))) {
        tree.parentByName.put(instance.name(), host);
      }
    }

    Map<String, List<Node>> children = new HashMap<>();
    Map<String, List<PrefabInstance>> references = new HashMap<>();
    for (PrefabInstance instance : instances) {
      String name = instance.name();
      String parent = tree.parentByName.get(name);
      for (String user : hosts.users().getOrDefault(name, List.of())) {
        if (!user.equals(parent) && byName.containsKey(user)) {
          references.computeIfAbsent(user, ignored -> new ArrayList<>()).add(instance);
        }
      }
    }
    // Nested prefabs before their hosts, so every node is complete when its host is created.
    for (PrefabInstance instance : depthFirst(instances, tree.parentByName).reversed()) {
      String name = instance.name();
      List<Node> nested = children.getOrDefault(name, new ArrayList<>());
      nested.sort(Comparator.comparingInt(node -> order.get(node.instance().name())));
      Node node = new Node(instance, List.copyOf(nested), references.getOrDefault(name, List.of()));
      tree.nodes.put(name, node);
      String parent = tree.parentByName.get(name);
      if (parent != null) children.computeIfAbsent(parent, ignored -> new ArrayList<>()).add(node);
    }

    List<PrefabInstance> roots =
        instances.stream()
            .filter(instance -> !tree.parentByName.containsKey(instance.name()))
            .sorted(rootOrder)
            .toList();
    for (PrefabInstance root : roots) {
      Node node = tree.nodes.get(root.name());
      String group = tree.groupByName.get(root.name());
      if (group == null) tree.ungrouped.add(node);
      else tree.rootsByGroup.get(group).add(node);
    }
    return tree;
  }

  /*
   * The group of a prefab: its own, else the one of its host. Hosts form no loop, the chain of hosts
   * ends at a prefab without one.
   */
  private String group(String name, AttachedPrefabLayout.Hosts hosts, PrefabGroups groups) {
    if (groupByName.containsKey(name)) return groupByName.get(name);
    Optional<String> own = groups.groupOf(name).filter(rootsByGroup::containsKey);
    String host = hosts.hosts().get(name);
    String group = own.orElseGet(() -> host == null ? null : group(host, hosts, groups));
    groupByName.put(name, group);
    return group;
  }

  /* Orders instances so that every host comes before the prefabs nested below it. */
  private static List<PrefabInstance> depthFirst(
      List<PrefabInstance> instances, Map<String, String> parentByName) {
    Map<String, List<PrefabInstance>> children = new HashMap<>();
    for (PrefabInstance instance : instances) {
      String parent = parentByName.get(instance.name());
      if (parent != null) {
        children.computeIfAbsent(parent, ignored -> new ArrayList<>()).add(instance);
      }
    }
    List<PrefabInstance> result = new ArrayList<>();
    for (PrefabInstance instance : instances) {
      if (!parentByName.containsKey(instance.name())) visit(instance, children, result);
    }
    return result;
  }

  private static void visit(
      PrefabInstance instance,
      Map<String, List<PrefabInstance>> children,
      List<PrefabInstance> result) {
    result.add(instance);
    for (PrefabInstance child : children.getOrDefault(instance.name(), List.of())) {
      visit(child, children, result);
    }
  }

  /**
   * Returns the groups outside of all groups.
   *
   * @return group names, in order
   */
  List<String> groups() {
    return groups;
  }

  /**
   * Returns the groups directly inside a group.
   *
   * @param group group name
   * @return group names, in order
   */
  List<String> subgroups(String group) {
    return subgroups.getOrDefault(group, List.of());
  }

  /**
   * Returns the prefabs directly inside a group.
   *
   * @param group group name
   * @return the prefabs, in list order
   */
  List<Node> roots(String group) {
    return rootsByGroup.getOrDefault(group, List.of());
  }

  /**
   * Returns the prefabs outside of all groups, that are not nested below another prefab.
   *
   * @return the prefabs, in list order
   */
  List<Node> ungrouped() {
    return ungrouped;
  }

  /**
   * Returns the group a prefab is listed in: its own group, or the one of its host.
   *
   * @param name instance name
   * @return the group, empty outside of all groups
   */
  Optional<String> groupOf(String name) {
    return Optional.ofNullable(groupByName.get(name));
  }

  /**
   * Returns the host of a prefab without a world position, the prefab it is shown next to in the
   * world.
   *
   * @param name instance name
   * @return its host, empty for a prefab with a world position or one that starts a chain
   */
  Optional<String> hostOf(String name) {
    return Optional.ofNullable(hosts.get(name));
  }

  /**
   * Returns the prefabs nested below a prefab, also indirectly.
   *
   * @param name instance name
   * @return names of the nested prefabs, depth first
   */
  List<String> descendants(String name) {
    List<String> result = new ArrayList<>();
    Node node = nodes.get(name);
    if (node != null) collect(node.children(), result);
    return result;
  }

  /**
   * Returns all prefabs listed in a group and the groups inside of it, the nested ones included.
   *
   * @param group group name
   * @return names of the prefabs, depth first
   */
  List<String> members(String group) {
    List<String> result = new ArrayList<>();
    for (String inside : subgroups(group)) result.addAll(members(inside));
    for (Node root : roots(group)) {
      result.add(root.instance().name());
      collect(root.children(), result);
    }
    return result;
  }

  private static void collect(List<Node> nodes, List<String> result) {
    for (Node node : nodes) {
      result.add(node.instance().name());
      collect(node.children(), result);
    }
  }
}
