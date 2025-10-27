package contrib.utils.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import contrib.systems.DebugDrawSystem;
import contrib.systems.LevelEditorSystem;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.systems.CameraSystem;
import core.utils.Point;

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

  private static int currentStartTileIndex = 0;

  public StartTilesMode() {
    super("Start Tiles Mode");
  }

  @Override
  public void execute() {
    // Primary Up/Down to change selected start tile index. The index can be between 0 and the lists
    // size. if its equal
    // to the lists size, it adds a new point to the list.
    int maxIndex = getLevel().startTiles().size();
    if (Gdx.input.isKeyJustPressed(PRIMARY_UP)) {
      currentStartTileIndex = (currentStartTileIndex + 1) % (maxIndex + 1);
    } else if (Gdx.input.isKeyJustPressed(PRIMARY_DOWN)) {
      currentStartTileIndex = Math.floorMod(currentStartTileIndex - 1, maxIndex + 1);
    }

    // Mouse interactions:
    // - LMB: set the selected start tile to the cursor position. if the index is equal to the list
    // size, add a new start tile at the cursor position.
    // - RMB: remove the start tile at the cursor position, if it exists.
    if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
      setStartTile();
    } else if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
      removeStartTile();
    }
  }

  @Override
  public void onEnter() {}

  @Override
  public void onExit() {}

  @Override
  public String getStatusText() {
    DungeonLevel level = getLevel();
    StringBuilder status = new StringBuilder("--- Start Tiles Mode ---");
    status.append("\nControls:");
    status.append("\n- E/Q: Change start tile index");
    status.append("\n\nSettings:");
    // List all start tiles with index + position. Add an entry for "New" at the end.
    for (int i = 0; i < level.startTiles().size(); i++) {
      Tile tile = level.startTiles().get(i);
      Point position = tile.position();
      status
          .append("\n")
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
      GlyphLayout textSize = new GlyphLayout(LevelEditorSystem.FONT_SMALL, label);
      DebugDrawSystem.drawText(
          LevelEditorSystem.FONT_SMALL,
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

  private void setStartTile() {
    Point cursorPos = getCursorPosition();
    DungeonLevel level = getLevel();
    Tile tile = level.tileAt(cursorPos).orElse(null);
    if (currentStartTileIndex == level.startTiles().size()) {
      if (tile == null || tile.levelElement() != LevelElement.FLOOR) {
        LevelEditorSystem.showFeedback("Start tile must be within bounds and on FLOOR!", Color.RED);
        return;
      }
      level.startTiles().add(tile);
    } else {
      level.startTiles().set(currentStartTileIndex, tile);
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
    level.startTiles().remove(tile);
    if (currentStartTileIndex == maxIndex) {
      currentStartTileIndex = Math.max(0, maxIndex - 1);
    }
  }
}
