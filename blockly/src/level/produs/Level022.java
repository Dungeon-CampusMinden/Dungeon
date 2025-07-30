package level.produs;

import contrib.components.HealthComponent;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.VelocityComponent;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import entities.BlocklyMonster;
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

    BlocklyMonster.BlocklyMonsterBuilder bossBuilder = BlocklyMonster.BLACK_KNIGHT.builder();
    bossBuilder.range(0);
    bossBuilder.speed(7.5f);
    bossBuilder.addToGame();
    bossBuilder.viewDirection(Direction.LEFT);
    bossBuilder.spawnPoint(customPoints().get(0).toCenteredPoint());

    Entity boss = bossBuilder.build().orElseThrow();
    boss.fetch(VelocityComponent.class).ifPresent(vc -> vc.maxSpeed(7.5f));
    boss.fetch(HealthComponent.class)
        .orElseThrow()
        .onDeath(
            entity -> {
              // todo we shouldnt just end the game, we need a real end screen
              DialogUtils.showTextPopup(
                  "NEEEEEEEEEEEEEEEEIN! ICH WERDE MICH RÄCHEN!", "SIEG!", Game::exit);
            });
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
