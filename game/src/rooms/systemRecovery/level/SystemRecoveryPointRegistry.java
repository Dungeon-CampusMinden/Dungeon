package rooms.systemRecovery.level;

import engine.level.DungeonLevel;
import engine.utils.Point;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Resolves and validates the named points required by the System Recovery level. */
public final class SystemRecoveryPointRegistry {

  /** Main terminal custom point. */
  public static final String TERMINAL = "terminal";

  /** Door custom points used by checkpoint restoration. */
  public static final String DOOR_MODULE_STORAGE = "door_modulspeicher";
  public static final String DOOR_INVENTORY_SCANNER = "door_inventarscanner";
  public static final String DOOR_TRANSPORT_STORAGE = "door_transportlager";
  public static final String DOOR_DATA_STORAGE = "door_datenspeicher";
  public static final String DOOR_DATA_ARCHIVE = "door_datenarchiv";

  /** World item custom points used by checkpoint restoration. */
  public static final String ARCHIVE_KEY_SPAWN = "chip_spawn";
  public static final String SEARCH_PROGRAM_CHIP = "chip";
  public static final String SYSTEM_CORE_ACCESS_MODULE_DESTINATION =
      "roboter_item_destination";

  private static final List<PointGroup> REQUIRED_GROUPS = createRequiredGroups();

  private SystemRecoveryPointRegistry() {}

  /**
   * Resolves every required point once and reports all missing points together.
   *
   * <p>Failing during level setup is intentional. A missing point otherwise becomes a delayed
   * failure in a later riddle, where the original layout mistake is much harder to diagnose.
   *
   * @param level level whose named points should be checked
   * @return immutable map of canonical point names to their positions
   * @throws IllegalStateException if one or more required points are missing
   */
  public static Map<String, Point> resolve(DungeonLevel level) {
    Objects.requireNonNull(level, "level");
    Map<String, Point> resolved = new LinkedHashMap<>();
    List<String> missing = new ArrayList<>();

    for (PointGroup group : REQUIRED_GROUPS) {
      for (String pointName : group.pointNames()) {
        try {
          resolved.put(pointName, resolvePoint(level, pointName));
        } catch (java.util.NoSuchElementException exception) {
          missing.add(group.riddle() + ": " + pointName);
        }
      }
    }

    if (!missing.isEmpty()) {
      throw new IllegalStateException(
          "System Recovery layout is missing required custom points:\n- "
              + String.join("\n- ", missing));
    }
    return Map.copyOf(resolved);
  }

  private static List<PointGroup> createRequiredGroups() {
    List<PointGroup> groups = new ArrayList<>();
    groups.add(
        new PointGroup(
            "level",
            List.of(
                "phone",
                TERMINAL,
                "end",
                DOOR_MODULE_STORAGE,
                DOOR_INVENTORY_SCANNER,
                DOOR_TRANSPORT_STORAGE,
                DOOR_DATA_STORAGE,
                DOOR_DATA_ARCHIVE,
                "door_speicher",
                "door_systemcore",
                "door_elevator",
                "label_modulspeicher",
                "label_inventarscanner",
                "label_transportlager",
                "label_datenspeicher",
                "label_sortmachine",
                "label_archive",
                "label_speicher",
                "label_suchroboter",
                "label_systemcore")));
    groups.add(
        new PointGroup(
            "riddle 1 - energy",
            merge(
                List.of("array_lever", "array_item_spawn", "batteriebox_modul", "display_energie"),
                indexed("a", 5))));
    groups.add(
        new PointGroup(
            "riddle 2 - module storage",
            merge(indexed("s", 5), List.of("display_room2", "room3_keypad"))));
    groups.add(
        new PointGroup(
            "riddle 3 - inventory scanner",
            merge(
                indexed("scanner", 5),
                List.of(
                    "scanner_display",
                    "scanner_lever",
                    "scanner_terminal",
                    "keypad_transportlager"))));
    groups.add(
        new PointGroup(
            "riddle 4 - transport storage",
            merge(
                indexed("band", 5),
                List.of(
                    "band_start",
                    "band_ende",
                    "lager_terminal",
                    "lager_roboter",
                    "display_storage"))));
    groups.add(
        new PointGroup(
            "riddle 5 - manual sorting",
            List.of(
                "sort_data_0",
                "sort_data1",
                "sort_data2",
                "sort_data3",
                "sort_data4",
                "sort_compare_display",
                "sort_trigger",
                ARCHIVE_KEY_SPAWN)));
    groups.add(new PointGroup("riddle 6 - bubble sort", List.of("sort_machine")));
    groups.add(
        new PointGroup(
            "riddle 7 - data archive",
            merge(
                List.of("archive_shelf_energie", "archive_shelf_module", "archive_shelf_aktiv"),
                indexed("archive_node", 3))));
    groups.add(
        new PointGroup(
            "riddle 8 - two-dimensional storage",
            merge(
                indexedMatrix("storage_cell_", 3, 4),
                List.of("display_2d", "storage_terminal", SEARCH_PROGRAM_CHIP))));
    groups.add(
        new PointGroup(
            "riddle 9 - search robot",
            List.of(
                "suchroboter",
                "suchroboter_controller",
                "roboter_start",
                "roboter_end",
                SYSTEM_CORE_ACCESS_MODULE_DESTINATION)));
    groups.add(
        new PointGroup(
            "riddle 10 - system core",
            merge(
                merge(
                    merge(List.of("core_terminal", "core_display"), indexed("b", 5)),
                    indexed("mod", 5)),
                List.of("map00", "map24"))));
    groups.add(
        new PointGroup(
            "story triggers",
            List.of(
                "dialog_trigger_module_storage",
                "dialog_trigger_inventory_scanner",
                "dialog_trigger_transport_storage",
                "dialog_trigger_manual_sorting",
                "dialog_trigger_data_archive",
                "dialog_trigger_two_dimensional_storage",
                "dialog_trigger_system_core")));
    return List.copyOf(groups);
  }

  private static List<String> indexed(String prefix, int count) {
    List<String> names = new ArrayList<>();
    for (int index = 0; index < count; index++) {
      names.add(prefix + index);
    }
    return names;
  }

  private static List<String> indexedMatrix(String prefix, int rows, int columns) {
    List<String> names = new ArrayList<>();
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        names.add(prefix + row + "_" + column);
      }
    }
    return names;
  }

  private static List<String> merge(List<String> first, List<String> second) {
    List<String> merged = new ArrayList<>(first);
    merged.addAll(second);
    return merged;
  }

  private static Point resolvePoint(DungeonLevel level, String canonicalName) {
    try {
      return level.getPoint(canonicalName);
    } catch (java.util.NoSuchElementException exception) {
      return switch (canonicalName) {
        case "band_ende" -> level.getPoint("baned_end");
        case "suchroboter_controller" -> level.getPoint("suchroboter_controlls");
        case "roboter_item_destination" -> level.getPoint("roboter_item_destionation");
        case "storage_cell_2_2" -> level.getPoint("storage_2_2");
        default -> throw exception;
      };
    }
  }

  private record PointGroup(String riddle, List<String> pointNames) {
    private PointGroup {
      pointNames = List.copyOf(pointNames);
    }
  }
}
