package aiAdvanced.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.System;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.utils.Point;

/**
 * This system is a mini version of {@link contrib.systems.LevelEditorSystem LevelEditorSystem} that
 * allows the user to edit the maze by clicking on the tiles. The left mouse button sets the tile to
 * a wall, and the right mouse button sets the tile to a floor.
 *
 * @see aiAdvanced.starter.PathfinderStarter PathfinderStarter
 */
public class MazeEditorSystem extends System {
  private static final int WALL_BUTTON = Input.Buttons.LEFT;
  private static final int FLOOR_BUTTON = Input.Buttons.RIGHT;

  @Override
  public void execute() {
    if (Gdx.input.isButtonPressed(FLOOR_BUTTON)) {
      setTile(LevelElement.FLOOR);
    } else if (Gdx.input.isButtonPressed(WALL_BUTTON)) {
      setTile(LevelElement.WALL);
    }
  }

  private void setTile(LevelElement element) {
    Point mosPos = SkillTools.cursorPositionAsPoint();
    mosPos = new Point(mosPos.x - 0.5f, mosPos.y - 0.25f);
    Tile mouseTile = LevelSystem.level().tileAt(mosPos);
    if (mouseTile == null) {
      return;
    }

    if (isImportantTile(mouseTile)) {
      return;
    }

    LevelSystem.level().changeTileElementType(mouseTile, element);
  }

  /**
   * Check if the tile is important. The important tiles are the exit tile and the tile where the
   * hero is located.
   *
   * @param tile the tile to check
   * @return true if the tile is important, false otherwise
   */
  private boolean isImportantTile(Tile tile) {
    Entity hero = Game.hero().orElse(null);
    if (hero == null) {
      return false;
    }

    return tile.levelElement() == LevelElement.EXIT || Game.tileAtEntity(hero) == tile;
  }
}
