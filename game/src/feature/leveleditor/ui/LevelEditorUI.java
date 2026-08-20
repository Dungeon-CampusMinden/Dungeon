package feature.leveleditor.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import engine.Game;
import feature.systems.LevelEditorSystem;

/**
 * Root Scene2D group of the level editor.
 *
 * <p>The group spans the whole stage and hosts all level editor panels:
 *
 * <ul>
 *   <li>{@link ModePanel} at the top of the screen, used to select the active editor mode.
 *   <li>{@link ModeDetailsPanel} at the left side of the screen, holding the controls of the
 *       currently selected mode.
 * </ul>
 *
 * <p>The group removes itself from the stage as soon as the {@link LevelEditorSystem} is no longer
 * registered in the {@link Game}.
 */
public class LevelEditorUI extends Group {

  private static final float SCREEN_PADDING = 12f;
  private static final float DETAILS_PANEL_WIDTH = 320f;
  private static final float DETAILS_PANEL_TOP_GAP = 200f;

  private final ModePanel modePanel = new ModePanel();
  private final ModeDetailsPanel detailsPanel = new ModeDetailsPanel();

  /** Creates a new level editor UI group. */
  public LevelEditorUI() {
    setTouchable(Touchable.childrenOnly);
    addActor(modePanel);
    addActor(detailsPanel);
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

  @Override
  public void act(float delta) {
    if (!Game.systems().containsKey(LevelEditorSystem.class)) {
      remove();
      LevelEditorSystem.uiDetached();
      return;
    }
    layoutPanels();
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
        stage.screenToStageCoordinates(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
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

  /** Keeps the group and its panels aligned with the current stage size. */
  private void layoutPanels() {
    Stage stage = getStage();
    if (stage == null) return;

    float stageWidth = stage.getWidth();
    float stageHeight = stage.getHeight();
    setPosition(0, 0);
    setSize(stageWidth, stageHeight);

    modePanel.pack();
    modePanel.setPosition(
        (stageWidth - modePanel.getWidth()) / 2f,
        stageHeight - modePanel.getHeight() - SCREEN_PADDING);

    detailsPanel.setWidth(DETAILS_PANEL_WIDTH);
    detailsPanel.validate();
    float detailsHeight =
        Math.min(
            detailsPanel.getPrefHeight(), stageHeight - DETAILS_PANEL_TOP_GAP - SCREEN_PADDING);
    detailsPanel.setHeight(detailsHeight);
    detailsPanel.setPosition(SCREEN_PADDING, stageHeight - DETAILS_PANEL_TOP_GAP - detailsHeight);
  }
}
