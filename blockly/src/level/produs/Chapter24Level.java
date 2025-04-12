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
import core.utils.MissingHeroException;
import entities.BlocklyMonsterFactory;
import java.util.List;
import java.util.Random;
import java.util.Set;
import level.BlocklyLevel;
import level.LevelManagementUtils;

/** PRODUS LEVEL. */
public class Chapter24Level extends BlocklyLevel {
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
  public Chapter24Level(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Kapitel 2: Level 4");
    this.blockBlocklyElement(
        // MOVEMENT
        "goToExit",
        // Richtungen
        "direction_here",
        // Inventar und Charakter
        "wait",
        "drop_item",
        "Items",
        // Bedingung
        "logic_pit_direction",
        "logic_breadcrumbs_direction",
        "logic_clover_direction",
        // Variable
        "get_number",
        // Kategorien
        // "Sonstige");
        "func_def",
        "func_call",
        "var_array",
        "array_set",
        "array_get",
        "array_length");
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
    LevelManagementUtils.heroViewDirection(PositionComponent.Direction.UP);
    LevelManagementUtils.zoomDefault();
    Game.hero()
        .orElseThrow(MissingHeroException::new)
        .fetch(AmmunitionComponent.class)
        .orElseThrow()
        .currentAmmunition(4);
    final int[] counter = {0};
    customPoints()
        .forEach(
            coordinate -> {
              if (counter[0] == 0 || random.nextBoolean()) {
                Game.add(BlocklyMonsterFactory.hedgehog(coordinate));
                counter[0]++;
              }
            });

    Coordinate[] coords = {
      new Coordinate(12, 4), new Coordinate(17, 4), new Coordinate(22, 4), new Coordinate(27, 4)
    };

    DoorTile[] doors = new DoorTile[coords.length];
    for (int i = 0; i < coords.length; i++) {
      doors[i] = (DoorTile) Game.tileAT(coords[i]);
    }

    for (int i = 0; i < doors.length; i++) {
      doors[i].close();
      if (i == counter[0] - 1) door = doors[i];
    }
  }

  @Override
  protected void onTick() {
    if (Game.hero().isPresent()) {
      float x = Game.hero().get().fetch(PositionComponent.class).orElseThrow().position().x;
      if (x >= 11) LevelManagementUtils.cameraFocusHero();
    }

    if (Game.entityStream(Set.of(AIComponent.class)).findAny().isPresent()) door.close();
    else door.open();
  }
}
