package produsAdvanced.level;

import contrib.components.LeverComponent;
import contrib.entities.LeverFactory;
import core.Entity;
import core.Game;
import core.level.elements.tile.DoorTile;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import java.util.List;
import level.AdvancedLevel;

/** Playground level to test things out. */
public class PlayGroundLevel extends AdvancedLevel {

  private LeverComponent plate;
  private DoorTile doorTile;

  /**
   * Call the parent constructor of a tile level with the given layout and design label. Set the
   * start tile of the hero to the given heroPos.
   *
   * @param layout 2D array containing the tile layout.
   * @param designLabel The design label for the level.
   * @param customPoints The custom points of the level.
   */
  public PlayGroundLevel(
      LevelElement[][] layout, DesignLabel designLabel, List<Coordinate> customPoints) {
    super(layout, designLabel, customPoints, "playground");
  }

  @Override
  protected void onFirstTick() {
    doorTile = (DoorTile) Game.tileAt(new Point(13, 3)).get();
    doorTile.close();
    Entity localplate = LeverFactory.pressurePlate(new Point(12, 3));
    plate = localplate.fetch(LeverComponent.class).get();
    Game.add(localplate);
    //     Game.add(TractorBeamFactory.createTractorBeam(new Point(6, 3), Direction.LEFT));
    //    Game.d(AdvancedFactory.moveableSphere(new Point(6, 3)));
  }

  @Override
  protected void onTick() {
    if (plate.isOn()) {
      doorTile.open();
    }
  }
}
