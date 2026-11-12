package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import feature.hud.elements.RichLabel;
import feature.leveleditor.LevelEditorMode;
import feature.systems.LevelEditorSystem;
import feature.systems.LevelEditorSystem.Mode;

/**
 * Panel at the left side of the screen, holding the controls and displays of the currently selected
 * {@link Mode}.
 *
 * <p>The panel is built from the following sections, in order:
 *
 * <ol>
 *   <li>a header showing {@link LevelEditorMode#getHeader()}
 *   <li>the mode specific content built by {@link LevelEditorMode#buildDetailsUI(Table)}
 *   <li>a separator and {@link LevelEditorMode#additionalInformation()}, if it is not blank
 * </ol>
 *
 * <p>The controls of the mode are shown in the {@link StatusBar}.
 */
public class ModeDetailsPanel extends Table {

  /** Font color used for all text rendered directly on the panel background. */
  public static final Color TEXT_COLOR = EditorTheme.TEXT.cpy();

  private static final int HEADER_FONT_SIZE = 17;
  private static final int TEXT_FONT_SIZE = 14;

  private final Table modeContent = new Table();
  private final Label header = EditorTheme.boldLabel("", HEADER_FONT_SIZE, EditorTheme.TEXT);
  private final RichLabel informationLabel =
      new RichLabel("", TEXT_FONT_SIZE, EditorTheme.TEXT_MUTED, false);

  private LevelEditorMode mode = null;
  private String informationText = null;
  private final boolean secondary;
  private static final float PADDING = 12f;

  /** Creates an empty details panel. */
  public ModeDetailsPanel() {
    this(false);
  }

  /**
   * Creates a details panel for either the primary or secondary mode content.
   *
   * @param secondary whether to use secondary mode hooks
   */
  public ModeDetailsPanel(boolean secondary) {
    this.secondary = secondary;
    setTouchable(Touchable.enabled);
    setBackground(EditorTheme.panel());
    pad(PADDING);
    top();
    header.setAlignment(Align.left);
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
   * Returns whether the panel takes all the height there is, see {@link
   * LevelEditorMode#fillsDetailsHeight()}.
   *
   * @return true to fill the available height
   */
  public boolean fillsHeight() {
    return mode != null && !secondary && mode.fillsDetailsHeight();
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
      if (secondary) mode.buildSecondaryDetailsUI(modeContent);
      else mode.buildDetailsUI(modeContent);
    }
  }

  /** Rebuilds the section layout of this panel from the current mode. */
  public void rebuild() {
    clearChildren();
    pad(PADDING);
    informationText = null;
    if (mode == null) return;

    if (secondary) {
      add(modeContent).grow().minWidth(0f).minHeight(0f).row();
      return;
    }

    String modeHeader = mode.getHeader();
    if (modeHeader != null && !modeHeader.isBlank()) {
      header.setText(modeHeader);
      add(header).growX().padBottom(10f).row();
    }

    if (mode.fillsDetailsHeight()) {
      add(modeContent).grow().minWidth(0f).minHeight(0f).row();
    } else {
      add(modeContent).growX().fillX().minWidth(0f).row();
    }

    String information = mode.additionalInformation();
    if (information != null && !information.isBlank()) {
      addSeparator();
      informationText = information;
      informationLabel.setText(RichLabel.toRichText(information));
      add(informationLabel).growX().row();
    }

    // Pushes all content to the top of the panel
    if (!mode.fillsDetailsHeight()) add().grow().row();
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (mode == null || !LevelEditorSystem.active()) return;
    if (secondary) mode.updateSecondaryDetailsUI();
    else mode.updateDetailsUI();
    if (!secondary) updateInformation();
  }

  @Override
  protected void sizeChanged() {
    super.sizeChanged();
    float available = getWidth() - getPadLeft() - getPadRight();
    if (available <= 0) return;
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
    add(EditorTheme.divider()).growX().height(1f).padTop(10f).padBottom(8f).row();
  }
}
