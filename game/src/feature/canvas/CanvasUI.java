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
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import engine.Game;
import engine.utils.BaseContainerUI;
import engine.utils.Scene2dElementFactory;
import engine.utils.logging.DungeonLogger;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogDesign;
import feature.hud.elements.RichLabel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(CanvasUI.class);

  /** Callback key fired when the player presses the close button. */
  public static final String EVENT_CLOSE = "canvasClose";

  private static final float VIEWPORT_MARGIN = 32f;
  private static final float TOP_MARGIN = 12f;
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
   * @param layout the visual canvas configuration
   * @param defaults the server provided default nodes; must not be null
   * @param dialogId the dialog id used for server events and closing
   * @param prototypes fresh local nodes carrying runtime callbacks
   */
  public CanvasUI(
      String canvasId,
      CanvasLayout layout,
      CanvasSnapshot defaults,
      String dialogId,
      List<CanvasNode> prototypes) {
    Objects.requireNonNull(canvasId, "canvasId");
    Objects.requireNonNull(layout, "layout");
    this.options = layout.options();
    this.defaults = Objects.requireNonNull(defaults, "defaults");
    this.dialogId = dialogId;

    setSize(Game.windowWidth(), Game.windowHeight());

    this.area = new CanvasArea(canvasId, layout.areaWidth(), layout.areaHeight(), options);
    this.area.dialogId(dialogId);

    this.loadedChanges = CanvasStore.load(canvasId);
    Map<String, CanvasNode> prototypesById = new LinkedHashMap<>();
    for (CanvasNode prototype : Objects.requireNonNull(prototypes, "prototypes")) {
      prototypesById.put(prototype.id(), prototype);
    }
    for (NodeState state : defaults.mergeWith(loadedChanges, options).nodes()) {
      CanvasNode prototype = prototypesById.get(state.id());
      if (prototype == null || !prototype.typeId().equals(state.typeId())) {
        if (state.origin() == NodeOrigin.DEFAULT) {
          LOGGER.warn(
              "No matching prototype for node '{}' ({}) on canvas '{}'; runtime behavior is unavailable",
              state.id(),
              state.typeId(),
              canvasId);
        }
        area.addNode(CanvasNodeType.create(state));
      } else {
        prototype.applyState(state);
        area.addNode(prototype);
      }
    }
    area.resetView();

    BaseContainerUI container = new BaseContainerUI(null, true, true);
    container.setFillParent(true);
    container.pad(VIEWPORT_MARGIN);
    container.padTop(TOP_MARGIN);
    container.setContent(
        buildWindow(layout.title(), layout.showResetViewButton(), layout.showFitButton()));
    addActor(container);
  }

  private Window buildWindow(String title, boolean showResetViewButton, boolean showFitButton) {
    Window window = new Window("", UIUtils.defaultSkin(), "no-title");
    window.setMovable(false);
    window.setBackground((Drawable) null);

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
            new RichLabel(
                title,
                DialogDesign.DIALOG_FONT_SPEC_TITLE
                    .withColor(Color.WHITE)
                    .withBorder(2f, Color.BLACK)))
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
