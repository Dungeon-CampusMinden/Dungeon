package entities.levercommands;

import core.Game;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import systems.FogOfWarSystem;
import utils.ICommand;

public class OpenPassageCommand implements ICommand {
  private final Coordinate topLeft;
  private final Coordinate bottomRight;
  private boolean isOpen = false;

  private LevelElement[][] originalTiles;

  public OpenPassageCommand(Coordinate topLeft, Coordinate bottomRight) {
    this.topLeft = topLeft;
    this.bottomRight = bottomRight;
  }

  @Override
  public void execute() {
    if (!this.isOpen) {
      this.originalTiles =
          new LevelElement[this.bottomRight.x - this.topLeft.x + 1]
              [this.topLeft.y - this.bottomRight.y + 1];
      for (int x = this.topLeft.x; x <= this.bottomRight.x; x++) {
        for (int y = this.bottomRight.y; y <= this.topLeft.y; y++) {
          Tile tile = Game.currentLevel().tileAt(new Coordinate(x, y));
          if (tile == null) return;
          this.originalTiles[x - this.topLeft.x][this.topLeft.y - y] = tile.levelElement();
          Game.currentLevel().changeTileElementType(tile, LevelElement.FLOOR);
          Tile newTile = Game.currentLevel().tileAt(new Coordinate(x, y));
          ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).updateTile(tile, newTile);
        }
      }
      this.isOpen = true;
    }
  }

  @Override
  public void undo() {
    if (this.isOpen) {
      for (int x = this.topLeft.x; x <= this.bottomRight.x; x++) {
        for (int y = this.bottomRight.y; y <= this.topLeft.y; y++) {
          Tile tile = Game.currentLevel().tileAt(new Coordinate(x, y));
          if (tile == null) return;
          LevelElement oldElement = this.originalTiles[x - this.topLeft.x][this.topLeft.y - y];
          Game.currentLevel().changeTileElementType(tile, oldElement);
          Tile newTile = Game.currentLevel().tileAt(new Coordinate(x, y));
          ((FogOfWarSystem) Game.systems().get(FogOfWarSystem.class)).updateTile(tile, newTile);
        }
      }
      // Workaround to fix wall textures
      for (int x = this.topLeft.x; x <= this.bottomRight.x; x++) {
        for (int y = this.bottomRight.y; y <= this.topLeft.y; y++) {
          Tile tile = Game.currentLevel().tileAt(new Coordinate(x, y));
          if (tile == null) return;
          Game.currentLevel().changeTileElementType(tile, tile.levelElement());
        }
      }
      this.isOpen = false;
    }
  }
}
