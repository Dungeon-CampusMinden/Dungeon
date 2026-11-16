package feature.prefabs.types;

import engine.level.elements.ILevel;
import feature.prefabs.GameFlags;
import feature.prefabs.PlayerSource;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProblem;
import feature.prefabs.PrefabProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Authored list of {@link GameFlags game flag} names on a flag prefab: the number of flags and one
 * name per possible flag.
 *
 * @param count number of used flags
 * @param names name of every possible flag, only shown up to {@link #count}
 */
record PrefabFlagList(PrefabProperty<Integer> count, List<PrefabProperty<String>> names) {

  /** Maximum number of flags. */
  static final int MAX_FLAGS = 8;

  /** Whether restoring a save can raise the event of a flag trigger. */
  static final PrefabProperty<Boolean> TRIGGER_ON_RESTORE =
      PrefabProperty.bool("trigger-on-restore", "Trigger On Restore", false);

  /**
   * Creates the properties of a flag list.
   *
   * @param defaultCount number of flags of a new instance
   * @return the list properties
   */
  static PrefabFlagList create(int defaultCount) {
    PrefabProperty<Integer> count =
        PrefabProperty.integer("flag-count", "Flag Count", defaultCount, 1, MAX_FLAGS);
    return new PrefabFlagList(
        count,
        PrefabTextList.entries(
            count, MAX_FLAGS, "flag", "Flag", index -> "flag-" + (index + 1), values -> true));
  }

  /**
   * Returns the count followed by the names, in the order the editor shows them.
   *
   * @return all properties of the list
   */
  List<PrefabProperty<?>> properties() {
    List<PrefabProperty<?>> properties = new ArrayList<>();
    properties.add(count);
    properties.addAll(names);
    return List.copyOf(properties);
  }

  /**
   * Returns the names of the used flags of an instance.
   *
   * @param instance instance holding the list
   * @return names up to the used count
   */
  List<String> values(PrefabInstance instance) {
    return PrefabTextList.values(instance, count, names);
  }

  /**
   * Returns where the player of the event of a flag trigger comes from: the players changing its
   * flags, and no player if restoring a save can raise it, see {@link #TRIGGER_ON_RESTORE}.
   *
   * @param instance flag trigger holding the list
   * @return origin of the player
   */
  PlayerSource triggerPlayers(PrefabInstance instance) {
    PlayerSource changes = PlayerSource.flags(values(instance));
    return instance.get(TRIGGER_ON_RESTORE) ? changes.or(PlayerSource.NO_PLAYER) : changes;
  }

  /**
   * Warns about the used flags of an instance that no Flag Set of the level changes, so they do not
   * change while the level is played.
   *
   * @param level level of the instance
   * @param instance instance holding the list
   * @return a warning, or none if every flag is changed somewhere
   */
  List<PrefabProblem> unchangedFlags(ILevel level, PrefabInstance instance) {
    Set<String> changed = FlagSetPrefab.flagsChangedIn(level);
    List<String> unchanged =
        values(instance).stream().filter(flag -> !changed.contains(flag)).distinct().toList();
    if (unchanged.isEmpty()) return List.of();
    return List.of(
        PrefabProblem.warning(
            instance.name(),
            "No Flag Set in this level changes "
                + String.join(", ", unchanged.stream().map(flag -> "\"" + flag + "\"").toList())));
  }

  /**
   * Returns a short editor summary of the used flags.
   *
   * @param instance instance holding the list
   * @return quoted flag names, shortened if long
   */
  String summary(PrefabInstance instance) {
    return PrefabDialogs.preview(
        String.join(", ", values(instance).stream().map(name -> "\"" + name + "\"").toList()));
  }
}
