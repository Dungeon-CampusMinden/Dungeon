package systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.utils.components.skill.SkillTools;
import core.Game;
import core.System;
import core.level.Tile;
import core.level.elements.tile.SkipTile;
import core.level.elements.tile.TileFactory;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.TileTextureFactory;
import core.systems.LevelSystem;
import core.utils.Point;

public class LevelEditor extends System {
  private static boolean active = true;
  private static int SKIP_BUTTON = Input.Keys.NUM_1;
  private static int FLOOR_BUTTON = Input.Keys.NUM_2;
  private static int WALL_BUTTON = Input.Keys.NUM_3;
  private static int HOLE_BUTTON = Input.Keys.NUM_4;
  private static int EXIT_BUTTON = Input.Keys.NUM_5;
  private static int DOOR_BUTTON = Input.Keys.NUM_6;

  public static boolean active() {
    return active;
  }

  public static void active(boolean active) {
    LevelEditor.active = active;
  }

  @Override
  public void execute() {
    if (!active) {
      return;
    }
    if (Gdx.input.isKeyJustPressed(SKIP_BUTTON)) {
      setTile(LevelElement.SKIP);
    }
    if (Gdx.input.isKeyJustPressed(FLOOR_BUTTON)) {
      setTile(LevelElement.FLOOR);
    }
    if (Gdx.input.isKeyJustPressed(WALL_BUTTON)) {
      setTile(LevelElement.WALL);
    }
    if (Gdx.input.isKeyJustPressed(HOLE_BUTTON)) {
      setTile(LevelElement.HOLE);
    }
    if (Gdx.input.isKeyJustPressed(EXIT_BUTTON)) {
      setTile(LevelElement.EXIT);
    }
    if (Gdx.input.isKeyJustPressed(DOOR_BUTTON)) {
      setTile(LevelElement.DOOR);
    }
  }

  private void setTile(LevelElement element) {
    Point mosPos = SkillTools.cursorPositionAsPoint();
    mosPos = new Point(mosPos.x - 0.5f, mosPos.y - 0.25f);
    Tile mouseTile = LevelSystem.level().tileAt(mosPos);
    if (mouseTile == null) {
      return;
    }
    LevelSystem.level().changeTileElementType(mouseTile, element);
  }
}
