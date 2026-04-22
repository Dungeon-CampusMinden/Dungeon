package contrib.editor.level.mode;

import contrib.editor.level.LevelEditorSystem;
import core.input.MouseButtons;
import core.level.Tile;
import core.level.utils.LevelElement;
import contrib.debug.systems.DebugDrawSystem;
import core.utils.InputManager;
import core.utils.Point;
import core.utils.Vector2;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * A level editor mode for painting and editing tiles in a dungeon level.
 *
 * <p>TilesMode provides a brush-based interface for modifying the level layout by changing tile
 * elements. Editors can select different tile types for left and right mouse buttons and paint
 * them onto the level with variable brush sizes.
 *
 * <p>Supported operations:
 * <ul>
 *   <li>Selecting tile elements for left and right painting
 *   <li>Adjusting brush size (1-7 tiles)
 *   <li>Painting with the left mouse button and selected left tile
 *   <li>Painting with the right mouse button and selected right tile
 *   <li>Erasing tiles to SKIP level elements
 *   <li>Pipetting tile types from existing tiles in the level
 *   <li>Visual preview of brush coverage
 * </ul>
 *
 * <p>Brush behavior:
 * <ul>
 *   <li>Left-click paints with the selected left tile using the current brush size
 *   <li>Right-click paints with the selected right tile using brush size 1 and selects the right tile when held
 *   <li>Brush size ranges from 1 to 7, with a diamond-shaped (manhattan distance) coverage pattern
 *   <li>Tertiary key erases tiles to SKIP with brush size 1
 * </ul>
 */
public final class TilesMode extends LevelEditorMode {

  private static final int MAX_BRUSH_SIZE = 7;

  private int selectedTileIndexL = 1;
  private int selectedTileIndexR = 2;
  private int brushSize = 1;

  public TilesMode(LevelEditorSystem system) {
    super(system, "Tiles Mode");
  }

  @Override
  protected void execute() {
    if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      if (InputManager.isButtonPressed(MouseButtons.RIGHT)) {
        selectedTileIndexR -= 1;
      } else {
        selectedTileIndexL -= 1;
      }
    } else if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      if (InputManager.isButtonPressed(MouseButtons.RIGHT)) {
        selectedTileIndexR += 1;
      } else {
        selectedTileIndexL += 1;
      }
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      brushSize = Math.min(MAX_BRUSH_SIZE, brushSize + 1);
    } else if (InputManager.isKeyJustPressed(SECONDARY_DOWN)) {
      brushSize = Math.max(1, brushSize - 1);
    }

    if (InputManager.isKeyJustPressed(QUATERNARY)) {
      tileElementAtCursor()
        .ifPresent(
          element -> {
            selectedTileIndexL = element.ordinal();
            system().showModeFeedback(
              "Picked tile " + element.name() + " for left paint", Color.WHITE);
          });
    }

    if (InputManager.isButtonPressed(MouseButtons.LEFT)) {
      applyBrush(selectedLeftElement(), brushSize);
    } else if (InputManager.isButtonPressed(MouseButtons.RIGHT)) {
      applyBrush(selectedRightElement(), 1);
    } else if (InputManager.isKeyPressed(TERTIARY)) {
      applyBrush(LevelElement.SKIP, 1);
    }
  }

  @Override
  public void render(Graphics2D g, float deltaSeconds) {
    Color previewColor = new Color(255, 255, 255, 64);

    forEachBrushTile(
      currentPreviewBrushSize(),
      tilePos ->
        DebugDrawSystem.drawRectangleOutline(
          tilePos.x(),
          tilePos.y(),
          1.0f,
          1.0f,
          previewColor));
  }

  @Override
  protected List<String> getStatusLines() {
    Point cursor = system().snappedCursorTileForModes();

    return List.of(
      "Cursor tile: (" + (int) cursor.x() + ", " + (int) cursor.y() + ")",
      "Left paint: " + selectedLeftElement().name(),
      "Right paint: " + selectedRightElement().name(),
      "Brush size: " + brushSize);
  }

  @Override
  protected Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(PRIMARY_UP, "Next left tile");
    controls.put(PRIMARY_DOWN, "Previous left tile");
    controls.put(SECONDARY_UP, "Brush +1");
    controls.put(SECONDARY_DOWN, "Brush -1");
    controls.put(MouseButtons.LEFT, "Paint with left tile");
    controls.put(MouseButtons.RIGHT, "Paint with right tile / hold for right-tile selection");
    controls.put(TERTIARY, "Erase to SKIP");
    controls.put(QUATERNARY, "Pipette tile to left paint");
    return controls;
  }

  private void applyBrush(LevelElement element, int targetBrushSize) {
    system()
      .currentDungeonLevelForModes()
      .ifPresent(
        level ->
          forEachBrushTile(
            targetBrushSize,
            targetPos ->
              level.tileAt(targetPos)
                .ifPresent(tile -> level.changeTileElementType(tile, element))));
  }

  private void forEachBrushTile(int targetBrushSize, java.util.function.Consumer<Point> consumer) {
    if (consumer == null) {
      return;
    }

    int normalizedBrushSize = Math.max(1, targetBrushSize);
    Point cursorPos = system().snappedCursorTileForModes();

    for (int dx = -normalizedBrushSize + 1; dx < normalizedBrushSize; dx++) {
      for (int dy = -normalizedBrushSize + 1; dy < normalizedBrushSize; dy++) {
        if (Math.abs(dx) + Math.abs(dy) >= normalizedBrushSize) {
          continue;
        }

        consumer.accept(cursorPos.translate(Vector2.of(dx, dy)));
      }
    }
  }

  private int currentPreviewBrushSize() {
    if (InputManager.isButtonPressed(MouseButtons.RIGHT) || InputManager.isKeyPressed(TERTIARY)) {
      return 1;
    }

    return this.brushSize;
  }

  private Optional<LevelElement> tileElementAtCursor() {
    Point cursorPos = system().snappedCursorTileForModes();
    return system()
      .currentDungeonLevelForModes()
      .flatMap(level -> level.tileAt(cursorPos))
      .map(Tile::levelElement);
  }

  private LevelElement selectedLeftElement() {
    LevelElement[] values = LevelElement.values();
    return values[Math.floorMod(selectedTileIndexL, values.length)];
  }

  private LevelElement selectedRightElement() {
    LevelElement[] values = LevelElement.values();
    return values[Math.floorMod(selectedTileIndexR, values.length)];
  }
}
