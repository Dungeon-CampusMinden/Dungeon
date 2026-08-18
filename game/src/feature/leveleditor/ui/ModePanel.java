package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.utils.FontHelper;
import feature.hud.UIUtils;
import feature.systems.LevelEditorSystem;
import feature.systems.LevelEditorSystem.Mode;
import java.util.EnumMap;
import java.util.Map;

/**
 * Panel at the top of the screen that allows selecting the active {@link Mode} of the level editor.
 *
 * <p>Every mode is represented by a square button showing a single letter, with the number of the
 * mode (the key the user can press to select it) centered below the button.
 */
public class ModePanel extends Table {

  private static final Color BACKGROUND_COLOR = new Color(0.086f, 0.086f, 0.086f, 1f);
  private static final String STYLE_SELECTED = "blue-outline";
  private static final String STYLE_UNSELECTED = "default";
  private static final float BUTTON_SIZE = 48f;

  private final Skin skin = UIUtils.defaultSkin();
  private final Map<Mode, TextButton> buttons = new EnumMap<>(Mode.class);
  private Mode selectedMode = null;

  /** Creates the mode selection panel with one button per {@link Mode}. */
  public ModePanel() {
    setBackground(skin.newDrawable("white", BACKGROUND_COLOR));
    pad(8f);
    defaults().pad(4f);

    Label.LabelStyle numberStyle =
        new Label.LabelStyle(FontHelper.getDefaultFont(18), Color.WHITE.cpy());

    for (Mode mode : Mode.values()) {
      TextButton button = new TextButton(String.valueOf(mode.letter()), skin, STYLE_UNSELECTED);
      button.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              LevelEditorSystem.currentMode(mode);
            }
          });
      buttons.put(mode, button);

      Label number = new Label(String.valueOf(mode.ordinal() + 1), numberStyle);
      number.setAlignment(Align.center);

      Table entry = new Table();
      entry.add(button).size(BUTTON_SIZE).row();
      entry.add(number).padTop(2f).fillX();
      add(entry).top();
    }

    selected(LevelEditorSystem.currentMode());
  }

  /**
   * Highlights the button of the given mode and resets all other buttons.
   *
   * @param mode the currently selected mode.
   */
  public void selected(Mode mode) {
    if (mode == selectedMode) return;
    selectedMode = mode;
    buttons.forEach(
        (buttonMode, button) ->
            button.setStyle(
                skin.get(
                    buttonMode == mode ? STYLE_SELECTED : STYLE_UNSELECTED,
                    TextButton.TextButtonStyle.class)));
  }
}
