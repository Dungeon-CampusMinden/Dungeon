package feature.canvas;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import engine.Game;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogDesign;
import java.util.List;
import java.util.Objects;

/**
 * The dialog shell around a {@link CanvasArea}.
 *
 * <p>Renders a window with a title, a small toolbar for view controls and a close button, and hosts
 * the canvas viewport itself. It also owns the local persistence lifecycle: on construction it
 * merges the server provided defaults with the changes stored in the {@link CanvasStore}, and when
 * the dialog leaves the stage it diffs the live nodes against the defaults and stores the result,
 * so the player's arrangement survives closing and reopening the canvas.
 */
public class CanvasUI extends Group {

  /** Callback key fired when the player presses the close button. */
  public static final String EVENT_CLOSE = "canvasClose";

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
   * @param areaWidth the viewport width in pixels
   * @param areaHeight the viewport height in pixels
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

    Table root = new Table();
    root.setFillParent(true);
    addActor(root);
    root.add(buildWindow(title, areaWidth, areaHeight)).center();
  }

  private Window buildWindow(String title, float areaWidth, float areaHeight) {
    Window window = new Window("", UIUtils.defaultSkin(), "no-title");
    window.setMovable(false);
    window.pad(16f);

    Table content = new Table();
    if (!title.isBlank()) {
      DialogDesign.addTitleTable(content, title);
    }
    content.add(buildToolbar()).growX().padBottom(8f).row();
    content.add(area).size(areaWidth, areaHeight).row();

    window.add(content);
    window.pack();
    return window;
  }

  private Table buildToolbar() {
    Table toolbar = new Table();
    toolbar.left();

    TextButton reset = Scene2dElementFactory.createButton("Reset View", "blue-outline", 18);
    reset.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            area.resetView();
          }
        });

    TextButton fit = Scene2dElementFactory.createButton("Fit", "blue-outline", 18);
    fit.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            area.zoomToFit();
          }
        });

    toolbar.add(reset).padRight(8f);
    toolbar.add(fit).padRight(8f);
    toolbar.add().growX();

    TextButton close = Scene2dElementFactory.createButton("Close", "red-outline", 18);
    close.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
            requestClose();
          }
        });
    toolbar.add(close);
    return toolbar;
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
