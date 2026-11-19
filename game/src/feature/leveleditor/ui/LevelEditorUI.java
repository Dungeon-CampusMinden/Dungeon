package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.input.GameInput;
import engine.input.InputContext;
import engine.input.UiActions;
import feature.leveleditor.EditorActions;
import feature.leveleditor.LevelEditorMode;
import feature.systems.LevelEditorSystem;
import java.util.ArrayList;
import java.util.List;

/**
 * Root Scene2D group of the level editor.
 *
 * <p>The group spans the whole stage and hosts all level editor panels:
 *
 * <ul>
 *   <li>{@link ModePanel} at the top of the screen, used to select the active editor mode, with the
 *       view options of the active mode next to it, see {@link LevelEditorMode#viewOptionsUI()}.
 *   <li>{@link ModeDetailsPanel} at the left side of the screen, holding the controls of the
 *       currently selected mode.
 *   <li>an optional right-side {@link ModeDetailsPanel}, holding secondary details for the active
 *       mode.
 *   <li>an optional toolbar of the active mode, which replaces the {@link ModePanel} while it is
 *       shown, see {@link LevelEditorMode#toolbarUI()}.
 *   <li>a prompt below the top bar while the mode waits for a click, see {@link
 *       LevelEditorMode#prompt()}.
 *   <li>the {@link StatusBar} along the bottom of the screen.
 *   <li>the {@link ProblemsPanel} above the status bar, while it is opened from there. A click
 *       anywhere else closes it.
 * </ul>
 *
 * <p>The group removes itself from the stage as soon as the {@link LevelEditorSystem} is no longer
 * registered in the {@link Game}.
 */
public class LevelEditorUI extends Group {

  private static final float SCREEN_PADDING = 8f;
  private static final float DETAILS_PANEL_WIDTH = 300f;
  private static final float SECONDARY_PANEL_WIDTH = 340f;
  private static final float PANEL_GAP = 6f;

