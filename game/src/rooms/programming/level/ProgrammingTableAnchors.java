package rooms.programming.level;

import engine.Game;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.prefabs.PrefabSpawner;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Resolves code-owned tabletop items from the furniture authored in the editor. */
final class ProgrammingTableAnchors {
  private record Anchor(String point, String table, Vector2 offset) {}

  private static final List<Anchor> ANCHORS =
      List.of(
          new Anchor("archive-instructions", "table-archive-instructions", Vector2.of(0.5f, 0.4f)),
          new Anchor("forge-maintenance-note", "table-forge-maintenance", Vector2.of(1.0f, 0.4f)),
          new Anchor("intro-tablet", "table-forge-intro", Vector2.of(1.0f, 0.4f)),
          new Anchor("loop-monitor", "table-archive-monitor", Vector2.of(0.5f, 0.4f)),
          new Anchor("loop-terminal", "table-archive-terminal", Vector2.of(0.5f, 0.4f)),
          new Anchor("methods-console", "table-workshop-console", Vector2.of(0.0f, 0.4f)),
          new Anchor("rune-archive-attack", "table-archive-rune-11", Vector2.of(1.15f, 0.4f)),
          new Anchor("rune-archive-backtrack", "table-archive-rune-8", Vector2.of(0.2f, 0.4f)),
          new Anchor("rune-archive-jump", "table-archive-rune-11", Vector2.of(0.2f, 0.4f)),
          new Anchor("rune-archive-long", "table-archive-rune-9", Vector2.of(0.2f, 0.4f)),
          new Anchor("rune-archive-patrol", "table-archive-rune-5", Vector2.of(1.2f, 0.4f)),
          new Anchor("rune-archive-short", "table-archive-rune-8", Vector2.of(1.15f, 0.4f)),
          new Anchor("rune-archive-spin", "table-archive-rune-9", Vector2.of(1.15f, 0.4f)),
          new Anchor("rune-archive-turn-left", "table-archive-rune-10", Vector2.of(1.15f, 0.4f)),
          new Anchor("rune-archive-turn-right", "table-archive-rune-10", Vector2.of(0.15f, 0.4f)),
          new Anchor("rune-bellows-do-while", "table-archive-rune-2", Vector2.of(0.2f, 0.4f)),
          new Anchor("rune-bellows-for", "table-archive-rune-2", Vector2.of(1.15f, 0.4f)),
          new Anchor("rune-bellows-while", "table-archive-rune-1", Vector2.of(1.15f, 0.4f)),
          new Anchor("rune-chain-lift-do-while", "table-archive-rune-3", Vector2.of(1.15f, 0.4f)),
          new Anchor("rune-chain-lift-for", "table-archive-rune-4", Vector2.of(0.2f, 0.4f)),
          new Anchor("rune-chain-lift-while", "table-archive-rune-3", Vector2.of(0.2f, 0.4f)),
          new Anchor(
              "rune-cooling-channel-do-while", "table-archive-rune-6", Vector2.of(0.2f, 0.4f)),
          new Anchor("rune-cooling-channel-for", "table-archive-rune-6", Vector2.of(1.15f, 0.4f)),
          new Anchor("rune-cooling-channel-while", "table-archive-rune-4", Vector2.of(1.15f, 0.4f)),
          new Anchor("rune-forge-press-do-while", "table-forge-rune-2", Vector2.of(0.0f, 0.4f)),
          new Anchor("rune-forge-press-for", "table-archive-rune-1", Vector2.of(0.2f, 0.4f)),
          new Anchor("rune-forge-press-while", "table-forge-rune-1", Vector2.of(0.6f, 0.4f)),
          new Anchor("rune-heart-gate-do-while", "table-archive-rune-7", Vector2.of(1.15f, 0.4f)),
          new Anchor("rune-heart-gate-for", "table-archive-rune-5", Vector2.of(0.2f, 0.4f)),
          new Anchor("rune-heart-gate-while", "table-archive-rune-7", Vector2.of(0.2f, 0.4f)),
          new Anchor("variables-translation", "table-forge-translation", Vector2.of(0.8f, 0.4f)),
          new Anchor("workshop-experiments", "table-workshop-console", Vector2.of(1.1f, 0.4f)),
          new Anchor("decisions-heart", "table-decisions-heart", Vector2.of(1.0f, 1.0f)),
          new Anchor("decisions-heart-fire", "table-decisions-heart", Vector2.of(0.0f, 0.5f)),
          new Anchor("decisions-rune-0", "table-decisions-rune-1", Vector2.of(0.1f, 0.3f)),
          new Anchor("decisions-rune-1", "table-decisions-rune-2", Vector2.of(0.1f, 0.3f)),
          new Anchor("decisions-rune-2", "table-decisions-rune-3", Vector2.of(0.1f, 0.3f)),
          new Anchor("decisions-rune-3", "table-decisions-rune-4", Vector2.of(0.1f, 0.3f)),
          new Anchor("decisions-rune-4", "table-decisions-rune-5", Vector2.of(0.1f, 0.3f)),
          new Anchor("decisions-rune-5", "table-decisions-rune-6", Vector2.of(0.1f, 0.3f)),
          new Anchor(
              "decisions-heart-offering-39.2", "table-decisions-heart", Vector2.of(-0.8f, 0.4f)),
          new Anchor(
              "decisions-heart-offering-42.0", "table-decisions-heart", Vector2.of(2.0f, 0.4f)),
          new Anchor(
              "decisions-heart-inscription", "table-decisions-heart", Vector2.of(0.6f, -1.0f)));

