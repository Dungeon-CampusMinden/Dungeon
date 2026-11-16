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

/**
 * The problems of the prefabs of the edited level, see {@link PrefabChecks}, for the panels of the
 * level editor. They are found again after the level changed.
 */
public final class LevelProblems {

  private static ILevel checkedLevel;
  private static boolean changed = true;
  private static List<PrefabProblem> problems = List.of();
  // Sorted into sections when they are needed, the status bar only counts the problems.
  private static List<Section> sections;

  private LevelProblems() {}

  /**
   * A section of the problem list: the problems of the prefabs listed in one group of the prefab
   * list.
   *
   * @param path the group and the groups containing it, outermost first; empty for the prefabs
   *     outside of all groups
   * @param problems problems of the section, errors first, then in level order
   */
  public record Section(List<String> path, List<PrefabProblem> problems) {}

  /** Marks the problems as outdated. The level editor calls it after every change of the level. */
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
   * @return non-empty sections; a new list once the problems were found again
   */
  public static List<Section> sections() {
    update();
    if (sections == null) sections = sections(checkedLevel, problems);
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

  private static void update() {
    ILevel level = Game.currentLevel().orElse(null);
    if (!changed && level == checkedLevel) return;
    changed = false;
    checkedLevel = level;
    problems = level == null ? List.of() : PrefabChecks.check(level);
    sections = null;
  }

  private static List<Section> sections(ILevel level, List<PrefabProblem> found) {
    if (level == null || found.isEmpty()) return List.of();
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
            Comparator.comparingInt(instance -> order.get(instance.name())));

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
      List<PrefabProblem> inGroup = byGroup.get(group);
      if (inGroup != null) result.add(new Section(groups.path(group), List.copyOf(inGroup)));
    }
    List<PrefabProblem> ungrouped = byGroup.get(null);
    if (ungrouped != null) result.add(new Section(List.of(), List.copyOf(ungrouped)));
    return List.copyOf(result);
  }
}
