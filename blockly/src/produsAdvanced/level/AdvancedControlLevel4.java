package produsAdvanced.level;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.level.elements.tile.ExitTile;
import core.level.elements.tile.PitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import level.BlocklyLevel;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>Modify your own interaction and combat controls.
 *
 * @see produsAdvanced.riddles.MyPlayerController
 */
public class AdvancedControlLevel4 extends BlocklyLevel {
  private static boolean showMsg = true;
  private static String msg = "Wenn ich hier zu langsam bin,fall ich runter.";
  private static String titel = "Level 4";

  private ExitTile exit;
  Set<LeverComponent> leverComponentSet;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public AdvancedControlLevel4(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Control");
  }

  @Override
  protected void onFirstTick() {
    if (showMsg) DialogUtils.showTextPopup(msg, titel, () -> showMsg = false);
    leverComponentSet = new HashSet<>();
    customPoints()
        .forEach(
            coordinate -> {
              Entity lever = LeverFactory.createLever(coordinate.toCenteredPoint());
              leverComponentSet.add(lever.fetch(LeverComponent.class).get());
              Game.add(lever);
            });

    exit = (ExitTile) Game.endTile();
    exit.close();

    Game.allTiles(LevelElement.PIT)
        .forEach(
            tile -> {
              ((PitTile) tile).timeToOpen(90);
              ((PitTile) tile).close();
            });
  }

  @Override
  protected void onTick() {
    if (leverComponentSet.stream().allMatch(LeverComponent::isOn)) exit.open();
  }
}
