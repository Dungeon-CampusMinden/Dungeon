package contrib.editor.level.mode;

import contrib.components.UIComponent;
import contrib.editor.level.LevelEditorSystem;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import core.Entity;
import core.Game;
import core.input.InputLabel.InputCode;
import core.platform.Platform;
import core.camera.CameraViewportState;
import core.game.render.TileOverlaySizing;
import core.utils.InputManager;
import core.utils.Point;
import core.level.utils.Coordinate;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * A level editor mode for creating and managing named points within a dungeon level.
 *
 * <p>PointMode enables editors to place, manipulate, and visualize named reference points in the
 * level. These points serve as markers for important locations, spawn points, objectives, or other
 * level-specific references.
 *
 * <p>Supported operations:
 * <ul>
 *   <li>Placing new named points at cursor positions
 *   <li>Picking up and moving existing points
 *   <li>Cloning points with automatic numbering
 *   <li>Deleting points from the level
 *   <li>Switching between different snap modes for flexible positioning
 *   <li>Visual rendering of point markers with labels
 * </ul>
 *
 * <p>The mode provides visual feedback with color-coded markers:
 * <ul>
 *   <li>Orange markers for existing points
 *   <li>Green markers for held/moved points
 *   <li>White labels for point names
 * </ul>
 *
 * <p>When placing a new point, the editor is prompted to enter a name via a dialog.
 * Held points can be cloned by right-clicking, with automatic numbering applied to the clone.
 */
public final class PointMode extends LevelEditorMode {

  private static final Color POINT_MARKER_COLOR = new Color(255, 196, 77, 220);
  private static final Color HELD_POINT_MARKER_COLOR = new Color(120, 220, 120, 230);
  private static final Color POINT_LABEL_COLOR = Color.WHITE;
  private static final int POINT_MARKER_MIN_PX = 8;
  private static final int POINT_MARKER_MAX_PX = 18;

  private EditorSnapMode snapMode = EditorSnapMode.ON_GRID;
  private String heldPointName = null;

  private UIComponent addPointDialog;

  /**
   * Constructs a new PointMode for managing and rendering point markers in the level editor.
   *
   * @param system The LevelEditorSystem instance to which this mode belongs, providing necessary
   *               context and functionality for the mode's operations.
   */
  public PointMode(LevelEditorSystem system) {
    super(system, "Point Mode");
  }

  @Override
  protected void execute() {
    if (addPointDialog != null && addPointDialog.isVisible()) {
      return;
    }

    if (InputManager.isKeyJustPressed(SECONDARY_UP)) {
      snapMode = snapMode.nextMode();
      system().showModeFeedback("Snap mode: " + snapMode.displayName(), Color.WHITE);
    }

    Point cursorPos = Platform.cursor().world();
    Point snapPos = currentSnapPosition();

    if (InputManager.isButtonJustPressed(core.input.MouseButtons.LEFT)) {
      if (heldPointName != null) {
        system()
          .currentDungeonLevelForModes()
          .ifPresent(level -> level.addNamedPoint(heldPointName, snapPos));
        system().showModeFeedback("Placed point: " + heldPointName, new Color(120, 220, 120));
        heldPointName = null;
      } else {
        openAddNamedPointDialog(snapPos);
      }
      return;
    }

    if (InputManager.isButtonJustPressed(core.input.MouseButtons.RIGHT)) {
      Optional<String> clickedPoint = findNamedPointAt(cursorPos);
      clickedPoint.ifPresent(point -> heldPointName = point);

      if (heldPointName == null) {
        system().showModeFeedback("No point to pick up on coordinate!", new Color(255, 220, 120));
      } else if (clickedPoint.isEmpty()) {
        system()
          .currentDungeonLevelForModes()
          .ifPresent(
            level -> {
              String baseName = heldPointName.replaceAll("\\d+$", "");
              String newPointName = baseName + (level.getHighestPointNumber(baseName) + 1);
              level.addNamedPoint(newPointName, snapPos);
              system().showModeFeedback(
                "Cloned point: " + newPointName, new Color(120, 220, 120));
            });
      } else {
        system().showModeFeedback("Picked point: " + heldPointName, new Color(120, 220, 120));
      }
      return;
    }

    if (InputManager.isKeyPressed(TERTIARY)) {
      findNamedPointAt(cursorPos)
        .ifPresent(
          pointName ->
            system()
              .currentDungeonLevelForModes()
              .ifPresent(
                level -> {
                  level.removeNamedPoint(pointName);
                  system().showModeFeedback(
                    "Removed point: " + pointName, new Color(255, 180, 180));
                }));
    }
  }

  @Override
  public void render(Graphics2D g, float deltaSeconds) {
    renderPointMarkers(g);
  }

  @Override
  public void onExit() {
    heldPointName = null;

    if (addPointDialog != null) {
      UIUtils.closeDialog(addPointDialog, true);
      InputManager.consumeTypedCharacters();
      addPointDialog = null;
    }
  }

  @Override
  protected List<String> getStatusLines() {
    Point cursor = currentSnapPosition();

    int totalPoints =
      system()
        .currentDungeonLevelForModes()
        .map(level -> level.namedPoints().size())
        .orElse(0);

    return List.of(
      "Cursor point: (" + cursor.x() + ", " + cursor.y() + ")",
      "Snap mode: " + snapMode.displayName(),
      "Held point: " + (heldPointName == null ? "<none>" : heldPointName),
      "Total points: " + totalPoints);
  }

