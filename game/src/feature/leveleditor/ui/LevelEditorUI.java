package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
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

    float detailsWidth = DETAILS_PANEL_WIDTH;
    detailsPanel.setWidth(detailsWidth);
    detailsPanel.validate();
    float detailsHeight = Math.min(detailsPanel.getPrefHeight(), stageHeight - 2f * SCREEN_PADDING);
    detailsPanel.setHeight(detailsHeight);
    detailsPanel.setPosition(SCREEN_PADDING, (stageHeight - detailsHeight) / 2f);
  }
}
