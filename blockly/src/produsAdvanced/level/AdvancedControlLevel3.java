package produsAdvanced.level;

import contrib.components.AIComponent;
import contrib.components.LeverComponent;
import contrib.entities.EntityFactory;
import contrib.entities.LeverFactory;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import level.BlocklyLevel;

/**
 * Manager for an advanced dungeon level.
 *
 * <p>Write your own interaction and combat controls.
 *
 * @see produsAdvanced.riddles.MyPlayerController
 */
public class AdvancedControlLevel3 extends BlocklyLevel {
  private static boolean showMsg = true;
  private static String msg =
      "Ein Schalter, vielleicht einfach mal ziehen? Und sind das Monster?! Ich konnte doch mal zaubern...";
  private static String titel = "Level 3";
  private LeverComponent l1, l2, l3;
  private ExitTile exit;
  private DoorTile door1, door2;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public AdvancedControlLevel3(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "Control");
  }

  @Override
  protected void onFirstTick() {
    if (showMsg) DialogUtils.showTextPopup(msg, titel, () -> showMsg = false);
    exit = (ExitTile) Game.endTile();
    exit.close();
    Entity lever1 = LeverFactory.createLever(customPoints().get(0).toCenteredPoint());
    Entity lever2 = LeverFactory.createLever(customPoints().get(1).toCenteredPoint());
    Entity lever3 = LeverFactory.createLever(customPoints().get(2).toCenteredPoint());
    l1 = lever1.fetch(LeverComponent.class).get();
    l2 = lever2.fetch(LeverComponent.class).get();
    l3 = lever3.fetch(LeverComponent.class).get();
    Game.add(lever1);
    Game.add(lever2);
    Game.add(lever3);
    door1 = (DoorTile) Game.tileAT(customPoints().get(3));
    door2 = (DoorTile) Game.tileAT(customPoints().get(4));
    door1.close();
    door2.close();
    customPoints().remove(0);
    customPoints().remove(0);
    customPoints().remove(0);
    customPoints().remove(0);
    customPoints().remove(0);

    customPoints()
        .forEach(
            coordinate -> {
              try {
                Entity m = EntityFactory.randomMonster();
                m.fetch(PositionComponent.class).get().position(coordinate.toCenteredPoint());
                Game.add(m);

              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  @Override
  protected void onTick() {
    if (l1.isOn()) door1.open();
    else door1.close();
    if (l2.isOn() && l3.isOn()) door2.open();
    else door2.close();
    if (Game.entityStream(Set.of(AIComponent.class)).findAny().isEmpty()) exit.open();
  }
}
