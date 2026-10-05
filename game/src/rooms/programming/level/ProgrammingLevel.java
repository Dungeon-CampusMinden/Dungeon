package rooms.programming.level;

import engine.Game;
import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.entities.deco.Deco;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import rooms.programming.Programming;

/** Level handler for the Programming 1 escape room. */
public class ProgrammingLevel extends DungeonLevel {

  private ProgrammingGolemRuntime runtime;
  private boolean continued;
  private final Set<Integer> introducedPlayers = new HashSet<>();

  private static final String LEVEL_NAME = "programming-1";

  /**
   * Creates the Programming 1 level.
   *
   * @param layout tile layout loaded from the level asset
   * @param designLabel visual tile design
   * @param namedPoints named points loaded from the level asset
   * @param decorations static decorations loaded from the level asset
   */
  public ProgrammingLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(
        ProgrammingDecisionWorld.layout(
            ProgrammingWorkshopWorld.layout(
                ProgrammingMazeWorld.layout(layout, namedPoints), namedPoints),
            namedPoints),
        designLabel,
        namedPoints,
        decorations,
        LEVEL_NAME);
    ProgrammingGates.initialize(this);
  }

  ProgrammingGolemRuntime runtime() {
    return runtime;
  }

  @Override
  protected void onFirstTick() {
    ProgrammingTableAnchors.initialize(this);
    ProgrammingProgress.initialize();
    runtime = ProgrammingRoomElements.spawn(this);
    var checkpoint = Programming.checkpoint();
    continued = checkpoint.isPresent();
    if (continued) runtime.restore(checkpoint.orElseThrow());
    else ProgrammingProgress.started("vessels", "Ordne jeder Eigenschaft ein passendes Gefäß zu.");
    ProgrammingAtmosphere.install();
  }

  @Override
  protected void onTick() {
    if (runtime != null && runtime.tickEnding()) return;
    if (!continued)
      Game.allPlayers()
          .filter(player -> introducedPlayers.add(player.id()))
          .forEach(
              player ->
                  BlackFadeCutscene.show(
                      ProgrammingStory.intro(), false, true, true, () -> {}, player.id()));
    if (runtime != null) runtime.tick();
  }
}
