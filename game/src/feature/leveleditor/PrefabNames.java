package feature.leveleditor;

import feature.prefabs.PrefabInstance;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Naming and grouping rules for prefab instances in the level editor. */
final class PrefabNames {

  private static final Pattern GROUP_SUFFIX = Pattern.compile("^(.+)-(\\d+)$");
  private static final Pattern TRAILING_NUMBER = Pattern.compile("^(.*?)(\\d+)$");

  private PrefabNames() {}

  /**
   * Returns everything before the last {@code -<number>} suffix, or the full name without one.
   *
   * @param name prefab instance name
   * @return group base name
   */
  static String baseName(String name) {
    Matcher matcher = GROUP_SUFFIX.matcher(name);
    return matcher.matches() ? matcher.group(1) : name;
  }

  /**
   * Returns the {@code -<number>} suffix of a name, or an empty string without one.
   *
   * @param name prefab instance name
   * @return group suffix including the dash
   */
  static String groupSuffix(String name) {
    return name.substring(baseName(name).length());
  }

  /**
   * Derives the name for a copy of a prefab. A trailing number is incremented, otherwise {@code -1}
   * is appended. The number keeps being incremented until the name is free.
   *
   * @param name source name
   * @param taken whether a name is already in use
   * @return unused derived name
   */
  static String nextName(String name, Predicate<String> taken) {
    Matcher matcher = TRAILING_NUMBER.matcher(name);
    String prefix;
    String digits;
    if (matcher.matches()) {
      prefix = matcher.group(1);
      digits = matcher.group(2);
    } else {
      prefix = name + "-";
      digits = "0";
    }
    BigInteger number = new BigInteger(digits);
    String candidate;
    do {
      number = number.add(BigInteger.ONE);
      String formatted = number.toString();
      if (formatted.length() < digits.length()) {
        formatted = "0".repeat(digits.length() - formatted.length()) + formatted;
      }
      candidate = prefix + formatted;
    } while (taken.test(candidate));
    return candidate;
  }

  /**
   * Returns everything before the first {@code /}, or {@code null} if the name has no area prefix.
   *
   * @param name prefab instance name
   * @return area prefix without the slash, or {@code null}
   */
  static String areaPrefix(String name) {
    int index = name.indexOf('/');
    return index > 0 ? name.substring(0, index) : null;
  }

  /** Node of the grouped prefab tree. */
  sealed interface Node permits Leaf, Branch {
    /**
     * Returns a stable identifier of this node within one tree.
     *
     * @return node ID
     */
    String id();
  }

  /**
   * A single prefab instance.
   *
   * @param instance prefab instance
   */
  record Leaf(PrefabInstance instance) implements Node {
    @Override
    public String id() {
      return entryId(instance.name());
    }
  }

  /** Kind of grouping of a branch. */
  enum BranchKind {
    /** Instances sharing the area prefix before the first {@code /}. */
    AREA,
    /** Instances sharing the base name before a {@code -<number>} suffix. */
    SUFFIX
  }

  /**
   * A group of nodes.
   *
   * @param kind grouping kind
   * @param key area prefix or base name
   * @param children ordered child nodes
   */
  record Branch(BranchKind kind, String key, List<Node> children) implements Node {
    /**
     * Creates a branch with an immutable child list.
     *
     * @param kind grouping kind
     * @param key area prefix or base name
     * @param children ordered child nodes
     */
    Branch {
      children = List.copyOf(children);
    }

    @Override
    public String id() {
      return groupId(kind, key);
    }

    Branch withChildren(List<Node> newChildren) {
      return new Branch(kind, key, newChildren);
    }
  }

  /**
   * Returns the node ID of an instance.
   *
   * @param name instance name
   * @return node ID
   */
  static String entryId(String name) {
    return "entry:" + name;
  }

  /**
   * Returns the node ID of a group.
   *
   * @param kind grouping kind
   * @param key area prefix or base name
   * @return node ID
   */
  static String groupId(BranchKind kind, String key) {
    return kind.name() + ":" + key;
  }

  /**
   * Groups prefabs into a tree. Instances sharing an area prefix with at least one other instance
   * form an area group. Within an area group, and for instances outside of one, instances sharing a
   * base name with at least one other instance form a suffix group. Every group is placed where its
   * first member appears and relative order is preserved inside of it.
   *
   * @param prefabs ordered prefab instances
   * @return ordered top-level nodes
   */
  static List<Node> tree(List<PrefabInstance> prefabs) {
    Map<String, Integer> areaCounts = new HashMap<>();
    for (PrefabInstance instance : prefabs) {
      String area = areaPrefix(instance.name());
      if (area != null) areaCounts.merge(area, 1, Integer::sum);
    }
    Map<String, List<PrefabInstance>> units = new LinkedHashMap<>();
    for (PrefabInstance instance : prefabs) {
      String area = areaPrefix(instance.name());
      String unit =
          area != null && areaCounts.get(area) > 1
              ? groupId(BranchKind.AREA, area)
              : groupId(BranchKind.SUFFIX, baseName(instance.name()));
      units.computeIfAbsent(unit, ignored -> new ArrayList<>()).add(instance);
    }
    List<Node> nodes = new ArrayList<>();
    for (List<PrefabInstance> members : units.values()) {
      String area = areaPrefix(members.getFirst().name());
      if (area != null && areaCounts.get(area) > 1) {
        nodes.add(new Branch(BranchKind.AREA, area, suffixGroups(members)));
      } else {
        nodes.addAll(suffixGroups(members));
      }
    }
    return nodes;
  }

