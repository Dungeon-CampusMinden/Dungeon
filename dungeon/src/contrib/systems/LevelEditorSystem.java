package contrib.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import contrib.utils.components.skill.SkillTools;
import core.Game;
import core.System;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.loader.DungeonSaver;
import core.level.utils.LevelElement;
import core.systems.LevelSystem;
import core.utils.*;

import java.util.Map;
import java.util.Optional;

/**
 * The LevelEditorSystem is responsible for handling the level editor. It allows the user to change
 * the {@link DungeonLevel} layout by setting different tiles. The user can set the following tiles:
 * skip, pit, floor, wall, hole, exit, door, and custom points. The user can also fill an area with
 * floor tiles and save the current dungeon.
 */
public class LevelEditorSystem extends System {

  private static final BitmapFont FONT = FontHelper.getDefaultFont(24);

  private static boolean internalStopped = false;
  private static boolean active = false;
  private static final int TOGGLE_ACTIVE = Input.Keys.F4;

  private static Mode currentMode = Mode.Tiles;
  private static final int MODE_1 = Input.Keys.NUM_1;
  private static final int MODE_2 = Input.Keys.NUM_2;
  private static final int MODE_3 = Input.Keys.NUM_3;
  private static final int MODE_4 = Input.Keys.NUM_4;
  private static final int MODE_5 = Input.Keys.NUM_5;
  private static final int SAVE_BUTTON = Input.Keys.NUM_0;

  private static final int ARROW_LEFT = Input.Keys.LEFT;
  private static final int ARROW_RIGHT = Input.Keys.RIGHT;
  private static final int ARROW_UP = Input.Keys.UP;
  private static final int ARROW_DOWN = Input.Keys.DOWN;

  /* Tile Mode Settings */
  private static int selectedTileIndexL = 1;
  private static int selectedTileIndexR = 0;
  private static int brushSize = 1;
  private static final int MAX_BRUSH_SIZE = 7;

  /* Deco Mode Settings */
  private static int selectedDecoIndex = 0;
  private static SnapMode decoSnapMode = SnapMode.OnGrid;

  /* Point Mode Settings */
  private static SnapMode pointSnapMode = SnapMode.OnGrid;

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
    if (Gdx.input.isKeyJustPressed(TOGGLE_ACTIVE)) {
      active = !active;
    }

    if (!active) {
      return;
    }

    Mode previousMode = currentMode;
    if (Gdx.input.isKeyPressed(MODE_1)) {
      currentMode = Mode.getMode(0);
    } else if (Gdx.input.isKeyPressed(MODE_2)) {
      currentMode = Mode.getMode(1);
    } else if (Gdx.input.isKeyPressed(MODE_3)) {
      currentMode = Mode.getMode(2);
    } else if (Gdx.input.isKeyPressed(MODE_4)) {
      currentMode = Mode.getMode(3);
    } else if (Gdx.input.isKeyPressed(MODE_5)) {
      currentMode = Mode.getMode(4);
    }

    if(!internalStopped || previousMode != currentMode) {
      switch (currentMode) {
        case Tiles -> executeTileMode();
        case Decos -> executeDecoMode();
        case Points -> executePointMode();
        case LevelBounds -> executeLevelBoundsMode();
        case ShiftLevel -> executeShiftLevelMode();
      }

      if (Gdx.input.isKeyJustPressed(SAVE_BUTTON)) {
        if (Game.currentLevel().orElse(null) instanceof DungeonLevel) {
          DungeonSaver.saveCurrentDungeon();
        } else {
          java.lang.System.out.println("Not a dungeon level.");
        }
      }
    }

    String status = switch (currentMode) {
      case Tiles -> getTileModeStatus();
      case Decos -> getDecoModeStatus();
      case Points -> getPointModeStatus();
      case LevelBounds -> getLevelBoundsModeStatus();
      case ShiftLevel -> getShiftLevelModeStatus();
    };
    // Prepend to status: mode selection info. a horizontal list of all mode numbers, separated by |. active mode is in [brackets]
    StringBuilder modeSelection = new StringBuilder("Level Editor v2 | Modes: ");
    for (int i = 0; i < Mode.values().length; i++) {
      if (i > 0) {
        modeSelection.append(" | ");
      }
      if (i == currentMode.ordinal()) {
        modeSelection.append("[").append(i + 1).append("]");
      } else {
        modeSelection.append(i + 1);
      }
    }
    modeSelection.append("\n");
    status = modeSelection + status;
    DebugDrawSystem.drawText(FONT, status, new Point(10.0f, Game.windowHeight() - 10.0f));

