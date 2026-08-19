package feature.leveleditor.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import engine.utils.Scene2dElementFactory;
import feature.hud.UIUtils;
import feature.hud.elements.RichLabel;
import feature.leveleditor.LevelEditorMode;
import feature.systems.LevelEditorSystem;
import feature.systems.LevelEditorSystem.Mode;
import java.util.Map;

/**
 * Panel at the left side of the screen, holding the controls and displays of the currently selected
 * {@link Mode}.
 *
 * <p>The panel is built from the following sections, in order:
 *
 * <ol>
 *   <li>a header showing {@link LevelEditorMode#getHeader()}
 *   <li>the mode specific content built by {@link LevelEditorMode#buildDetailsUI(Table)}
 *   <li>a separator and the controls of the mode as a key/description grid, if it specifies any
 *   <li>a separator and {@link LevelEditorMode#additionalInformation()}, if it is not blank
 * </ol>
 */
public class ModeDetailsPanel extends Table {

  /** Font color used for all text rendered directly on the panel background. */
  public static final Color TEXT_COLOR = Color.BLACK.cpy();

  private static final int HEADER_FONT_SIZE = 24;
  private static final int TEXT_FONT_SIZE = 16;

  private final Table modeContent = new Table();
  private final Table controlsContent = new Table();
  private final RichLabel header = new RichLabel("", HEADER_FONT_SIZE, TEXT_COLOR, false);
  private final RichLabel informationLabel = new RichLabel("", TEXT_FONT_SIZE, TEXT_COLOR, false);

  private LevelEditorMode mode = null;
  private String informationText = null;

  /** Creates an empty details panel. */
  public ModeDetailsPanel() {
    setTouchable(Touchable.enabled);
    setBackground(UIUtils.defaultSkin().getDrawable("generic-area"));
    pad(12f);
    top();
    header.setAlignment(Align.center);
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
    buildControlsContent();
    rebuild();
  }

  /** Clears and rebuilds the mode specific content of this panel. */
  private void buildModeContent() {
    modeContent.clearChildren();
    if (mode != null) {
      mode.buildDetailsUI(modeContent);
    }
  }

  /** Clears and rebuilds the controls grid of this panel. */
  private void buildControlsContent() {
    controlsContent.clearChildren();
    if (mode == null) return;
    Map<Integer, String> modeControls = mode.controls();
    if (modeControls == null || modeControls.isEmpty()) return;

    controlsContent.add(text("Controls:")).colspan(2).left().padBottom(4f).row();
    modeControls.forEach(
        (key, action) -> {
          controlsContent.add(text(keyTag(key))).left().padRight(10f).padBottom(2f);
          controlsContent.add(text(action)).left().growX().padBottom(2f).row();
        });
  }

  /** Rebuilds the section layout of this panel from the current mode. */
  public void rebuild() {
    clearChildren();
    informationText = null;
    if (mode == null) return;

    header.setText(mode.getHeader());
    add(header).growX().padBottom(10f).row();

    add(modeContent).growX().row();

    if (controlsContent.hasChildren()) {
      addSeparator();
      add(controlsContent).growX().row();
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

  private static RichLabel text(String content) {
    return new RichLabel(RichLabel.toRichText(content), TEXT_FONT_SIZE, TEXT_COLOR, false);
  }

  /**
   * Builds the {@code [key]} markup that renders the input prompt graphic for the given key or
   * mouse button.
   *
   * @param key the key or mouse button code.
   * @return the rich text markup rendering the input prompt graphic.
   */
  private static String keyTag(int key) {
    if (key == Input.Buttons.LEFT || key == Input.Buttons.RIGHT) {
      return "[key code=" + key + " type=mouse]";
    }
    return "[key code=" + swapForLayout(key) + "]";
  }

  /**
   * Quick and dirty fix for the german keyboard layout where Y and Z are swapped.
   *
   * @param key the key code.
   * @return the key code to render an icon for.
   */
  private static int swapForLayout(int key) {
    if (key == Input.Keys.Y) return Input.Keys.Z;
    if (key == Input.Keys.Z) return Input.Keys.Y;
    return key;
  }
}
