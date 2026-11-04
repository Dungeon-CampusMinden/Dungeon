package feature.leveleditor;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import engine.input.ActionId;
import engine.input.GameInput;
import engine.level.DungeonLevel;
import engine.level.Tile;
import engine.level.utils.LevelElement;
import engine.systems.CameraSystem;
import engine.utils.FontHelper;
import engine.utils.Point;
import feature.systems.DebugDrawSystem;
import feature.systems.LevelEditorSystem;
import java.util.LinkedHashMap;
import java.util.Map;

/** The StartTilesMode allows the user to place and manage start tiles in the level editor. */
public class StartTilesMode extends LevelEditorMode {

  private static final Color[] START_TILE_COLORS = {
    Color.GREEN,
    Color.BLUE,
    Color.YELLOW,
    Color.CYAN,
    Color.MAGENTA,
    Color.ORANGE,
    Color.PINK,
    Color.LIME,
    Color.SKY,
    Color.SALMON
  };

  private static final BitmapFont FONT = FontHelper.getDefaultFont(16);

  private static int currentStartTileIndex = 0;

  /**
   * Constructs a new StartTilesMode.
   *
   * @param levelChangedCallback invoked after start tiles are changed.
   */
  public StartTilesMode(Runnable levelChangedCallback) {
    super("Start Tiles Mode", levelChangedCallback);
  }

  @Override
  public void execute() {
    // Primary Up/Down to change selected start tile index. The index can be between 0 and the lists
    // size. if its equal to the lists size, it adds a new point to the list.
    int maxIndex = getLevel().startTiles().size();
    if (GameInput.justPressed(EditorActions.PRIMARY_NEXT)) {
      currentStartTileIndex = (currentStartTileIndex + 1) % (maxIndex + 1);
    } else if (GameInput.justPressed(EditorActions.PRIMARY_PREV)) {
      currentStartTileIndex = Math.floorMod(currentStartTileIndex - 1, maxIndex + 1);
    }

    // Mouse interactions:
    // - LMB: set the selected start tile to the cursor position. if the index is equal to the list
    // size, add a new start tile at the cursor position.
    // - RMB: remove the start tile at the cursor position, if it exists.
    if (GameInput.justPressed(EditorActions.PLACE)) {
      setStartTile();
    } else if (GameInput.justPressed(EditorActions.ALT)) {
      removeStartTile();
    }
  }

  @Override
  public void onEnter() {}

  @Override
  public void onExit() {}

  @Override
  public String additionalInformation() {
    DungeonLevel level = getLevel();
    StringBuilder status = new StringBuilder();
    // List all start tiles with index + position. Add an entry for "New" at the end.
    for (int i = 0; i < level.startTiles().size(); i++) {
      Tile tile = level.startTiles().get(i);
      Point position = tile.position();
      if (i != 0) {
        status.append("\n");
      }
      status
          .append(i + 1)
          .append(": (")
          .append(position.x())
          .append(", ")
          .append(position.y())
          .append(")");
      if (i == currentStartTileIndex) {
        status.append(" <");
      }
      // Render a colored square in the world
      Color color = START_TILE_COLORS[i % START_TILE_COLORS.length];
      String label = "Start: " + (i + 1);
      Point textPosition = position.translate(0.5f, 0.5f);
      Vector3 screen =
          CameraSystem.camera().project(new Vector3(textPosition.x(), textPosition.y(), 0));
      GlyphLayout textSize = new GlyphLayout(FONT, label);
      DebugDrawSystem.drawText(
          FONT,
          label,
          new Point(screen.x - textSize.width / 2f, screen.y + textSize.height / 2f),
          color);
      DebugDrawSystem.drawRectangleOutline(position.x(), position.y(), 1, 1, color);
    }
    status.append("\n").append("Add Start Point");
    if (level.startTiles().size() == currentStartTileIndex) {
      status.append(" <");
    }
    return status.toString();
  }

  @Override
  public Map<ActionId, String> getControls() {
    Map<ActionId, String> controls = new LinkedHashMap<>();
    controls.put(EditorActions.PLACE, "Place Start Tile");
    controls.put(EditorActions.ALT, "Delete Start Tile on Cursor");
    controls.put(EditorActions.PRIMARY_NEXT, "Next Start Tile Index");
    controls.put(EditorActions.PRIMARY_PREV, "Prev Start Tile Index");
    return controls;
  }

  private void setStartTile() {
    Point cursorPos = getCursorPosition();
    DungeonLevel level = getLevel();
    Tile tile = level.tileAt(cursorPos).orElse(null);
    if (tile == null || tile.levelElement() != LevelElement.FLOOR) {
      LevelEditorSystem.showFeedback(
          "Start tile must be within the level bounds and on a FLOOR tile!", Color.RED);
      return;
    }
    if (currentStartTileIndex == level.startTiles().size()) {
      level.startTiles().add(tile);
      levelChanged();
    } else {
      if (level.startTiles().get(currentStartTileIndex) != tile) {
        level.startTiles().set(currentStartTileIndex, tile);
        levelChanged();
      }
    }
  }

  private void removeStartTile() {
    DungeonLevel level = getLevel();
    int maxIndex = level.startTiles().size();
    Point cursorPos = getCursorPosition();
    if (maxIndex == 1) {
      LevelEditorSystem.showFeedback("Cannot remove the last start tile.", Color.YELLOW);
      return;
    }
    Tile tile = level.tileAt(cursorPos).orElse(null);
    if (tile == null) {
      LevelEditorSystem.showFeedback("No start tile found under cursor.", Color.YELLOW);
      return;
    }
    if (!level.startTiles().remove(tile)) return;
    if (currentStartTileIndex == maxIndex) {
      currentStartTileIndex = Math.max(0, maxIndex - 1);
    }
    levelChanged();
  }
}
