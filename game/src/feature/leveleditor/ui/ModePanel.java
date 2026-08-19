package feature.leveleditor.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import engine.components.DrawComponent;
import engine.utils.FontHelper;
import engine.utils.Scene2dElementFactory;
import feature.entities.deco.Deco;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogDesign;
import feature.systems.LevelEditorSystem;
import feature.systems.LevelEditorSystem.Mode;
import java.util.EnumMap;
import java.util.Map;

/**
 * Panel at the top of the screen that allows selecting the active {@link Mode} of the level editor.
 *
 * <p>Every mode is represented by a square button showing a letter or an image, with the number of
 * the mode (the key the user can press to select it) centered below the button.
 */
public class ModePanel extends Table {

  private static final String STYLE_SELECTED = "blue-outline";
  private static final String STYLE_UNSELECTED = "default";
  private static final float BUTTON_SIZE = 48f;

  private final Skin skin = UIUtils.defaultSkin();
  private final Map<Mode, Button> buttons = new EnumMap<>(Mode.class);
  private Mode selectedMode = null;

  /** Creates the mode selection panel with one button per {@link Mode}. */
  public ModePanel() {
    setTouchable(Touchable.enabled);
    setBackground(skin.getDrawable("generic-area"));
    pad(8f);
    defaults().pad(4f);

    Label.LabelStyle numberStyle =
        new Label.LabelStyle(FontHelper.getFont(DialogDesign.DIALOG_FONT_SPEC_NORMAL.withSize(18)), ModeDetailsPanel.TEXT_COLOR.cpy());

    for (Mode mode : Mode.values()) {
      Button button = createButton(mode);
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
            applyBackground(
                button, buttonMode == mode ? STYLE_SELECTED : STYLE_UNSELECTED));
  }

  private Button createButton(Mode mode) {
    ImageButton imageButton =
        switch (mode) {
          case Tiles ->
              Scene2dElementFactory.createImageButton(
                  "dungeon/default/floor/floor_1.png", STYLE_UNSELECTED);
          case Decos ->
              Scene2dElementFactory.createImageButton(
                  new DrawComponent(Deco.TreeMedium.path(), Deco.TreeMedium.config()).getSprite(),
                  STYLE_UNSELECTED);
          case Points ->
              Scene2dElementFactory.createImageButton(
                  "hud/kenney/flag_square.png", STYLE_UNSELECTED);
          case SaveLevel ->
              Scene2dElementFactory.createImageButton("hud/settings.png", STYLE_UNSELECTED);
          default -> null;
        };
    if (imageButton == null) {
      return new TextButton(String.valueOf(mode.letter()), skin, STYLE_UNSELECTED);
    }

    imageButton.getImageCell().size(BUTTON_SIZE * 0.68f);
    imageButton.getImage().setScaling(Scaling.fit);
    return imageButton;
  }

  private void applyBackground(Button button, String styleName) {
    TextButton.TextButtonStyle base = skin.get(styleName, TextButton.TextButtonStyle.class);
    if (button instanceof ImageButton imageButton) {
      ImageButton.ImageButtonStyle style = imageButton.getStyle();
      style.up = base.up;
      style.down = base.down;
      style.over = base.over;
      imageButton.setStyle(style);
    } else {
      button.setStyle(base);
    }
  }
}