  private final ModePanel modePanel = new ModePanel();
  private final ModeDetailsPanel detailsPanel = new ModeDetailsPanel();
  private final ModeDetailsPanel secondaryDetailsPanel = new ModeDetailsPanel(true);
  private final StatusBar statusBar = new StatusBar(this::toggleProblems, this::problemsShown);
  private final ProblemsPanel problemsPanel = new ProblemsPanel(this::toggleProblems);
  // Closes the problems panel on a click outside of it. The entry in the status bar toggles it.
  private final InputListener problemsOutsideClick =
      new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
          Actor target = event.getTarget();
          if (target == null
              || !(target.isDescendantOf(problemsPanel) || statusBar.togglesProblems(target))) {
            showProblems(false);
          }
          return false;
        }
      };
  private final Table prompt = new Table();
  private final Label promptLabel = EditorTheme.boldLabel("", 15, EditorTheme.ON_SELECTION);
  private Table toolbar;
  private Table viewOptions;
  private Table modal;
  private Runnable modalOnClose;
  private InputContext modalInput;
  // The bars at the top of the screen, as laid out in the last frame.
  private final List<Actor> topRow = new ArrayList<>();

  /** Creates a new level editor UI group. */
  public LevelEditorUI() {
    setTouchable(Touchable.childrenOnly);
    // Panels are operable UI: a press on their background must not reach the level below.
    addListener(
        new InputListener() {
          @Override
          public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return true;
          }
        });
    addActor(modePanel);
    addActor(detailsPanel);
    addActor(secondaryDetailsPanel);
    addActor(statusBar);
    prompt.setBackground(
        EditorTheme.padded(EditorTheme.rounded(EditorTheme.SELECTION, null, 6), 14f, 6f));
    prompt.setTouchable(Touchable.disabled);
    promptLabel.setAlignment(Align.center);
    prompt.add(promptLabel);
    prompt.setVisible(false);
    addActor(prompt);
    problemsPanel.setVisible(false);
    addActor(problemsPanel);
  }

  /** Opens the list of the problems of the level, or closes it if it is open. */
  public void toggleProblems() {
    showProblems(!problemsPanel.isVisible());
  }

  private void showProblems(boolean shown) {
    EditorTheme.hideTooltips();
    problemsPanel.setVisible(shown);
    Stage stage = getStage();
    if (stage == null) return;
    stage.removeCaptureListener(problemsOutsideClick);
    if (!shown) return;
    problemsPanel.toFront();
    stage.addCaptureListener(problemsOutsideClick);
  }

  /**
   * Returns whether the list of the problems of the level is open.
   *
   * @return true while the problems panel is shown
   */
  public boolean problemsShown() {
    return problemsPanel.isVisible();
  }

  /**
   * Gets the mode selection panel at the top of the screen.
   *
   * @return the mode panel.
   */
  public ModePanel modePanel() {
    return modePanel;
  }

  /**
   * Gets the details panel at the left side of the screen.
   *
   * @return the details panel.
   */
  public ModeDetailsPanel detailsPanel() {
    return detailsPanel;
  }

  /**
   * Gets the optional right-side details panel.
   *
   * @return the secondary details panel
   */
  public ModeDetailsPanel secondaryDetailsPanel() {
    return secondaryDetailsPanel;
  }

  @Override
  public void act(float delta) {
    if (!Game.systems().containsKey(LevelEditorSystem.class)) {
      showProblems(false);
      remove();
      LevelEditorSystem.uiDetached();
      return;
    }
    updateToolbar();
    updateViewOptions();
    updatePrompt();
    layoutPanels();
    secondaryDetailsPanel.setVisible(
        LevelEditorSystem.active()
            && secondaryDetailsPanel.mode() != null
            && secondaryDetailsPanel.mode().hasSecondaryDetailsUI());
    super.act(delta);
  }

  /**
   * Checks whether the mouse cursor currently hovers one of the level editor panels.
   *
   * @return true if the cursor is over a panel of this UI, false otherwise.
   */
  public boolean isCursorOverUI() {
    Stage stage = getStage();
    if (stage == null || !isVisible()) return false;
    Vector2 stagePosition =
        stage.screenToStageCoordinates(
            new Vector2(GameInput.pointer().x(), GameInput.pointer().y()));
    Actor hit = stage.hit(stagePosition.x, stagePosition.y, true);
    while (hit != null) {
      if (hit == this) return true;
      if (hit instanceof SelectBox.SelectBoxScrollPane<?> popup
          && popup.getSelectBox().isDescendantOf(this)) {
        return true;
      }
      hit = hit.getParent();
    }
    return false;
  }

  /**
   * Moves an area drawn over the level, e.g. a list in the level overlay, as little as needed to
   * keep it in view: on the screen, above the status bar and clear of the panels. It moves sideways
   * out of a side panel and down out of the bars at the top. An area too large for the space keeps
   * its top left corner in view.
   *
   * @param box area in stage coordinates, moved in place
   * @param margin distance kept to the panels and the edges of the screen
   */
  public void keepInView(Rectangle box, float margin) {
    box.x = Math.max(margin, Math.min(box.x, getWidth() - margin - box.width));
    box.y =
        Math.min(
            getHeight() - margin - box.height, Math.max(box.y, statusBar.getHeight() + margin));
    for (Actor bar : topRow) {
      if (near(bar, box, margin)) box.y = bar.getY() - margin - box.height;
    }
    if (problemsPanel.isVisible() && near(problemsPanel, box, margin)) {
      box.x = problemsPanel.getX() + problemsPanel.getWidth() + margin;
    }
    if (secondaryDetailsPanel.isVisible() && near(secondaryDetailsPanel, box, margin)) {
      box.x = secondaryDetailsPanel.getX() - margin - box.width;
    }
    if (detailsPanel.isVisible() && near(detailsPanel, box, margin)) {
      box.x = detailsPanel.getX() + detailsPanel.getWidth() + margin;
    }
  }

  // Whether an actor covers an area or comes closer to it than the margin.
  private static boolean near(Actor actor, Rectangle box, float margin) {
    return actor.getWidth() > 0f
        && actor.getHeight() > 0f
        && actor.getX() < box.x + box.width + margin
        && actor.getX() + actor.getWidth() + margin > box.x
        && actor.getY() < box.y + box.height + margin
        && actor.getY() + actor.getHeight() + margin > box.y;
  }

  /** Shows the toolbar the active mode currently asks for. */
  private void updateToolbar() {
    LevelEditorMode mode = detailsPanel.mode();
    Table wanted = LevelEditorSystem.active() && mode != null ? mode.toolbarUI() : null;
    if (wanted == toolbar) return;
    if (toolbar != null) toolbar.remove();
    toolbar = wanted;
    if (toolbar != null) addActor(toolbar);
    modePanel.setVisible(toolbar == null);
  }

  /** Shows the view options of the active mode next to the mode panel. */
  private void updateViewOptions() {
    LevelEditorMode mode = detailsPanel.mode();
    Table wanted =
        LevelEditorSystem.active() && mode != null && toolbar == null ? mode.viewOptionsUI() : null;
    if (wanted == viewOptions) return;
    if (viewOptions != null) viewOptions.remove();
    viewOptions = wanted;
    if (viewOptions != null) addActor(viewOptions);
  }

  private void updatePrompt() {
    LevelEditorMode mode = detailsPanel.mode();
    String text = mode == null ? null : mode.prompt();
    prompt.setVisible(text != null && !text.isBlank());
    if (prompt.isVisible() && !text.contentEquals(promptLabel.getText())) {
      promptLabel.setText(text);
      prompt.pack();
    }
  }

  /**
   * Shows a panel centered above the editor, on a dimmed backdrop that blocks the level and the
   * other panels. Escape or a click on the backdrop next to the panel closes it.
   *
   * <p>While it is shown, its input context lies above the editor's and the cursor counts as being
   * over the editor UI, so the level does not react to keys, clicks or the mouse wheel.
   *
   * @param panel panel to show
   * @param onClose called once when the panel is closed, in any way; may be {@code null}
   */
  public void showModal(Actor panel, Runnable onClose) {
    closeModal();
    EditorTheme.hideTooltips();
    Table backdrop = new Table();
    backdrop.setTouchable(Touchable.enabled);
    backdrop.setBackground(EditorTheme.flat(new Color(0f, 0f, 0f, 0.45f)));
    backdrop.add(panel).center();
    backdrop.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (event.getTarget() == backdrop) closeModal();
          }
        });
    modal = backdrop;
    modalOnClose = onClose;
    modalInput =
        new InputContext(EditorActions.EDITOR_POPUP).onPress(UiActions.CLOSE, this::closeModal);
    GameInput.activate(modalInput);
    addActor(backdrop);
    backdrop.setBounds(0f, 0f, getWidth(), getHeight());
    backdrop.validate();
  }

  /** Closes the panel shown by {@link #showModal}, if there is one. */
  public void closeModal() {
    if (modal == null) return;
    Table closed = modal;
    Runnable onClose = modalOnClose;
    modal = null;
    modalOnClose = null;
    GameInput.deactivate(modalInput);
    modalInput = null;
    Stage stage = closed.getStage();
    if (stage != null && stage.getKeyboardFocus() != null) {
      if (stage.getKeyboardFocus().isDescendantOf(closed)) stage.setKeyboardFocus(null);
    }
    closed.remove();
    if (onClose != null) onClose.run();
  }

  /** Keeps the group and its panels aligned with the current stage size. */
  private void layoutPanels() {
    Stage stage = getStage();
    if (stage == null) return;

    float stageWidth = stage.getWidth();
    float stageHeight = stage.getHeight();
    setPosition(0, 0);
    setSize(stageWidth, stageHeight);

    statusBar.setBounds(0f, 0f, stageWidth, StatusBar.HEIGHT);
    float bottom = StatusBar.HEIGHT + SCREEN_PADDING;

    topRow.clear();
    modePanel.pack();
    float rowWidth = modePanel.getWidth();
    if (viewOptions != null) {
      viewOptions.pack();
      rowWidth += PANEL_GAP + viewOptions.getWidth();
    }
    // The mode panel and the view options are centered together.
    modePanel.setPosition(
        Math.round(Math.max(SCREEN_PADDING, (stageWidth - rowWidth) / 2f)),
        stageHeight - modePanel.getHeight() - SCREEN_PADDING);
    if (modePanel.isVisible()) topRow.add(modePanel);
    if (viewOptions != null) {
      viewOptions.setPosition(
          modePanel.getX() + modePanel.getWidth() + PANEL_GAP,
          modePanel.getY() + modePanel.getHeight() - viewOptions.getHeight());
      topRow.add(viewOptions);
    }
    if (toolbar != null) {
      toolbar.pack();
      toolbar.setPosition(
          Math.round((stageWidth - toolbar.getWidth()) / 2f),
          stageHeight - toolbar.getHeight() - SCREEN_PADDING);
      topRow.add(toolbar);
    }

    float topRowBottom = stageHeight;
    for (Actor actor : topRow) topRowBottom = Math.min(topRowBottom, actor.getY());
    if (prompt.isVisible()) {
      prompt.setPosition(
          Math.round((stageWidth - prompt.getWidth()) / 2f),
          topRowBottom - prompt.getHeight() - PANEL_GAP);
    }

    if (problemsPanel.isVisible()) {
      problemsPanel.setWidth(ProblemsPanel.WIDTH);
      problemsPanel.validate();
      float maxHeight = Math.max(120f, (stageHeight - bottom) * 0.5f);
      float height = Math.min(problemsPanel.getPrefHeight(), maxHeight);
      problemsPanel.setBounds(SCREEN_PADDING, bottom, ProblemsPanel.WIDTH, height);
    }

    if (modal != null) {
      modal.setBounds(0f, 0f, stageWidth, stageHeight);
      modal.toFront();
    }

    layoutSidePanel(detailsPanel, SCREEN_PADDING, DETAILS_PANEL_WIDTH, topRow, stageHeight, bottom);
    layoutSidePanel(
        secondaryDetailsPanel,
        stageWidth - SCREEN_PADDING - SECONDARY_PANEL_WIDTH,
        SECONDARY_PANEL_WIDTH,
        topRow,
        stageHeight,
        bottom);
  }

  // Side panels start below the top row only where it reaches over them.
  private static void layoutSidePanel(
      ModeDetailsPanel panel,
      float x,
      float width,
      List<Actor> topRow,
      float stageHeight,
      float bottom) {
    float top = stageHeight - SCREEN_PADDING;
    for (Actor actor : topRow) {
      if (actor.getX() < x + width && actor.getX() + actor.getWidth() > x) {
        top = Math.min(top, actor.getY() - PANEL_GAP);
      }
    }
    panel.setWidth(width);
    panel.validate();
    float available = Math.max(0f, top - bottom);
    boolean fill = panel.fillsHeight();
    float height = fill ? available : Math.min(panel.getPrefHeight(), available);
    panel.setBounds(x, top - height, width, height);
  }
}
