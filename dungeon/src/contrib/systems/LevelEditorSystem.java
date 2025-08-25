package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import contrib.utils.components.skill.SkillTools;
import core.Game;
import core.System;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.loader.DungeonSaver;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.Queue;

/**
 * The LevelEditorSystem is responsible for handling the level editor. It allows the user to change
 * the {@link DungeonLevel} layout by setting different tiles. The user can set the following tiles:
 * skip, pit, floor, wall, hole, exit, door, and custom points. The user can also fill an area with
 * floor tiles and save the current dungeon.
 */
public class LevelEditorSystem extends System {
  private static final int SKIP_BUTTON = Input.Keys.NUM_1;
  private static final int PIT_BUTTON = Input.Keys.NUM_2;
  private static final int FLOOR_BUTTON = Input.Keys.NUM_3;
  private static final int WALL_BUTTON = Input.Keys.NUM_4;
  private static final int HOLE_BUTTON = Input.Keys.NUM_5;
  private static final int EXIT_BUTTON = Input.Keys.NUM_6;
  private static final int DOOR_BUTTON = Input.Keys.NUM_7;
  private static final int CUSTOM_POINT = Input.Keys.NUM_8;
  private static final int FILL_WITH_FLOOR = Input.Keys.NUM_9;
  private static final int SAVE_BUTTON = Input.Keys.DPAD_DOWN;
  private static final int maxFillRange = 100;
  private static boolean active = true;

  /**
   * Gets the active status of the LevelEditorSystem.
   *
   * @return true if the LevelEditorSystem is active, false if not.
   */
  public static boolean active() {
    return active;
  }

  /**
   * Sets the active status of the LevelEditorSystem.
   *
   * @param active The active status to set.
   */
  public static void active(boolean active) {
    LevelEditorSystem.active = active;
  }

  @Override
  public void execute() {
    if (!active) {
      return;
    }
    if (Gdx.input.isKeyPressed(SKIP_BUTTON)) {
      setTile(LevelElement.SKIP);
    }
    if (Gdx.input.isKeyPressed(PIT_BUTTON)) {
      setTile(LevelElement.PIT);
    }
    if (Gdx.input.isKeyPressed(FLOOR_BUTTON)) {
      setTile(LevelElement.FLOOR);
    }
    if (Gdx.input.isKeyPressed(WALL_BUTTON)) {
      setTile(LevelElement.WALL);
    }
    if (Gdx.input.isKeyPressed(HOLE_BUTTON)) {
      setTile(LevelElement.HOLE);
    }
    if (Gdx.input.isKeyJustPressed(EXIT_BUTTON)) {
      setTile(LevelElement.EXIT);
    }
    if (Gdx.input.isKeyJustPressed(DOOR_BUTTON)) {
      setTile(LevelElement.DOOR);
    }
    if (Gdx.input.isKeyJustPressed(CUSTOM_POINT)) {
      setCustomPoint();
    }
    if (Gdx.input.isKeyJustPressed(SAVE_BUTTON)) {
      if (Game.currentLevel().orElse(null) instanceof DungeonLevel) {
        DungeonSaver.saveCurrentDungeon();
      } else {
        java.lang.System.out.println(Game.currentLevel().orElse(null).printLevel());
      }
    }
    if (Gdx.input.isKeyJustPressed(FILL_WITH_FLOOR)) {
      fillWithFloor();
    }
  }

  /**
   * Floods area with floor tiles. Only affects tiles within a certain range of the cursor and only
   * skip tiles. It uses a queue to flood the area.
   */
  private void fillWithFloor() {
    Point mosPos = SkillTools.cursorPositionAsPoint().translate(Vector2.of(-0.5f, -0.25f));

    Tile startTile = LevelSystem.level().orElse(null).tileAt(mosPos).orElse(null);
    if (startTile == null) {
      return;
    }
    Queue<Tile> queue = new java.util.LinkedList<>();
    queue.add(startTile);
    int range = 0;
    while (!queue.isEmpty() && range < maxFillRange) {
      Tile currentTile = queue.poll();
      if (currentTile == null) {
        continue;
      }
      if (currentTile.levelElement() == LevelElement.SKIP
          || currentTile.levelElement() == LevelElement.FLOOR) {
        LevelSystem.level().orElse(null).changeTileElementType(currentTile, LevelElement.FLOOR);

        Vector2[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
        for (Vector2 direction : directions) {
          Tile neighbourTile =
              currentTile
                  .level()
                  .tileAt(currentTile.coordinate().translate(direction))
                  .orElse(null);
          if (neighbourTile != null && !queue.contains(neighbourTile)) {
            queue.add(neighbourTile);
          }
        }
      }
      range++;
    }
  }

  private void setTile(LevelElement element) {
    Point mosPos = SkillTools.cursorPositionAsPoint().translate(Vector2.of(-0.5f, -0.25f));
    Tile mouseTile = LevelSystem.level().orElse(null).tileAt(mosPos).orElse(null);
    if (mouseTile == null) {
      return;
    }
    LevelSystem.level().orElse(null).changeTileElementType(mouseTile, element);
  }

  private void setCustomPoint() {
    Point mosPos = SkillTools.cursorPositionAsPoint().translate(Vector2.of(-0.5f, -0.25f));
    Tile mouseTile = LevelSystem.level().orElse(null).tileAt(mosPos).orElse(null);
    if (mouseTile == null) {
      return;
    }
    if (Game.currentLevel().orElse(null) instanceof DungeonLevel devDungeonLevel) {
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
