package contrib.utils.systems.levelEditor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import contrib.systems.DebugDrawSystem;
import contrib.utils.CheckPatternPainter;
import core.level.utils.Coordinate;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.Vector2;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** The TilesMode allows the user to place different types of tiles in the level editor. */
public class TilesMode extends LevelEditorMode {

  private static int selectedTileIndexL = 1;
  private static int selectedTileIndexR = 2;
  private static int brushSize = 1;
  private static final int MAX_BRUSH_SIZE = 7;

  /** Constructs a new TilesMode. */
  public TilesMode() {
    super("Tiles Mode");
  }

  @Override
  public void render() {
    // Draw squares on all affected tiles via the DebugDrawSystem
    Point cursorPos = getCursorPosition();
    cursorPos = new Point((float) Math.floor(cursorPos.x()), (float) Math.floor(cursorPos.y()));
    for (int dx = -brushSize + 1; dx < brushSize; dx++) {
      for (int dy = -brushSize + 1; dy < brushSize; dy++) {
        // Ignore corners
        if (Math.abs(dx) + Math.abs(dy) >= brushSize) {
          continue;
        }
        Point targetPos = cursorPos.translate(Vector2.of(dx, dy));
        DebugDrawSystem.drawRectangleOutline(
            targetPos.x(), targetPos.y(), 1.0f, 1.0f, new Color(1, 1, 1, 0.2f));
      }
    }
  }

  @Override
  public void execute() {
    if (Gdx.input.isKeyJustPressed(PRIMARY_DOWN)) {
      if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
        selectedTileIndexR -= 1;
      } else {
        selectedTileIndexL -= 1;
      }
    } else if (Gdx.input.isKeyJustPressed(PRIMARY_UP)) {
      if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
        selectedTileIndexR += 1;
      } else {
        selectedTileIndexL += 1;
      }
    }
    if (Gdx.input.isKeyJustPressed(SECONDARY_UP)) {
      brushSize = Math.min(MAX_BRUSH_SIZE, brushSize + 1);
    } else if (Gdx.input.isKeyJustPressed(SECONDARY_DOWN)) {
      brushSize = Math.max(1, brushSize - 1);
    }

    if (Gdx.input.isKeyJustPressed(QUARTERNARY)) {
      // Pick tile under cursor to LMB
      Point cursorPos = getCursorPosition();
      getLevel()
          .tileAt(cursorPos)
          .ifPresent(
              t -> {
                LevelElement element = t.levelElement();
                selectedTileIndexL = element.ordinal();
              });
    }

    /* Mouse interactions:
     * - LMB place tile on cursor
     * - RMB place SKIP tile on cursor
     */
    Optional<LevelElement> levelElement = Optional.empty();
    int targetBrushSize;
    if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
      levelElement =
          Optional.of(
              LevelElement.values()[
                  Math.floorMod(selectedTileIndexL, LevelElement.values().length)]);
      targetBrushSize = brushSize;
    } else {
      targetBrushSize = 1;
      if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
        levelElement =
            Optional.of(
                LevelElement.values()[
                    Math.floorMod(selectedTileIndexR, LevelElement.values().length)]);
      } else if (Gdx.input.isKeyPressed(TERTIARY)) {
        levelElement = Optional.of(LevelElement.SKIP);
      }
    }
    levelElement.ifPresent(
        element -> {
          // Set tiles in a distance brush area. 1 = 1 tile, 2 = 1 tile + surrounding
          // tiles in x and
          // y independently (not a square), etc.
          Point cursorPos = getCursorPosition();
          for (int dx = -targetBrushSize + 1; dx < targetBrushSize; dx++) {
            for (int dy = -targetBrushSize + 1; dy < targetBrushSize; dy++) {
              // Ignore corners
              if (Math.abs(dx) + Math.abs(dy) >= targetBrushSize) {
                continue;
              }
              Point targetPos = cursorPos.translate(Vector2.of(dx, dy));
              setTile(targetPos, element);
            }
          }
          CheckPatternPainter.paintCheckerPattern(getLevel().layout());
        });
  }

  @Override
  public void onEnter() {}

  @Override
  public void onExit() {}

  @Override
  public String getStatusText() {
    StringBuilder status = new StringBuilder();
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

    // Get tile under cursor
    Point cursorPos = getCursorPosition();
    getLevel()
        .tileAt(cursorPos)
        .ifPresent(
            tile -> {
              Coordinate c = cursorPos.toCoordinate();
              status.append("\n\nCursor Tile:");
              status.append("\n- (").append(c.x()).append(", ").append(c.y()).append(")");
              status.append("\n- LevelElement: ").append(tile.levelElement().name());
              status.append("\n- Texture: ").append(tile.texturePath().pathString());
              if (tile.tintColor() == -1) {
                status.append("\n- TintColor RGBA: (---)");
              } else {
                Color tintColor = new Color(tile.tintColor() == -1 ? 0xFFFFFFFF : tile.tintColor());
                status.append(
                    String.format(
                        "\n- TintColor RGBA: (%.2f, %.2f, %.2f, %.2f)",
                        tintColor.r, tintColor.g, tintColor.b, tintColor.a));
              }
            });

    return status.toString();
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(PRIMARY_UP, "Next Tile");
    controls.put(PRIMARY_DOWN, "Prev Tile");
    controls.put(SECONDARY_UP, "Brush Size [L] +1");
    controls.put(SECONDARY_DOWN, "Brush Size [L] -1");
    controls.put(TERTIARY, "Place SKIP Tile");
    controls.put(QUARTERNARY, "Pick from cursor");
    controls.put(Input.Buttons.LEFT, "Place Tile [L]");
    controls.put(Input.Buttons.RIGHT, "Place Tile [R]");
    return controls;
  }
}
