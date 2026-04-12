package contrib.editor.level.mode;

import contrib.editor.level.systems.LitiengineLevelEditorSystem;
import core.input.MouseButtons;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.camera.LitiengineCameraViews;
import core.platform.litiengine.render.LitiengineOverlaySizing;
import contrib.debug.systems.LitiengineDebugDrawSystem;
import core.utils.InputManager;
import core.utils.Point;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** LITIENGINE level editor mode for editing start tiles. */
public final class StartTilesMode extends LevelEditorMode {

  private static final Color[] START_TILE_COLORS = {
    Color.GREEN,
    Color.BLUE,
    Color.YELLOW,
    Color.CYAN,
    Color.MAGENTA,
    Color.ORANGE,
    Color.PINK,
    new Color(50, 205, 50),   // lime
    new Color(135, 206, 235), // sky
    new Color(250, 128, 114)  // salmon
  };

  private int currentStartTileIndex = 0;

  public StartTilesMode(LitiengineLevelEditorSystem system) {
    super(system, "Start Tiles Mode");
  }

  @Override
  protected void execute() {
    int maxIndex =
      system()
        .currentDungeonLevelForModes()
        .map(level -> level.startTiles().size())
        .orElse(0);

    if (InputManager.isKeyJustPressed(PRIMARY_UP)) {
      currentStartTileIndex = (currentStartTileIndex + 1) % (maxIndex + 1);
    } else if (InputManager.isKeyJustPressed(PRIMARY_DOWN)) {
      currentStartTileIndex = Math.floorMod(currentStartTileIndex - 1, maxIndex + 1);
    }

    if (InputManager.isButtonJustPressed(MouseButtons.LEFT)) {
      setStartTileAtCursor();
    } else if (InputManager.isButtonJustPressed(MouseButtons.RIGHT)) {
      removeStartTileAtCursor();
    }
  }

  @Override
  public void render(Graphics2D g, float deltaSeconds) {
    activeCameraView()
      .ifPresent(
        view ->
          system()
            .currentDungeonLevelForModes()
            .ifPresent(
              level -> {
                int tilePx = view.tilePx();

                for (int i = 0; i < level.startTiles().size(); i++) {
                  Tile tile = level.startTiles().get(i);
                  Point pos = tile.position();

                  Color color = START_TILE_COLORS[i % START_TILE_COLORS.length];

                  LitiengineDebugDrawSystem.drawRectangleOutline(
                    pos.x(),
                    pos.y(),
                    1.0f,
                    1.0f,
                    color);

                  Point screenTopLeft = LitiengineCameraViews.worldToScreen(pos);
                  Point labelPos =
                    new Point(
                      screenTopLeft.x() + 4,
                      screenTopLeft.y() + LitiengineOverlaySizing.scaledPixels(tilePx, 0.5f, 14));

                  LitiengineDebugDrawSystem.drawText(
                    "Start: " + (i + 1),
                    labelPos,
                    color);
                }
              }));
  }

  @Override
  public void onEnter() {
    currentStartTileIndex =
      system()
        .currentDungeonLevelForModes()
        .map(level -> Math.min(currentStartTileIndex, level.startTiles().size()))
        .orElse(0);
  }

  @Override
  protected List<String> getStatusLines() {
    return system()
      .currentDungeonLevelForModes()
      .<List<String>>map(
        level -> {
          int size = level.startTiles().size();
          int shownIndex = Math.min(currentStartTileIndex, size);

          if (currentStartTileIndex < size) {
            Tile selected = level.startTiles().get(currentStartTileIndex);
            Point pos = selected.position();
            return List.of(
              "Selected start tile slot: " + (shownIndex + 1),
              "Existing start tiles: " + size,
              "Selected position: (" + (int) pos.x() + ", " + (int) pos.y() + ")",
              "Placement is only valid on FLOOR tiles.");
          }

          return List.of(
            "Selected start tile slot: " + (shownIndex + 1),
            "Existing start tiles: " + size,
            "Selected slot: <new start tile>",
            "Placement is only valid on FLOOR tiles.");
        })
      .orElse(List.of("No dungeon level loaded."));
  }

  @Override
  protected Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(PRIMARY_UP, "Next start tile slot");
    controls.put(PRIMARY_DOWN, "Previous start tile slot");
    controls.put(MouseButtons.LEFT, "Place or replace selected start tile");
    controls.put(MouseButtons.RIGHT, "Delete start tile on cursor");
    return controls;
  }

  private void setStartTileAtCursor() {
    Point cursorPos = system().snappedCursorTileForModes();

    system()
      .currentDungeonLevelForModes()
      .ifPresent(
        level -> {
          Tile tile = level.tileAt(cursorPos).orElse(null);
          if (tile == null || tile.levelElement() != LevelElement.FLOOR) {
            system().showModeFeedback(
              "Start tile must be within the level bounds and on a FLOOR tile!",
              Color.RED);
            return;
          }

          if (currentStartTileIndex == level.startTiles().size()) {
            level.startTiles().add(tile);
            system().showModeFeedback(
              "Added start tile " + (currentStartTileIndex + 1),
              START_TILE_COLORS[currentStartTileIndex % START_TILE_COLORS.length]);
          } else {
            level.startTiles().set(currentStartTileIndex, tile);
            system().showModeFeedback(
              "Updated start tile " + (currentStartTileIndex + 1),
              START_TILE_COLORS[currentStartTileIndex % START_TILE_COLORS.length]);
          }
        });
  }

  private void removeStartTileAtCursor() {
    system()
      .currentDungeonLevelForModes()
      .ifPresent(
        level -> {
          int maxIndex = level.startTiles().size();
          Point cursorPos = system().snappedCursorTileForModes();

          if (maxIndex <= 1) {
            system().showModeFeedback("Cannot remove the last start tile.", Color.YELLOW);
            return;
          }

          Tile tile = level.tileAt(cursorPos).orElse(null);
          if (tile == null) {
            system().showModeFeedback("No start tile found under cursor.", Color.YELLOW);
            return;
          }

          boolean removed = level.startTiles().remove(tile);
          if (!removed) {
            system().showModeFeedback("No start tile found under cursor.", Color.YELLOW);
            return;
          }

          if (currentStartTileIndex > level.startTiles().size()) {
            currentStartTileIndex = Math.max(0, level.startTiles().size());
          }

          system().showModeFeedback("Removed start tile.", Color.YELLOW);
        });
  }
}
