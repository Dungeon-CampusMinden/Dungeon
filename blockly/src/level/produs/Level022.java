package level.produs;

import client.Client;
import contrib.hud.DialogUtils;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import entities.monster.BlocklyMonster;
import java.util.List;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this final level, the boss mirrors the hero's movements. The player must cleverly lure the
 * boss into the abyss to win.
 */
public class Level022 extends BlocklyLevel {
  private static boolean showText = true;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level022(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 22");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Variable
        "get_number",
        // Bedingung
        "logic_bossView_direction",
        // Kategorien
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    LevelManagementUtils.cameraFocusOn(new Coordinate(15, 8));
    LevelManagementUtils.centerHero();
    LevelManagementUtils.zoomDefault();
    LevelManagementUtils.heroViewDirection(Direction.RIGHT);
    Game.randomTile(LevelElement.DOOR).ifPresent(d -> ((DoorTile) d).close());
    Game.randomTile(LevelElement.EXIT).ifPresent(d -> ((ExitTile) d).close());

    BlocklyMonster.BLACK_KNIGHT
        .builder()
        .attackRange(0)
        .speed(Client.MOVEMENT_FORCE.x())
        .addToGame()
        .viewDirection(Direction.LEFT)
        .onDeath(
            // todo we shouldn't just end the game, we need a real end screen
            entity ->
                DialogUtils.showTextPopup(
                    "NEEEEEEEEEEEEEEEEIN! ICH WERDE MICH RÄCHEN!", "SIEG!", Game::exit))
        .build(customPoints().getFirst());

    Game.allTiles(LevelElement.PIT)
        .forEach(
            tile -> {
              ((PitTile) tile).timeToOpen(60);
              ((PitTile) tile).close();
            });

    if (showText) {
      DialogUtils.showTextPopup(
          "Jetzt ist Schluss mit lustig. Ich kopiere jede deiner Bewegungen, dann besiegst du mich nie.",
          "BOSS");
      showText = false;
    }
  }
}
