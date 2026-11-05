package rooms.morphco.level;

import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.rendering.Atmosphere;
import engine.rendering.Light;
import engine.utils.Point;
import engine.utils.Tuple;
import feature.entities.deco.Deco;
import java.util.List;
import java.util.Map;

/**
 * Client side of the Morph & Co. level.
 *
 * <p>All gameplay objects arrive from the server. The client only renders the factory lighting,
 * including the red alarm light while the synchronized alarm light entity exists.
 */
public class MorphCoClientLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "morph-co";

  /**
   * Creates the client representation of the level.
   *
   * @param layout tile layout
   * @param designLabel tile design
   * @param namedPoints named points of the level file
   * @param decorations static decorations of the level file
   */
  public MorphCoClientLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }

  @Override
  public Atmosphere atmosphere() {
    return MorphCoLighting.atmosphere();
  }

  @Override
  public void lights(List<Light> out) {
    MorphCoLighting.lights(layout(), out);
  }
}
