package feature.leveleditor;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.level.utils.Coordinate;
import engine.level.utils.LevelElement;
import engine.systems.input.InputManager;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.leveleditor.ui.LevelElementGrid;
import feature.leveleditor.ui.NumberSetting;
import feature.systems.DebugDrawSystem;
import feature.utils.CheckPatternPainter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** The TilesMode allows the user to place different types of tiles in the level editor. */
public class TilesMode extends LevelEditorMode {

  private static int selectedTileIndexL = 1;
  private static int selectedTileIndexR = 2;
  private static int brushSize = 1;
  private static final int MAX_BRUSH_SIZE = 7;
  private static final int GRID_COLUMNS = 4;

  /** Texture used for level elements that have no dedicated preview texture. */
  public static final String FALLBACK_TEXTURE = "dungeon/default/floor/floor_1.png";

  /** Preview textures shown on the tile buttons of the details panel. */
  private static final Map<LevelElement, String> TILE_TEXTURES =
      Map.of(
          LevelElement.SKIP, "dungeon/default/floor/empty.png",
          LevelElement.FLOOR, "dungeon/default/floor/floor_1.png",
          LevelElement.WALL, "dungeon/default/wall/top.png",
          LevelElement.HOLE, "dungeon/default/floor/floor_hole.png",
          LevelElement.EXIT, "dungeon/default/floor/floor_ladder.png",
          LevelElement.PIT, "dungeon/default/floor/pit_open.png",
          LevelElement.DOOR, "dungeon/default/door/top.png",
          LevelElement.PORTAL, "dungeon/default/portal/portal_block.png",
          LevelElement.GLASSWALL, "dungeon/default/portal/glasswall/glasswall_horizontal.png",
          LevelElement.GITTER, "dungeon/default/portal/gutter/gutter_horizontal.png");

  private LevelElementGrid grid = null;
  private NumberSetting brushSizeSetting = null;

  /**
   * Gets the path of the preview texture used for the given level element.
   *
   * @param element the level element.
   * @return the texture path, or {@link #FALLBACK_TEXTURE} if the element has no dedicated texture.
   */
  public static String texturePath(LevelElement element) {
    return TILE_TEXTURES.getOrDefault(element, FALLBACK_TEXTURE);
  }

  /** Constructs a new TilesMode. */
  public TilesMode() {
    super("Tiles Mode");
  }

  @Override
  public String getHeader() {
    return "Level Tiles";
  }

  @Override
  public void buildDetailsUI(Table content) {
    content.clearChildren();
    grid =
        new LevelElementGrid(
            GRID_COLUMNS,
            TilesMode::texturePath,
            () -> selectedTileIndexL,
            () -> selectedTileIndexR,
            ordinal -> selectedTileIndexL = ordinal,
            ordinal -> selectedTileIndexR = ordinal);
    brushSizeSetting =
        new NumberSetting(
            "Brush Size", 1, MAX_BRUSH_SIZE, () -> brushSize, size -> brushSize = size);

    content.add(grid).growX().row();
    content.add(brushSizeSetting).growX().padTop(10f).row();
  }

  @Override
  public void updateDetailsUI() {
    if (grid != null) grid.refresh();
    if (brushSizeSetting != null) brushSizeSetting.refresh();
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
    if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      if (InputManager.isButtonPressed(Input.Buttons.RIGHT)) {
        selectedTileIndexR -= 1;
      } else {
        selectedTileIndexL -= 1;
      }
    } else if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      if (InputManager.isButtonPressed(Input.Buttons.RIGHT)) {
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

    if (InputManager.isKeyJustPressed(QUARTERNARY)) {
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
    if (InputManager.isButtonPressed(Input.Buttons.LEFT)) {
      levelElement =
          Optional.of(
              LevelElement.values()[
                  Math.floorMod(selectedTileIndexL, LevelElement.values().length)]);
      targetBrushSize = brushSize;
    } else {
      targetBrushSize = 1;
      if (InputManager.isButtonPressed(Input.Buttons.RIGHT)) {
        levelElement =
            Optional.of(
                LevelElement.values()[
                    Math.floorMod(selectedTileIndexR, LevelElement.values().length)]);
      } else if (InputManager.isKeyPressed(TERTIARY)) {
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
  public String additionalInformation() {
    StringBuilder status = new StringBuilder();
    LevelElement[] elements = LevelElement.values();
    status
        .append("Selected [L]: ")
        .append(elements[Math.floorMod(selectedTileIndexL, elements.length)].name())
        .append("\nSelected [R]: ")
        .append(elements[Math.floorMod(selectedTileIndexR, elements.length)].name());
    Point cursorPos = getCursorPosition();
    if (getLevel() == null) return status.toString();
    getLevel()
        .tileAt(cursorPos)
        .ifPresent(
            tile -> {
              status.append("\n");
              Coordinate c = cursorPos.toCoordinate();
              status
                  .append("Tile under cursor (")
                  .append(c.x())
                  .append(",")
                  .append(c.y())
                  .append("):");
              status
                  .append("\n")
                  .append(tile.levelElement().name())
                  .append("  |  ")
                  .append(tile.texturePath().pathString());
              if (tile.tintColor() == -1) {
                status.append("\nTint: (---)");
              } else {
                Color tintColor = new Color(tile.tintColor());
                status.append(
                    String.format(
                        "\nTint: (%.1f, %.1f, %.1f, %.1f)",
                        tintColor.r, tintColor.g, tintColor.b, tintColor.a));
              }
            });

    return status.toString();
  }

  @Override
  public Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(Input.Buttons.LEFT, "Place Tile [L]");
    controls.put(Input.Buttons.RIGHT, "Place Tile [R]");
    controls.put(TERTIARY, "Place SKIP Tile");
    controls.put(QUARTERNARY, "Pick tile from cursor");
    return controls;
  }
}