  private static List<Node> suffixGroups(List<PrefabInstance> prefabs) {
    Map<String, List<PrefabInstance>> byBase = new LinkedHashMap<>();
    for (PrefabInstance instance : prefabs) {
      byBase.computeIfAbsent(baseName(instance.name()), ignored -> new ArrayList<>()).add(instance);
    }
    List<Node> nodes = new ArrayList<>();
    byBase.forEach(
        (base, members) -> {
          if (members.size() > 1) {
            nodes.add(
                new Branch(
                    BranchKind.SUFFIX, base, members.stream().<Node>map(Leaf::new).toList()));
          } else {
            nodes.add(new Leaf(members.getFirst()));
          }
        });
    return nodes;
  }

  /**
   * Sorts a tree alphabetically by node name. Members of suffix groups are sorted by their numeric
   * suffix, with a member without suffix first.
   *
   * @param nodes nodes from {@link #tree}
   * @return sorted copy
   */
  static List<Node> alphabetical(List<Node> nodes) {
    Comparator<String> byName =
        Comparator.<String, String>comparing(name -> name, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Comparator.naturalOrder());
    Comparator<Node> bySuffix =
        Comparator.comparing(
                (Node node) -> suffixNumber(nodeName(node)),
                Comparator.nullsFirst(Comparator.naturalOrder()))
            .thenComparing(PrefabNames::nodeName, byName);
    return sorted(nodes, Comparator.comparing(PrefabNames::nodeName, byName), bySuffix);
  }

  private static List<Node> sorted(
      List<Node> nodes, Comparator<Node> byName, Comparator<Node> bySuffix) {
    List<Node> result = new ArrayList<>();
    for (Node node : nodes) {
      if (node instanceof Branch branch) {
        List<Node> children =
            branch.kind() == BranchKind.SUFFIX
                ? branch.children().stream().sorted(bySuffix).toList()
                : sorted(branch.children(), byName, bySuffix);
        result.add(branch.withChildren(children));
      } else {
        result.add(node);
      }
    }
    result.sort(byName);
    return result;
  }

  private static String nodeName(Node node) {
    return switch (node) {
      case Leaf leaf -> leaf.instance().name();
      case Branch branch -> branch.key();
    };
  }

  private static BigInteger suffixNumber(String name) {
    String suffix = groupSuffix(name);
    return suffix.isEmpty() ? null : new BigInteger(suffix.substring(1));
  }

  /**
   * Flattens a tree back into a load order.
   *
   * @param nodes ordered nodes
   * @return flattened prefab order
   */
  static List<PrefabInstance> flatten(List<Node> nodes) {
    List<PrefabInstance> result = new ArrayList<>();
    for (Node node : nodes) {
      switch (node) {
        case Leaf leaf -> result.add(leaf.instance());
        case Branch branch -> result.addAll(flatten(branch.children()));
      }
    }
    return result;
  }

  /**
   * Returns all instances below a node.
   *
   * @param node tree node
   * @return instances in order
   */
  static List<PrefabInstance> instances(Node node) {
    return flatten(List.of(node));
  }

  /**
   * Reorders prefabs so every group is contiguous.
   *
   * @param prefabs ordered prefab instances
   * @return grouped load order
   */
  static List<PrefabInstance> groupedOrder(List<PrefabInstance> prefabs) {
    return flatten(tree(prefabs));
  }

  /**
   * Finds a node by its ID.
   *
   * @param nodes nodes to search recursively
   * @param id node ID
   * @return the node, or {@code null}
   */
  static Node find(List<Node> nodes, String id) {
    for (Node node : nodes) {
      if (node.id().equals(id)) return node;
      if (node instanceof Branch branch) {
        Node found = find(branch.children(), id);
        if (found != null) return found;
      }
    }
    return null;
  }

  /**
   * Moves a child of a group, or a top-level node, in front of the sibling at {@code
   * insertionIndex}.
   *
   * @param nodes top-level nodes
   * @param parentId ID of the parent group, or {@code null} for top-level nodes
   * @param childId ID of the moved node
   * @param insertionIndex insertion position among the original siblings
   * @return reordered tree, or {@code null} if the node was not found
   */
  static List<Node> moveChild(
      List<Node> nodes, String parentId, String childId, int insertionIndex) {
    if (parentId == null) {
      for (int index = 0; index < nodes.size(); index++) {
        if (nodes.get(index).id().equals(childId)) return move(nodes, index, insertionIndex);
      }
      return null;
    }
    for (int index = 0; index < nodes.size(); index++) {
      if (!(nodes.get(index) instanceof Branch branch)) continue;
      List<Node> children =
          branch.id().equals(parentId)
              ? moveChild(branch.children(), null, childId, insertionIndex)
              : moveChild(branch.children(), parentId, childId, insertionIndex);
      if (children == null) continue;
      List<Node> result = new ArrayList<>(nodes);
      result.set(index, branch.withChildren(children));
      return result;
    }
    return null;
  }

  /**
   * Moves an element of a list in front of the element at {@code insertionIndex} in the original
   * list, where {@code insertionIndex == list.size()} moves it to the end.
   *
   * @param list source list
   * @param from index of the moved element
   * @param insertionIndex insertion position in the original list
   * @param <T> element type
   * @return reordered copy
   */
  static <T> List<T> move(List<T> list, int from, int insertionIndex) {
    List<T> result = new ArrayList<>(list);
    T moved = result.remove(from);
    int target = insertionIndex > from ? insertionIndex - 1 : insertionIndex;
    result.add(Math.clamp(target, 0, result.size()), moved);
    return result;
  }
}
