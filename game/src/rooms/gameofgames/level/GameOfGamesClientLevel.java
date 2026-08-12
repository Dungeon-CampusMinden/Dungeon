package rooms.gameofgames.level;

import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import feature.entities.deco.Deco;
import java.util.List;
import java.util.Map;
import rooms.gameofgames.util.InteractionFeedback;

/** Client-side level setup for the Game of Games escape room. */
public class GameOfGamesClientLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "game-of-games-1";

  /**
   * Creates the first Game of Games client level.
   *
   * @param layout the tile layout loaded from the server level state
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level state
   * @param decorations static decorations loaded from the level state
   */
  public GameOfGamesClientLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }

  /**
   * Creates the first Game of Games client level.
   *
   * @param layout the tile layout loaded from the server level state
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level state
   */
  public GameOfGamesClientLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, LEVEL_NAME);
  }

  @Override
  protected void onTick() {
    InteractionFeedback.update();
  }
}
