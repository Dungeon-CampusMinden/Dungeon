package rooms.soulweaver.level;

import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import feature.entities.deco.Deco;
import feature.prefabs.PrefabInstance;
import java.util.List;
import java.util.Map;

/** Client-side level handler for the Programming 1 escape room. */
public class SoulweaverClientLevel extends DungeonLevel {

  private List<PrefabInstance> anchorSources = List.of();

  private static final String LEVEL_NAME = "soulweaver";

  /**
   * Creates the client representation of the Programming 1 level.
   *
   * @param layout the level's tile grid
   * @param designLabel the base tile theme
   * @param namedPoints markers used by the room logic
   * @param decorations decorations stored in the level file
   */
  public SoulweaverClientLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(
        SoulweaverDecisionWorld.layout(
            SoulweaverWorkshopWorld.layout(
                SoulweaverMazeWorld.layout(layout, namedPoints), namedPoints),
            namedPoints),
        designLabel,
        namedPoints,
        decorations,
        LEVEL_NAME);
    SoulweaverTerminal.reset();
    SoulweaverMethods.reset();
    SoulweaverDecisions.reset();
    SoulweaverGates.initialize(this);
  }

  @Override
  protected void onFirstTick() {
    SoulweaverTableAnchors.initialize(this);
    anchorSources = List.copyOf(activePrefabs());
    SoulweaverAtmosphere.install();
  }

  @Override
  protected void onTick() {
    var sources = activePrefabs();
    if (sources.equals(anchorSources)) return;
    SoulweaverTableAnchors.refresh(this);
    anchorSources = List.copyOf(sources);
  }
}