  private ProgrammingTableAnchors() {}

  // The parser attaches prefabs after construction. Validate before room logic reads the markers.
  static void initialize(DungeonLevel level) {
    refresh(level, true);
  }

  /**
   * Runs after editor respawns, even while normal gameplay ticks are paused.
   *
   * @param level level whose prefabs changed
   */
  static void changed(ILevel level) {
    if (level instanceof ProgrammingLevel programming)
      PrefabSpawner.afterChanges(level, () -> refresh(programming));
  }

  /**
   * Updates client markers without changing server-owned entities.
   *
   * @param level room whose table markers are refreshed
   */
  static void refresh(DungeonLevel level) {
    refresh(level, false);
  }

  private static void refresh(DungeonLevel level, boolean requireTables) {
    Map<String, Point> tables =
        level.prefabs(ProgrammingTablePrefab.class).stream()
            .collect(
                Collectors.toMap(
                    ProgrammingTablePrefab::name,
                    table ->
                        ProgrammingTablePrefab.POSITION.get(
                            table.currentInstance().orElseThrow())));
    if (requireTables)
      for (Anchor anchor : ANCHORS)
        if (!tables.containsKey(anchor.table()))
          throw new IllegalStateException("Missing Programming table: " + anchor.table());
    boolean authoritative =
        !Game.isMultiplayerClient()
            && Game.currentLevel().filter(current -> current == level).isPresent();
    var entities = authoritative ? Game.levelEntities().toList() : List.<engine.Entity>of();
    for (Anchor anchor : ANCHORS) {
      Point table = tables.get(anchor.table());
      // A live deletion leaves required puzzle items at their last usable position.
      if (table == null) continue;
      Point at = table.translate(anchor.offset());
      Point previous = level.namedPoints().put(anchor.point(), at);
      if (previous == null || previous.equals(at)) continue;
      for (var entity : entities) {
        if (!entity.name().equals("programming-" + anchor.point())) continue;
        entity
            .fetch(PositionComponent.class)
            .filter(position -> position.position().equals(previous))
            .ifPresent(position -> position.position(at));
      }
    }
    if (authoritative) ProgrammingProps.moveHeartTorch(level);
  }
}