  @Override
  protected Map<InputCode, String> getControls() {
    Map<InputCode, String> controls = new LinkedHashMap<>();
    controls.put(key(SECONDARY_UP), "Change snap mode");
    controls.put(mouseButton(core.input.MouseButtons.LEFT), "Place point / open name dialog");
    controls.put(mouseButton(core.input.MouseButtons.RIGHT), "Pick point / clone held point");
    controls.put(key(TERTIARY), "Delete point");
    return controls;
  }

  private Point currentSnapPosition() {
    return snapMode.getPosition(Platform.cursor().world());
  }

  private Optional<String> findNamedPointAt(Point worldPos) {
    if (worldPos == null) {
      return Optional.empty();
    }

    Coordinate toCheck = worldPos.toCoordinate();
    return system()
      .currentDungeonLevelForModes().flatMap(level -> level.namedPoints().entrySet().stream()
        .filter(entry -> entry.getValue().toCoordinate().equals(toCheck))
        .map(Map.Entry::getKey)
        .findFirst());
  }

  private void openAddNamedPointDialog(Point snapPos) {
    InputManager.consumeTypedCharacters();

    if (addPointDialog != null && addPointDialog.isVisible()) {
      return;
    }

    Entity player = Game.player().orElse(null);

    DialogContext context =
      DialogContext.builder()
        .type(DialogType.DefaultTypes.FREE_INPUT)
        .put(DialogContextKeys.TITLE, "Add Named Point")
        .put(DialogContextKeys.QUESTION, "Name of new point")
        .build();

    UIComponent dialogUI =
      player != null
        ? DialogFactory.show(context, player.id())
        : DialogFactory.show(context);

    InputManager.consumeTypedCharacters();
    addPointDialog = dialogUI;

    dialogUI.registerCallback(
      DialogContextKeys.INPUT_CALLBACK,
      data -> {
        if (data instanceof String string && !string.isBlank()) {
          system()
            .currentDungeonLevelForModes()
            .ifPresent(level -> level.addNamedPoint(string, snapPos));
          system().showModeFeedback("Added point: " + string, new Color(120, 220, 120));
        }

        UIUtils.closeDialog(dialogUI, true);
        InputManager.consumeTypedCharacters();
        addPointDialog = null;
      });

    dialogUI.registerCallback(
      DialogContextKeys.ON_CANCEL,
      _ -> {
        UIUtils.closeDialog(dialogUI, true);
        addPointDialog = null;
      });

    dialogUI.onClose(_ -> addPointDialog = null);
  }

  private void renderPointMarkers(Graphics2D g) {
    activeCameraView()
      .ifPresent(
        view ->
          system()
            .currentDungeonLevelForModes()
            .ifPresent(
              level -> {
                int markerSize =
                  TileOverlaySizing.scaledPixelsClamped(
                    view.tilePx(),
                    1f / 3f,
                    POINT_MARKER_MIN_PX,
                    POINT_MARKER_MAX_PX);

                level.namedPoints()
                  .forEach((name, pos) -> drawNamedPointMarker(g, name, pos, markerSize));

                if (heldPointName != null) {
                  drawHeldPointGhost(g, heldPointName, currentSnapPosition(), markerSize);
                }
              }));
  }

  private void drawNamedPointMarker(
    Graphics2D g,
    String name,
    Point pointPos,
    int markerSize) {

    Point screenCenter = CameraViewportState.worldCenterToScreen(pointPos);
    boolean heldPoint = name != null && name.equals(heldPointName);

    drawMarker(
      g,
      screenCenter,
      markerSize,
      heldPoint ? HELD_POINT_MARKER_COLOR : POINT_MARKER_COLOR);

    int radius = markerSize / 2;
    drawLabel(
      g,
      name,
      new Point(screenCenter.x() + radius + 4, screenCenter.y() - 4)
    );
  }

  private void drawHeldPointGhost(
    Graphics2D g,
    String name,
    Point pointPos,
    int markerSize) {

    Point screenCenter = CameraViewportState.worldCenterToScreen(pointPos);

    drawMarker(
      g,
      screenCenter,
      markerSize,
      HELD_POINT_MARKER_COLOR);

    int radius = markerSize / 2;
    drawLabel(
      g,
      name + " (held)",
      new Point(screenCenter.x() + radius + 4, screenCenter.y() - 4)
    );
  }

  private void drawMarker(Graphics2D g, Point center, int size, Color fill) {
    int radius = size / 2;
    int x = Math.round(center.x()) - radius;
    int y = Math.round(center.y()) - radius;

    Color old = g.getColor();
    g.setColor(fill);
    g.fillOval(x, y, size, size);
    g.setColor(Color.BLACK);
    g.drawOval(x, y, size, size);
    g.setColor(old);
  }

  private void drawLabel(Graphics2D g, String text, Point pos) {
    Color old = g.getColor();
    g.setColor(PointMode.POINT_LABEL_COLOR);
    g.drawString(text, Math.round(pos.x()), Math.round(pos.y()));
    g.setColor(old);
  }
}
