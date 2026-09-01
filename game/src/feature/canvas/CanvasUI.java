package feature.canvas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.Game;
import engine.utils.BaseContainerUI;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogDesign;
import java.util.List;
import java.util.Objects;

/**
 * The dialog shell around a {@link CanvasArea}.
 *
 * <p>Renders a viewport-filling window with an optional title and canvas-overlay controls. It also
 * owns the local persistence lifecycle: on construction it merges the server provided defaults with
 * the changes stored in the {@link CanvasStore}, and when the dialog leaves the stage it diffs the
 * live nodes against the defaults and stores the result, so the player's arrangement survives
 * closing and reopening the canvas.
 */
public class CanvasUI extends Group {

  /** Callback key fired when the player presses the close button. */
  public static final String EVENT_CLOSE = "canvasClose";

  private static final float VIEWPORT_MARGIN = 32f;
  private static final float CONTROL_MARGIN = 12f;
  private static final float CONTROL_SPACING = 8f;
  private static final float TITLE_HEIGHT = 48f;
  private static final float TITLE_BOTTOM_PADDING = 12f;

  private final CanvasArea area;
  private final CanvasOptions options;
  private final CanvasSnapshot defaults;
  private final CanvasSnapshot loadedChanges;
  private final String dialogId;

  /**
   * Creates the canvas dialog.
   *
   * @param canvasId the canvas id; used for overlay persistence
   * @param title the window title, may be empty
   * @param areaWidth the preferred viewport width in pixels
   * @param areaHeight the preferred viewport height in pixels
   * @param options the canvas configuration; must not be null
   * @param defaults the server provided default nodes; must not be null
   * @param dialogId the dialog id used for server events and closing
   */
  public CanvasUI(
      String canvasId,
      String title,
      float areaWidth,
      float areaHeight,
      CanvasOptions options,
      CanvasSnapshot defaults,
      String dialogId) {
    this(canvasId, title, areaWidth, areaHeight, options, defaults, dialogId, true, true);
  }

  /**
   * Creates the canvas dialog.
   *
   * @param canvasId the canvas id; used for overlay persistence
   * @param title the window title, may be empty
   * @param areaWidth the preferred viewport width in pixels
   * @param areaHeight the preferred viewport height in pixels
   * @param options the canvas configuration; must not be null
   * @param defaults the server provided default nodes; must not be null
   * @param dialogId the dialog id used for server events and closing
   * @param showResetViewButton whether to show the reset-view button
   * @param showFitButton whether to show the fit-to-content button
   */
  public CanvasUI(
      String canvasId,
      String title,
      float areaWidth,
      float areaHeight,
      CanvasOptions options,
      CanvasSnapshot defaults,
      String dialogId,
      boolean showResetViewButton,
      boolean showFitButton) {
    Objects.requireNonNull(canvasId, "canvasId");
    this.options = Objects.requireNonNull(options, "options");
    this.defaults = Objects.requireNonNull(defaults, "defaults");
    this.dialogId = dialogId;

    setSize(Game.windowWidth(), Game.windowHeight());

    this.area = new CanvasArea(canvasId, areaWidth, areaHeight, options);
    this.area.dialogId(dialogId);

    this.loadedChanges = CanvasStore.load(canvasId);
    for (NodeState state : defaults.mergeWith(loadedChanges, options).nodes()) {
      area.addNode(CanvasNodeType.create(state));
    }
    area.resetView();

    BaseContainerUI container = new BaseContainerUI(null, true, true);
    container.setFillParent(true);
    container.pad(VIEWPORT_MARGIN);
    container.setContent(buildWindow(title, showResetViewButton, showFitButton));
    addActor(container);
  }

  private Window buildWindow(String title, boolean showResetViewButton, boolean showFitButton) {
    Window window = new Window("", UIUtils.defaultSkin(), "no-title");
    window.setMovable(false);

    Table content = new Table();
    if (!title.isBlank()) {
      addTitle(content, title);
    }
    content.add(buildCanvas(showResetViewButton, showFitButton)).grow().row();

    window.add(content).grow();
    return window;
  }

  private void addTitle(Table content, String title) {
    Table titleTable = new Table();
    titleTable
        .add(
            Scene2dElementFactory.createLabel(
                title, DialogDesign.DIALOG_FONT_SPEC_TITLE.withColor(Color.BLACK)))
        .center()
        .row();
    content.add(titleTable).growX().height(TITLE_HEIGHT).padBottom(TITLE_BOTTOM_PADDING).row();
  }

  private Stack buildCanvas(boolean showResetViewButton, boolean showFitButton) {
    Stack stack = new Stack();
    stack.add(area);

    Table controls = new Table();
    controls.top().left();
    controls.pad(CONTROL_MARGIN);

    if (showResetViewButton) {
      TextButton reset = Scene2dElementFactory.createButton("Reset View", "blue-outline", 18);
      reset.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
              area.resetView();
            }
          });
      controls.add(reset).padRight(CONTROL_SPACING);
    }

    if (showFitButton) {
      TextButton fit = Scene2dElementFactory.createButton("Fit", "blue-outline", 18);
      fit.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
              area.zoomToFit();
            }
          });
      controls.add(fit);
    }

    controls.add().growX();

    TextButton close = Scene2dElementFactory.createButton("Close", "red-outline", 18);
    close.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            requestClose();
          }
        });
    controls.add(close);

    stack.add(controls);
    return stack;
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    setSize(Game.windowWidth(), Game.windowHeight());
    super.draw(batch, parentAlpha);
  }

  /** Persists the local changes and asks the server to close this dialog. */
  public void requestClose() {
    flush();
    if (dialogId != null) {
      DialogCallbackResolver.createButtonCallback(dialogId, EVENT_CLOSE).accept(null);
    }
  }

  /**
   * Diffs the live nodes against the server defaults and stores the result in the {@link
   * CanvasStore}.
   */
  public void flush() {
    List<NodeState> live = area.nodes().stream().map(CanvasNode::toState).toList();
    CanvasStore.save(
        area.canvasId(), CanvasSnapshot.changesOf(defaults, live, loadedChanges, options));
  }

  /**
   * Returns the hosted canvas viewport.
   *
   * @return the canvas area
   */
  public CanvasArea area() {
    return area;
  }

  /**
   * Returns the server provided default nodes this dialog was opened with.
   *
   * @return the default node snapshot
   */
  public CanvasSnapshot defaults() {
    return defaults;
  }

  @Override
  protected void setStage(Stage stage) {
    super.setStage(stage);
    if (stage == null) {
      flush();
    }
  }
}
