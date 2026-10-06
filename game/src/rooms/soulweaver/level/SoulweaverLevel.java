package rooms.soulweaver.level;

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
import rooms.soulweaver.Soulweaver;

/** Level handler for the Programming 1 escape room. */
public class SoulweaverLevel extends DungeonLevel {

  private SoulweaverGolemRuntime runtime;
  private boolean continued;
  private final Set<Integer> introducedPlayers = new HashSet<>();

  private static final String LEVEL_NAME = "soulweaver";

  /**
   * Creates the Programming 1 level.
   *
   * @param layout tile layout loaded from the level asset
   * @param designLabel visual tile design
   * @param namedPoints named points loaded from the level asset
   * @param decorations static decorations loaded from the level asset
   */
  public SoulweaverLevel(
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
    SoulweaverGates.initialize(this);
  }

  SoulweaverGolemRuntime runtime() {
    return runtime;
  }

  @Override
  protected void onFirstTick() {
    SoulweaverTableAnchors.initialize(this);
    SoulweaverProgress.initialize();
    runtime = SoulweaverRoomElements.spawn(this);
    var checkpoint = Soulweaver.checkpoint();
    continued = checkpoint.isPresent();
    if (continued) runtime.restore(checkpoint.orElseThrow());
    else SoulweaverProgress.started("vessels", "Ordne jeder Eigenschaft ein passendes Gefäß zu.");
    SoulweaverAtmosphere.install();
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
                      SoulweaverStory.intro(), false, true, true, () -> {}, player.id()));
    if (runtime != null) runtime.tick();
  }
}