    // Draw level boundaries in green with alpha 0.3f
    ILevel level = LevelSystem.level().orElse(null);
    if (level != null) {
      Tile[][] layout = level.layout();
      DebugDrawSystem.drawRectangleOutline(
        0, 0, layout[0].length, layout.length,
        new Color(0, 1, 0, 0.3f));
    }
  }

  private String getTileModeStatus(){
    StringBuilder status = new StringBuilder("--- Edit Tiles Mode ---");
    status.append("\nControls:");
    status.append("\n- Up/Down to change tile");
    status.append("\n- Left/Right to change brush size");

    status.append("\n\nSettings:");
    status.append("\n[L] Brush Size: ").append(brushSize);
    for (int i = 0; i < LevelElement.values().length; i++) {
      LevelElement element = LevelElement.values()[i];
      boolean hasL = (i == Math.floorMod(selectedTileIndexL, LevelElement.values().length));
      boolean hasR = (i == Math.floorMod(selectedTileIndexR, LevelElement.values().length));
      status.append("\n").append(element.name());
      if (hasL) {
        status.append(" [L]");
      }
      if (hasR) {
        status.append(" [R]");
      }
    }

    return status.toString();
  }
  private void executeTileMode(){
    if (Gdx.input.isKeyJustPressed(ARROW_DOWN)) {
      if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
        selectedTileIndexR += 1;
      } else {
        selectedTileIndexL += 1;
      }
    } else if (Gdx.input.isKeyJustPressed(ARROW_UP)) {
      if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
        selectedTileIndexR -= 1;
      } else {
        selectedTileIndexL -= 1;
      }
    }
    if (Gdx.input.isKeyJustPressed(ARROW_LEFT)) {
      brushSize = Math.max(1, brushSize - 1);
    } else if (Gdx.input.isKeyJustPressed(ARROW_RIGHT)) {
      brushSize = Math.min(MAX_BRUSH_SIZE, brushSize + 1);
    }

    /* Mouse interactions:
     * - LMB place tile on cursor
     * - RMB place SKIP tile on cursor
     */
    Optional<LevelElement> levelElement = Optional.empty();
    int targetBrushSize;
    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
      levelElement = Optional.of(LevelElement.values()[Math.floorMod(selectedTileIndexL, LevelElement.values().length)]);
      targetBrushSize = brushSize;
    } else {
      targetBrushSize = 1;
      if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
        levelElement = Optional.of(LevelElement.values()[Math.floorMod(selectedTileIndexR, LevelElement.values().length)]);
      }
    }
    levelElement.ifPresent(element -> {
      // Set tiles in a distance brush area. 1 = 1 tile, 2 = 1 tile + surrounding tiles in x and y independently (not a square), etc.
      Point cursorPos = getCursorPosition();
      for (int dx = -targetBrushSize + 1; dx < targetBrushSize; dx++) {
        for (int dy = -targetBrushSize + 1; dy < targetBrushSize; dy++) {
          //Ignore corners
          if (Math.abs(dx) + Math.abs(dy) >= targetBrushSize) {
            continue;
          }
          Point targetPos = cursorPos.translate(Vector2.of(dx, dy));
          setTile(targetPos, element);
        }
      }
    });

    // Draw squares on all affected tiles via the DebugDrawSystem
    Point cursorPos = getCursorPosition();
    cursorPos = new Point((float)Math.floor(cursorPos.x()), (float)Math.floor(cursorPos.y()));
    for (int dx = -brushSize + 1; dx < brushSize; dx++) {
      for (int dy = -brushSize + 1; dy < brushSize; dy++) {
        // Ignore corners
        if (Math.abs(dx) + Math.abs(dy) >= brushSize) {
          continue;
        }
        Point targetPos = cursorPos.translate(Vector2.of(dx, dy));
        DebugDrawSystem.drawRectangleOutline(targetPos.x(), targetPos.y(), 1.0f, 1.0f, new Color(1, 1, 1, 0.2f));
      }
    }
  }

  private String getDecoModeStatus(){
    return "Deco Mode";
  }
  private void executeDecoMode(){

  }

  private String getPointModeStatus(){
    return "Point Mode";
  }
  private void executePointMode(){

  }

  private String getLevelBoundsModeStatus(){
    StringBuilder status = new StringBuilder("--- Edit Level Bounds Mode ---");
    status.append("\nControls:");
    status.append("\n- Up: increase height");
    status.append("\n- Down: decrease height");
    status.append("\n- Right: increase width");
    status.append("\n- Left: decrease width");
    return status.toString();
  }
  private void executeLevelBoundsMode(){
    if (Gdx.input.isKeyJustPressed(ARROW_UP)) {
      addSize(0, 1);
    } else if (Gdx.input.isKeyJustPressed(ARROW_DOWN)) {
      addSize(0, -1);
    }

    if (Gdx.input.isKeyJustPressed(ARROW_RIGHT)) {
      addSize(1, 0);
    } else if (Gdx.input.isKeyJustPressed(ARROW_LEFT)) {
      addSize(-1, 0);
    }
  }

  private String getShiftLevelModeStatus(){
    StringBuilder status = new StringBuilder("--- Edit Level Shift Mode ---");
    status.append("\nControls:");
    status.append("\n- Up: shift level up");
    status.append("\n- Down: shift level down");
    status.append("\n- Right: shift level right");
    status.append("\n- Left: shift level left");
    return status.toString();
  }
  private void executeShiftLevelMode(){
    if (Gdx.input.isKeyJustPressed(ARROW_UP)) {
      shiftLevel(0, 1);
    } else if (Gdx.input.isKeyJustPressed(ARROW_DOWN)) {
      shiftLevel(0, -1);
    }

    if (Gdx.input.isKeyJustPressed(ARROW_RIGHT)) {
      shiftLevel(1, 0);
    } else if (Gdx.input.isKeyJustPressed(ARROW_LEFT)) {
      shiftLevel(-1, 0);
    }
  }

  private Point getCursorPosition(){
//    return SkillTools.cursorPositionAsPoint().translate(Vector2.of(-0.5f, -0.25f));
    return SkillTools.cursorPositionAsPoint();
  }

  private void setTile(Point position, LevelElement element) {
    Tile tile = LevelSystem.level().orElse(null).tileAt(position).orElse(null);
    if (tile == null) {
      return;
    }
    LevelSystem.level().orElse(null).changeTileElementType(tile, element);
    // Also set the tiles around the position, to update their sprites for the new neighboring tile
    for (int dx = -1; dx <= 1; dx++) {
      for (int dy = -1; dy <= 1; dy++) {
        Point neighborPos = position.translate(Vector2.of(dx, dy));
        Tile neighborTile = LevelSystem.level().orElse(null).tileAt(neighborPos).orElse(null);
        if (neighborTile != null) {
          LevelSystem.level().orElse(null).changeTileElementType(neighborTile, neighborTile.levelElement());
        }
      }
    }
  }

  private void addSize(int addX, int addY){
    LOGGER.info("Resizing layout: x + "+addX+", y + "+addY);

    ILevel l = Game.currentLevel().orElse(null);

    if(l == null){
      LOGGER.warning("No current level to resize!");
      return;
    }

    Tile[][] layout = l.layout();

    int rows = layout.length;
    int cols = layout[0].length;

    int newRows = rows + addY;
    int newCols = cols + addX;

    LevelElement[][] newLayout = new LevelElement[newRows][newCols];

    for (int i = 0; i < newRows; i++) {
      for (int j = 0; j < newCols; j++) {
        if(i == rows || j == cols){
          newLayout[i][j] = LevelElement.SKIP;
        } else {
          newLayout[i][j] = layout[i][j].levelElement();
        }
      }
    }

    l.setLayout(newLayout);
  }

  private void shiftLevel(int x, int y){
    if(x == 0 && y == 0) return;

    LOGGER.info("Shifting level by: x="+x+", y="+y);

    ILevel l = Game.currentLevel().orElse(null);
    if(l == null){
      LOGGER.warning("No current level to shift!");
      return;
    }

    Tile[][] layout = l.layout();

    //ALGORITHM: shift all tiles in the layout by x and y, which are either 1, 0 or -1
    int rows = layout.length;
    int cols = layout[0].length;

    LevelElement[][] newLayout = new LevelElement[rows][cols];

    // Iterate through the current layout and shift tiles accordingly
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        int newI = i - y;
        int newJ = j - x;

        // Check if the new position is within bounds
        if (newI >= 0 && newI < rows && newJ >= 0 && newJ < cols) {
          newLayout[i][j] = layout[newI][newJ].levelElement();
        } else {
          newLayout[i][j] = LevelElement.SKIP; // Empty space for out-of-bounds tiles
        }
      }
    }

    // Copy the shifted layout back to the original layout
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        l.changeTileElementType(layout[i][j], newLayout[i][j]);
      }
    }

    //Set all tiles again to fix the sprites
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        l.changeTileElementType(layout[i][j], layout[i][j].levelElement());
      }
    }

    //Shift all named points
    Map<String, Point> namedPoints = Game.currentLevel().orElseThrow().namedPoints();
    namedPoints.replaceAll((s, p) -> new Point(p.x() + x, p.y() + y));
  }


  private void setCustomPoint() {}

  @Override
  public void stop() {
    internalStopped = true;
  }

  @Override
  public void run() {
    internalStopped = false;
  }

  private enum Mode {
    Tiles,
    Decos,
    Points,
    LevelBounds,
    ShiftLevel,
    ;

    public static Mode getMode(int number){
      if (number < 0 || number >= values().length){
        throw new IllegalArgumentException("Invalid mode number: " + number);
      }
      return values()[number];
    }
  }

  private enum SnapMode {
    OnGrid,
    OneTenthGrid,
    OffGrid,
    ;

    SnapMode previousMode(){
      return values()[(this.ordinal() - 1 + values().length) % values().length];
    }
    SnapMode nextMode(){
      return values()[(this.ordinal() + 1) % values().length];
    }

    Point getPosition(Point position){
      return switch (this) {
        case OnGrid -> new Point(Math.round(position.x()), Math.round(position.y()));
        case OneTenthGrid -> new Point(Math.round(position.x() * 10) / 10.0f, Math.round(position.y() * 10) / 10.0f);
        default -> position;
      };
    }
  }
}
