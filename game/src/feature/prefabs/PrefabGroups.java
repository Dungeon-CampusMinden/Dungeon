package feature.prefabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Groups of the prefab instances of a level, e.g. "meta", "room-1" or "puzzle-1", to organize the
 * prefab list of the level editor. Groups have no effect on the game.
 *
 * <p>Groups can contain further groups, e.g. a puzzle inside the group of its room. Group names are
 * unique in a level, whatever group contains them. An instance is in at most one group. The level
 * editor lists an instance without a group with the prefab that uses it, or outside of all groups.
 *
 * <p>Level files store the group of an instance in its record, and all groups in order, empty ones
 * included. Before groups existed, prefabs were sorted into sections by a name prefix like {@code
 * "room-1/door"}, see {@link #fromNamePrefixes} and {@link #namesWithoutPrefix}.
 */
public final class PrefabGroups {

  // Groups outside of all groups, in order.
  private final List<String> roots = new ArrayList<>();
  // The groups inside every group, in order. Every group has an entry.
  private final Map<String, List<String>> children = new HashMap<>();
  // The group containing a group, for groups inside another group.
  private final Map<String, String> parents = new HashMap<>();
  private final Map<String, String> groupByInstance = new HashMap<>();

  /**
   * Turns the sections of older levels into groups: an instance named like {@code "room-1/door"}
   * gets the group {@code "room-1"}. Groups are ordered by their first instance. The names still
   * start with the group, see {@link #namesWithoutPrefix}.
   *
   * @param instances instances in load order
   * @return the groups
   */
  public static PrefabGroups fromNamePrefixes(List<PrefabInstance> instances) {
    PrefabGroups groups = new PrefabGroups();
    for (PrefabInstance instance : instances) {
      sectionOf(instance.name()).ifPresent(group -> groups.assign(instance.name(), group));
    }
    return groups;
  }

  /**
   * Returns names without the group for the instances that start with it, like {@code
   * "room-1/door"} in the group {@code "room-1"}, which becomes {@code "door"}. Names were written
   * like this before groups existed.
   *
   * <p>If the shorter name is taken, the instance gets the next free number, see {@link
   * PrefabNames#nextName}. The names of the other instances never change, and the shorter names are
   * handed out in load order.
   *
   * @param instances instances in load order
   * @return new names by old name, empty if no name starts with its group
   */
  public Map<String, String> namesWithoutPrefix(List<PrefabInstance> instances) {
    Map<String, String> shorter = new LinkedHashMap<>();
    for (PrefabInstance instance : instances) {
      String name = instance.name();
      String group = groupByInstance.get(name);
      String rest = name.substring(name.indexOf('/') + 1).strip();
      if (group != null && sectionOf(name).equals(Optional.of(group)) && !rest.isEmpty()) {
        shorter.put(name, rest);
      }
    }
    Set<String> taken = new HashSet<>();
    for (PrefabInstance instance : instances) {
      if (!shorter.containsKey(instance.name())) taken.add(instance.name());
    }
    Map<String, String> result = new LinkedHashMap<>();
    for (String name : shorter.keySet()) {
      if (taken.add(shorter.get(name))) result.put(name, shorter.get(name));
    }
    // Only now, so a number never takes the name another instance gets without its group.
    for (String name : shorter.keySet()) {
      if (result.containsKey(name)) continue;
      String free = PrefabNames.nextName(shorter.get(name), taken::contains);
      taken.add(free);
      result.put(name, free);
    }
    return result;
  }

  // The section of a name from before groups existed, e.g. "room-1" of "room-1/door".
  private static Optional<String> sectionOf(String name) {
    int slash = name.indexOf('/');
    if (slash <= 0) return Optional.empty();
    String section = name.substring(0, slash).strip();
    return section.isEmpty() ? Optional.empty() : Optional.of(section);
  }

  /**
   * Returns all groups, each followed by the groups inside of it.
   *
   * @return group names, depth first
   */
  public List<String> names() {
    List<String> result = new ArrayList<>();
    for (String root : roots) collect(root, result);
    return Collections.unmodifiableList(result);
  }

  private void collect(String group, List<String> result) {
    result.add(group);
    for (String child : children.get(group)) collect(child, result);
  }

  /**
   * Returns the groups outside of all groups.
   *
   * @return unmodifiable view of the group names, in order
   */
  public List<String> roots() {
    return Collections.unmodifiableList(roots);
  }

  /**
   * Returns the groups directly inside a group.
   *
   * @param group group name
   * @return unmodifiable view of the group names in order, empty for an unknown group
   */
  public List<String> children(String group) {
    List<String> result = children.get(group);
    return result == null ? List.of() : Collections.unmodifiableList(result);
  }

  /**
   * Returns the group containing a group.
   *
   * @param group group name
   * @return the containing group, empty for a group outside of all groups
   */
  public Optional<String> parentOf(String group) {
    return Optional.ofNullable(parents.get(group));
  }

  /**
   * Returns a group and the groups containing it.
   *
   * @param group group name, or {@code null}
   * @return unmodifiable list of the group names, outermost first; empty for {@code null}
   */
  public List<String> path(String group) {
    List<String> path = new ArrayList<>();
    for (String current = group; current != null; current = parents.get(current)) {
      path.addFirst(current);
    }
    return Collections.unmodifiableList(path);
  }

  /**
   * Returns whether a group is another group or inside of it, also indirectly.
   *
   * @param group group name
   * @param ancestor name of the other group
   * @return true if {@code group} is {@code ancestor} or inside of it
   */
  public boolean isWithin(String group, String ancestor) {
    for (String current = group; current != null; current = parents.get(current)) {
      if (current.equals(ancestor)) return true;
    }
    return false;
  }

  /**
   * Returns whether a group exists.
   *
   * @param group group name
   * @return true if the group exists
   */
  public boolean contains(String group) {
    return children.containsKey(group);
  }

  /**
   * Returns the group of an instance.
   *
   * @param instance instance name
   * @return its group, empty if it has none
   */
  public Optional<String> groupOf(String instance) {
    return Optional.ofNullable(groupByInstance.get(instance));
  }

  /**
   * Adds an empty group behind the groups outside of all groups, if it does not exist yet.
   *
   * @param group group name
   */
  public void add(String group) {
    add(group, null);
  }

  /**
   * Adds an empty group behind the groups inside another group, if it does not exist yet.
   *
   * @param group group name
   * @param parent group to add it into, or {@code null} to add it outside of all groups
   * @throws IllegalArgumentException if the name is blank or the parent does not exist
   */
  public void add(String group, String parent) {
    requireName(group);
    if (parent != null && !contains(parent)) {
      throw new IllegalArgumentException("Unknown group: " + parent);
    }
    if (contains(group)) return;
    children.put(group, new ArrayList<>());
    siblings(parent).add(group);
    if (parent != null) parents.put(group, parent);
  }

  /**
   * Puts an instance into a group. A group that does not exist yet is added outside of all groups.
   *
   * @param instance instance name
   * @param group group name, or {@code null} to remove the instance from its group
   */
  public void assign(String instance, String group) {
    Objects.requireNonNull(instance, "instance");
    if (group == null) {
      groupByInstance.remove(instance);
      return;
    }
    add(group);
    groupByInstance.put(instance, group);
  }

  /**
   * Renames a group, its instances and groups stay in it.
   *
   * @param group current group name
   * @param newName new group name
   * @throws IllegalArgumentException if the group does not exist or the new name is taken
   */
  public void rename(String group, String newName) {
    requireName(newName);
    if (!contains(group)) throw new IllegalArgumentException("Unknown group: " + group);
    if (group.equals(newName)) return;
    if (contains(newName)) throw new IllegalArgumentException("Group already exists: " + newName);
    String parent = parents.remove(group);
    List<String> siblings = siblings(parent);
    siblings.set(siblings.indexOf(group), newName);
    if (parent != null) parents.put(newName, parent);
    List<String> inside = children.remove(group);
    children.put(newName, inside);
    for (String child : inside) parents.put(child, newName);
    groupByInstance.replaceAll((instance, current) -> current.equals(group) ? newName : current);
  }

  /**
   * Removes a group. What is inside of it, instances and groups, moves to the group containing it,
   * or outside of all groups. The groups take the place of the removed group.
   *
   * @param group group name
   */
  public void remove(String group) {
    if (!contains(group)) return;
    String parent = parents.remove(group);
    List<String> siblings = siblings(parent);
    int index = siblings.indexOf(group);
    siblings.remove(index);
    List<String> inside = children.remove(group);
    siblings.addAll(index, inside);
    for (String child : inside) {
      if (parent == null) parents.remove(child);
      else parents.put(child, parent);
    }
    if (parent == null) {
      groupByInstance.values().removeIf(group::equals);
    } else {
      groupByInstance.replaceAll((instance, current) -> current.equals(group) ? parent : current);
    }
  }

  /**
   * Moves a group, with everything inside of it, in front of a group inside another group, or
   * outside of all groups.
   *
   * @param group group name
   * @param parent group to move it into, or {@code null} to move it outside of all groups
   * @param insertionIndex index of the group it is moved in front of, among the groups directly
   *     inside {@code parent} in their current order, their count to move it behind them
   * @throws IllegalArgumentException if a group does not exist, or the parent is inside the group
   */
  public void move(String group, String parent, int insertionIndex) {
    if (!contains(group)) throw new IllegalArgumentException("Unknown group: " + group);
    if (parent != null && !contains(parent)) {
      throw new IllegalArgumentException("Unknown group: " + parent);
    }
    if (parent != null && isWithin(parent, group)) {
      throw new IllegalArgumentException("A group cannot move into itself: " + group);
    }
    List<String> from = siblings(parents.get(group));
    List<String> to = siblings(parent);
    int fromIndex = from.indexOf(group);
    int target = from == to && insertionIndex > fromIndex ? insertionIndex - 1 : insertionIndex;
    from.remove(fromIndex);
    to.add(Math.clamp(target, 0, to.size()), group);
    if (parent == null) parents.remove(group);
    else parents.put(group, parent);
  }

  // The list a group inside the given group is kept in.
  private List<String> siblings(String parent) {
    return parent == null ? roots : children.get(parent);
  }

  /**
   * Keeps the groups of renamed instances.
   *
   * @param renamed new names by old name
   */
  public void renameInstances(Map<String, String> renamed) {
    Map<String, String> moved = new HashMap<>();
    renamed.forEach(
        (oldName, newName) -> {
          String group = groupByInstance.remove(oldName);
          if (group != null) moved.put(newName, group);
        });
    // Added after all removals, as a new name can be the old name of another renamed instance.
    groupByInstance.putAll(moved);
  }

  /**
   * Forgets the group of a removed instance.
   *
   * @param name instance name
   */
  public void removeInstance(String name) {
    groupByInstance.remove(name);
  }

  /**
   * Returns an independent copy, e.g. to restore the groups after a failed edit.
   *
   * @return the copy
   */
  public PrefabGroups copy() {
    PrefabGroups copy = new PrefabGroups();
    copy.set(this);
    return copy;
  }

  /**
   * Replaces all groups and memberships with the ones of other groups.
   *
   * @param other groups to copy
   */
  public void set(PrefabGroups other) {
    if (other == this) return;
    roots.clear();
    roots.addAll(other.roots);
    children.clear();
    other.children.forEach((group, inside) -> children.put(group, new ArrayList<>(inside)));
    parents.clear();
    parents.putAll(other.parents);
    groupByInstance.clear();
    groupByInstance.putAll(other.groupByInstance);
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof PrefabGroups groups
        && roots.equals(groups.roots)
        && children.equals(groups.children)
        && parents.equals(groups.parents)
        && groupByInstance.equals(groups.groupByInstance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roots, children, parents, groupByInstance);
  }

  private static void requireName(String group) {
    if (group == null || group.isBlank()) {
      throw new IllegalArgumentException("Group name must not be blank");
    }
  }
}
