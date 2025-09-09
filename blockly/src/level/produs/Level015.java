package level.produs;

import components.AmmunitionComponent;
import contrib.components.AIComponent;
import contrib.hud.DialogUtils;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.MissingHeroException;
import entities.monster.BlocklyMonster;
import java.util.List;
import java.util.Random;
import java.util.Set;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/**
 * In this level, variables are introduced. Between 1 and 4 monsters spawn randomly. The player must
 * defeat all monsters, keep track of how many there were, and then enter the corresponding door.
 */
public class Level015 extends BlocklyLevel {
  private final Random random = new Random();
  private static boolean showText = true;
  private DoorTile door;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public Level015(LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Level 15");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Richtungen
        // Inventar und Charakter
        "drop_item",
        "Items",
        // Bedingung
        "logic_breadcrumbs_direction",
        "logic_clover_direction",
        "logic_bossView_direction",
        // Variable
        "get_number",
        // Kategorien
        "Sonstige");
  }

  @Override
  protected void onFirstTick() {
    LevelManagementUtils.fog(false);
    if (showText) {
      DialogUtils.showTextPopup(
          "Hier musst du mitzählen. Die Anzahl der Monster verrät dir welche Tür du nehmen musst. Ich geb dir noch ein paar Feuerballspruchrollen. Viel Erfolg!",
          "Kapitel 2: Flucht");
      showText = false;
    }
    LevelManagementUtils.centerHero();
    LevelManagementUtils.cameraFocusOn(new Coordinate(11, 7));
    LevelManagementUtils.heroViewDirection(Direction.UP);
    LevelManagementUtils.zoomDefault();
    Game.hero()
        .orElseThrow(MissingHeroException::new)
        .fetch(AmmunitionComponent.class)
        .orElseThrow()
        .currentAmmunition(4);
    final int[] counter = {0};
    BlocklyMonster.Builder hedgehogBuilder =
        BlocklyMonster.HEDGEHOG.builder().attackRange(0).addToGame();

    customPoints()
        .forEach(
            coordinate -> {
              if (counter[0] == 0 || random.nextBoolean()) {
                hedgehogBuilder.build(coordinate.toPoint());
                counter[0]++;
              }
            });

    Coordinate[] coords = {
      new Coordinate(12, 4), new Coordinate(17, 4), new Coordinate(22, 4), new Coordinate(27, 4)
    };

    DoorTile[] doors = new DoorTile[coords.length];
    for (int i = 0; i < coords.length; i++) {
      doors[i] = (DoorTile) Game.tileAt(coords[i]).orElse(null);
    }

    for (int i = 0; i < doors.length; i++) {
      doors[i].close();
      if (i == counter[0] - 1) door = doors[i];
    }
  }

  @Override
  protected void onTick() {
    if (Game.hero().isPresent()) {
      float x = Game.hero().get().fetch(PositionComponent.class).orElseThrow().position().x();
      if (x >= 11) LevelManagementUtils.cameraFocusHero();
    }

    if (Game.levelEntities(Set.of(AIComponent.class)).findAny().isPresent()) door.close();
    else door.open();
  }
}
