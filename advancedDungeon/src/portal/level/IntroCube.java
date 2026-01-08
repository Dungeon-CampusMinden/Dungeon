package portal.level;

import contrib.entities.LeverFactory;
import contrib.utils.ICommand;
import core.Game;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import portal.physicsobject.PressurePlates;
import portal.portals.PortalColor;
import portal.portals.PortalFactory;
import portal.riddles.MyCubeSpawnerLeaver;
import portal.util.AdvancedLevel;

import java.util.Map;

public class IntroCube extends AdvancedLevel {


  public IntroCube(
    LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "IntroCube");
  }

  @Override
  protected void onFirstTick() {
    Game.add(PressurePlates.cubePressurePlate(getPoint("plate"),1));
    MyCubeSpawnerLeaver l = new MyCubeSpawnerLeaver();
    Game.add(LeverFactory.createLever(getPoint("spawner"), new ICommand() {
      @Override
      public void execute() {
        l.spawn(getPoint("spawner").translate(Vector2.of(1,0)));
      }

      @Override
      public void undo() {

      }
    }));


  }
}
