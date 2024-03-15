package systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.utils.components.skill.SkillTools;
import core.Game;
import core.System;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.utils.Point;
import level.DevDungeonLevel;
import level.utils.DungeonSaver;

public class LevelEditorSystem extends System {
  private static boolean active = true;
  private static final int SKIP_BUTTON = Input.Keys.NUM_1;
  private static final int PIT_BUTTON = Input.Keys.NUM_2;
  private static final int FLOOR_BUTTON = Input.Keys.NUM_3;
  private static final int WALL_BUTTON = Input.Keys.NUM_4;
  private static final int HOLE_BUTTON = Input.Keys.NUM_5;
  private static final int EXIT_BUTTON = Input.Keys.NUM_6;
  private static final int DOOR_BUTTON = Input.Keys.NUM_7;
  private static final int CUSTOM_POINT = Input.Keys.NUM_8;
  private static final int SAVE_BUTTON = Input.Keys.Y;

  public static boolean active() {
    return active;
  }

  public static void active(boolean active) {
    LevelEditorSystem.active = active;
  }

  @Override
  public void execute() {
    if (!active) {
      return;
    }
    if (Gdx.input.isKeyPressed(SKIP_BUTTON)) {
      this.setTile(LevelElement.SKIP);
    }
    if (Gdx.input.isKeyPressed(PIT_BUTTON)) {
      this.setTile(LevelElement.PIT);
    }
    if (Gdx.input.isKeyPressed(FLOOR_BUTTON)) {
      this.setTile(LevelElement.FLOOR);
    }
    if (Gdx.input.isKeyPressed(WALL_BUTTON)) {
      this.setTile(LevelElement.WALL);
    }
    if (Gdx.input.isKeyPressed(HOLE_BUTTON)) {
      this.setTile(LevelElement.HOLE);
    }
    if (Gdx.input.isKeyJustPressed(EXIT_BUTTON)) {
      this.setTile(LevelElement.EXIT);
    }
    if (Gdx.input.isKeyJustPressed(DOOR_BUTTON)) {
      this.setTile(LevelElement.DOOR);
    }
    if (Gdx.input.isKeyJustPressed(CUSTOM_POINT)) {
      this.setCustomPoint();
    }
    if (Gdx.input.isKeyJustPressed(SAVE_BUTTON)) {
      if (Game.currentLevel() instanceof DevDungeonLevel) {
        DungeonSaver.saveCurrentDungeon();
      } else {
        java.lang.System.out.println(Game.currentLevel().printLevel());
      }
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

  private void setCustomPoint() {
    Point mosPos = SkillTools.cursorPositionAsPoint();
    mosPos = new Point(mosPos.x - 0.5f, mosPos.y - 0.25f);
    Tile mouseTile = LevelSystem.level().tileAt(mosPos);
    if (mouseTile == null) {
      return;
    }
    if (Game.currentLevel() instanceof DevDungeonLevel devDungeonLevel) {
      if (devDungeonLevel.customPoints().contains(mouseTile.coordinate())) {
        java.lang.System.out.println("[-] Custom point: " + mouseTile.coordinate());
        devDungeonLevel.removeCustomPoint(mouseTile.coordinate());
      } else {
        java.lang.System.out.println("[+] Custom point: " + mouseTile.coordinate());
        devDungeonLevel.addCustomPoint(mouseTile.coordinate());
      }
    }
  }
}
