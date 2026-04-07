package core.platform.litiengine.levelEditor;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import core.platform.Platform;
import core.platform.litiengine.render.LitiengineCameraViews;
import core.utils.InputManager;
import core.utils.Point;
import core.level.utils.Coordinate;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** LITIENGINE level editor mode for creating, moving and deleting named points. */
public final class PointMode extends LevelEditorMode {

  private static final Color POINT_MARKER_COLOR = new Color(255, 196, 77, 220);
  private static final Color HELD_POINT_MARKER_COLOR = new Color(120, 220, 120, 230);
  private static final Color POINT_LABEL_COLOR = Color.WHITE;
  private static final int POINT_MARKER_MIN_PX = 8;
  private static final int POINT_MARKER_MAX_PX = 18;

  private EditorSnapMode snapMode = EditorSnapMode.OnGrid;
  private String heldPointName = null;

  public PointMode(core.platform.litiengine.systems.LitiengineLevelEditorSystem system) {
    super(system, "Point Mode");
  }

  @Override
  protected void execute() {
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
  protected Map<Integer, String> getControls() {
    Map<Integer, String> controls = new LinkedHashMap<>();
    controls.put(SECONDARY_UP, "Change snap mode");
    controls.put(core.input.MouseButtons.LEFT, "Place point / open name dialog");
    controls.put(core.input.MouseButtons.RIGHT, "Pick point / clone held point");
    controls.put(TERTIARY, "Delete point");
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
    final Point dialogSnapPos = snapPos;

    UIComponent dialogUI =
      DialogFactory.show(
        DialogContext.builder()
          .type(DialogType.DefaultTypes.FREE_INPUT)
          .put(DialogContextKeys.TITLE, "Add Named Point")
          .put(DialogContextKeys.QUESTION, "Name of new point")
          .build());

    dialogUI.registerCallback(
      DialogContextKeys.INPUT_CALLBACK,
      data -> {
        if (data instanceof String string) {
          String pointName = string.trim();
          if (!pointName.isEmpty()) {
            system()
              .currentDungeonLevelForModes()
              .ifPresent(level -> level.addNamedPoint(pointName, dialogSnapPos));
            system().showModeFeedback("Added point: " + pointName, new Color(120, 220, 120));
          }
        }
        UIUtils.closeDialog(dialogUI, true);
      });

    dialogUI.registerCallback(DialogContextKeys.ON_CANCEL, data -> UIUtils.closeDialog(dialogUI, true));
  }

  private void renderPointMarkers(Graphics2D g) {
    LitiengineCameraViews.View view = LitiengineCameraViews.get();
    if (view == null || view.tilePx() <= 0) {
      return;
    }

    Graphics2D g2 = (Graphics2D) g.create();
    try {
      system()
        .currentDungeonLevelForModes()
        .ifPresent(
          level -> {
            int levelHeight = level.layout().length;
            int markerSize =
              Math.clamp(view.tilePx() / 3, POINT_MARKER_MIN_PX, POINT_MARKER_MAX_PX);

            level.namedPoints()
              .forEach(
                (name, pos) ->
                  drawNamedPointMarker(g2, name, pos, levelHeight, view, markerSize));

            if (heldPointName != null) {
              drawHeldPointGhost(
                g2, heldPointName, currentSnapPosition(), levelHeight, view, markerSize);
            }
          });
    } finally {
      g2.dispose();
    }
  }

  private void drawNamedPointMarker(
    Graphics2D g,
    String name,
    Point pointPos,
    int levelHeight,
    LitiengineCameraViews.View view,
    int markerSize) {

    int tilePx = view.tilePx();
    int screenX = (int) Math.round(pointPos.x() * tilePx + view.offsetX() + tilePx * 0.5f);

    float screenTileY =
      levelHeight > 0 ? (levelHeight - 1 - pointPos.y()) * tilePx : pointPos.y() * tilePx;
    int screenY = (int) Math.round(screenTileY + view.offsetY() + tilePx * 0.5f);

    int radius = markerSize / 2;
    boolean heldPoint = name != null && name.equals(heldPointName);

    g.setColor(heldPoint ? HELD_POINT_MARKER_COLOR : POINT_MARKER_COLOR);
    g.fillOval(screenX - radius, screenY - radius, markerSize, markerSize);

    g.setColor(Color.BLACK);
    g.drawOval(screenX - radius, screenY - radius, markerSize, markerSize);

    g.setColor(POINT_LABEL_COLOR);
    g.drawString(name, screenX + radius + 4, screenY - 4);
  }

  private void drawHeldPointGhost(
    Graphics2D g,
    String name,
    Point pointPos,
    int levelHeight,
    LitiengineCameraViews.View view,
    int markerSize) {

    int tilePx = view.tilePx();
    int screenX = (int) Math.round(pointPos.x() * tilePx + view.offsetX() + tilePx * 0.5f);

    float screenTileY =
      levelHeight > 0 ? (levelHeight - 1 - pointPos.y()) * tilePx : pointPos.y() * tilePx;
    int screenY = (int) Math.round(screenTileY + view.offsetY() + tilePx * 0.5f);

    int radius = markerSize / 2;

    g.setColor(HELD_POINT_MARKER_COLOR);
    g.fillOval(screenX - radius, screenY - radius, markerSize, markerSize);

    g.setColor(Color.BLACK);
    g.drawOval(screenX - radius, screenY - radius, markerSize, markerSize);

    g.setColor(POINT_LABEL_COLOR);
    g.drawString(name + " (held)", screenX + radius + 4, screenY - 4);
  }
}
