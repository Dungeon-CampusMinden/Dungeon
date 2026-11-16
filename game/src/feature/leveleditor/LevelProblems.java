package feature.leveleditor;

import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.ILevel;
import feature.prefabs.PrefabChecks;
import feature.prefabs.PrefabGroups;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProblem;
import feature.prefabs.PrefabWiring;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The problems of the prefabs of the edited level, see {@link PrefabChecks}, for the panels of the
 * level editor. They are found again whenever the level changed.
 */
public final class LevelProblems {

  private static ILevel checkedLevel;
  // The instances that were checked, compared by identity: edits replace changed instances.
  private static List<PrefabInstance> checkedPrefabs = List.of();
  private static boolean changed = true;
  private static List<PrefabProblem> problems = List.of();
  private static List<Section> sections = List.of();
  private static int version;

  private LevelProblems() {}

  /**
   * A section of the problem list: the problems of the prefabs listed in one group of the prefab
   * list.
   *
   * @param group the group, or {@code null} for the prefabs outside of all groups
   * @param path the group and the groups containing it, outermost first; empty outside of all
   *     groups
   * @param problems problems of the section, errors first, then in level order
   */
  public record Section(String group, List<String> path, List<PrefabProblem> problems) {}

  /**
   * Marks the problems as outdated, e.g. after tiles were changed. Changes of the prefab instances
   * are noticed without this.
   */
  public static void levelChanged() {
    changed = true;
  }

  /**
   * Returns the problems of the current level.
   *
   * @return problems ordered by instance in load order, empty without a level
   */
  public static List<PrefabProblem> current() {
    update();
    return problems;
  }

  /**
   * Returns the problems of the current level grouped like the prefab list: by the group a prefab
   * is listed in, in the order of the groups, followed by the prefabs outside of all groups.
   *
   * @return non-empty sections
   */
  public static List<Section> sections() {
    update();
    return sections;
  }

  /**
   * Counts the problems of a severity.
   *
   * @param severity severity to count
   * @return number of problems of the current level with the severity
   */
  public static int count(PrefabProblem.Severity severity) {
    return (int) current().stream().filter(problem -> problem.severity() == severity).count();
  }

  /**
   * Returns a number that changes whenever the problems or their sections change, e.g. to rebuild a
   * list showing them.
   *
   * @return current version
   */
  public static int version() {
    update();
    return version;
  }

  private static void update() {
    ILevel level = Game.currentLevel().orElse(null);
    if (level == null) {
      if (checkedLevel != null || !problems.isEmpty()) {
        checkedLevel = null;
        checkedPrefabs = List.of();
        replace(List.of(), List.of());
      }
      return;
    }
    if (!changed && level == checkedLevel && sameInstances(level.prefabs())) return;
    changed = false;
    checkedLevel = level;
    checkedPrefabs = List.copyOf(level.prefabs());
    List<PrefabProblem> found = PrefabChecks.check(level);
    replace(found, sections(level, found));
  }

  private static void replace(List<PrefabProblem> found, List<Section> grouped) {
    if (found.equals(problems) && grouped.equals(sections)) return;
    problems = found;
    sections = grouped;
    version++;
  }

  private static boolean sameInstances(List<PrefabInstance> prefabs) {
    if (prefabs.size() != checkedPrefabs.size()) return false;
    for (int index = 0; index < prefabs.size(); index++) {
      if (prefabs.get(index) != checkedPrefabs.get(index)) return false;
    }
    return true;
  }

  private static List<Section> sections(ILevel level, List<PrefabProblem> found) {
    if (found.isEmpty()) return List.of();
    PrefabGroups groups =
        level instanceof DungeonLevel dungeonLevel
            ? dungeonLevel.prefabGroups()
            : new PrefabGroups();
    List<PrefabInstance> prefabs = level.prefabs();
    Map<String, Integer> order = new HashMap<>();
    for (PrefabInstance instance : prefabs) order.putIfAbsent(instance.name(), order.size());
    AttachedPrefabLayout.Hosts hosts =
        AttachedPrefabLayout.hosts(
            prefabs, PrefabWiring.connections(prefabs), PrefabMode::hasWorldPosition);
    PrefabHierarchy hierarchy =
        PrefabHierarchy.build(
            prefabs,
            hosts,
            groups,
            Comparator.comparingInt(instance -> order.getOrDefault(instance.name(), 0)));

    List<PrefabProblem> sorted = new ArrayList<>(found);
    // Stable, so the problems of one severity stay in level order.
    sorted.sort(Comparator.comparing(PrefabProblem::severity).reversed());
    // The prefabs outside of all groups are collected under null.
    Map<String, List<PrefabProblem>> byGroup = new LinkedHashMap<>();
    for (PrefabProblem problem : sorted) {
      String group = hierarchy.groupOf(problem.instance()).orElse(null);
      byGroup.computeIfAbsent(group, ignored -> new ArrayList<>()).add(problem);
    }
    List<Section> result = new ArrayList<>();
    for (String group : groups.names()) {
      List<PrefabProblem> inGroup = byGroup.remove(group);
      if (inGroup != null)
        result.add(new Section(group, path(groups, group), List.copyOf(inGroup)));
    }
    List<PrefabProblem> ungrouped = byGroup.get(null);
    if (ungrouped != null) result.add(new Section(null, List.of(), List.copyOf(ungrouped)));
    return List.copyOf(result);
  }

  private static List<String> path(PrefabGroups groups, String group) {
    List<String> path = new ArrayList<>();
    for (Optional<String> current = Optional.of(group);
        current.isPresent();
        current = groups.parentOf(current.get())) {
      path.addFirst(current.get());
    }
    return List.copyOf(path);
  }
}
