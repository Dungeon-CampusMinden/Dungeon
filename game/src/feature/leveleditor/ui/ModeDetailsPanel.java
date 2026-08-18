package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.elements.RichLabel;
import feature.leveleditor.LevelEditorMode;
import feature.systems.LevelEditorSystem;
import feature.systems.LevelEditorSystem.Mode;
import java.util.Map;

/**
 * Panel at the left side of the screen, vertically centered, holding the controls and displays of
 * the currently selected {@link Mode}.
 *
 * <p>The panel is built from the following sections, in order:
 *
 * <ol>
 *   <li>a header showing {@link LevelEditorMode#getHeader()}
 *   <li>the mode specific content built by {@link LevelEditorMode#buildDetailsUI(Table)}
 *   <li>a separator and the controls of the mode, if it specifies any
 *   <li>a separator and {@link LevelEditorMode#additionalInformation()}, if it is not blank
 * </ol>
 */
public class ModeDetailsPanel extends Table {

  private static final Color BACKGROUND_COLOR = new Color(0.086f, 0.086f, 0.086f, 1f);
  private static final Color TEXT_COLOR = new Color(0.85f, 0.85f, 0.85f, 1f);
  private static final int HEADER_FONT_SIZE = 24;
  private static final int TEXT_FONT_SIZE = 16;

  private final Table modeContent = new Table();
  private final RichLabel header = new RichLabel("", HEADER_FONT_SIZE, Color.WHITE.cpy());
  private final RichLabel controlsLabel = new RichLabel("", TEXT_FONT_SIZE, TEXT_COLOR.cpy());
  private final RichLabel informationLabel = new RichLabel("", TEXT_FONT_SIZE, TEXT_COLOR.cpy());

  private LevelEditorMode mode = null;
  private String informationText = null;

  /** Creates an empty details panel. */
  public ModeDetailsPanel() {
    setBackground(UIUtils.defaultSkin().newDrawable("white", BACKGROUND_COLOR));
    pad(12f);
    top();
    header.setAlignment(com.badlogic.gdx.utils.Align.center);
    controlsLabel.setWrap(true);
    informationLabel.setWrap(true);
  }

  /**
   * Gets the mode this panel currently displays.
   *
   * @return the current mode, can be {@code null}.
   */
  public LevelEditorMode mode() {
    return mode;
  }

  /**
   * Sets the mode this panel displays and rebuilds the whole panel.
   *
   * @param mode the mode to display, can be {@code null} to clear the panel.
   */
  public void mode(LevelEditorMode mode) {
    this.mode = mode;
    buildModeContent();
    rebuild();
  }

  /** Clears and rebuilds the mode specific content of this panel. */
  private void buildModeContent() {
    modeContent.clearChildren();
    if (mode != null) {
      mode.buildDetailsUI(modeContent);
    }
  }

  /** Rebuilds the section layout of this panel from the current mode. */
  public void rebuild() {
    clearChildren();
    informationText = null;
    if (mode == null) return;

    header.setText(mode.getHeader());
    add(header).growX().padBottom(10f).row();

    add(modeContent).growX().row();

    Map<Integer, String> modeControls = mode.controls();
    if (modeControls != null && !modeControls.isEmpty()) {
      addSeparator();
      controlsLabel.setText(RichLabel.toRichText(formatControls(modeControls)));
      add(controlsLabel).growX().row();
    }

    String information = mode.additionalInformation();
    if (information != null && !information.isBlank()) {
      addSeparator();
      informationText = information;
      informationLabel.setText(RichLabel.toRichText(information));
      add(informationLabel).growX().row();
    }

    // Pushes all content to the top of the panel
    add().grow().row();
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (mode == null || !LevelEditorSystem.active()) return;
    mode.updateDetailsUI();
    updateInformation();
  }

  @Override
  protected void sizeChanged() {
    super.sizeChanged();
    float available = getWidth() - getPadLeft() - getPadRight();
    if (available <= 0) return;
    header.setMaxPrefWidth(available);
    controlsLabel.setMaxPrefWidth(available);
    informationLabel.setMaxPrefWidth(available);
  }

  /**
   * Refreshes the additional information section. Rebuilds the panel if the section has to appear
   * or disappear.
   */
  private void updateInformation() {
    String current = mode.additionalInformation();
    boolean hadSection = informationText != null;
    boolean hasSection = current != null && !current.isBlank();
    if (hadSection != hasSection) {
      rebuild();
      return;
    }
    if (hasSection && !current.equals(informationText)) {
      informationText = current;
      informationLabel.setText(RichLabel.toRichText(current));
    }
  }

  private void addSeparator() {
    add(Scene2dElementFactory.createHorizontalDivider()).growX().padTop(8f).padBottom(8f).row();
  }

  private static String formatControls(Map<Integer, String> modeControls) {
    StringBuilder text = new StringBuilder("Controls:");
    modeControls.forEach(
        (key, action) ->
            text.append("\n").append(LevelEditorMode.keyName(key)).append(" - ").append(action));
    return text.toString();
  }
}
