package feature.leveleditor.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import engine.components.DrawComponent;
import engine.utils.Cursors;
import feature.entities.deco.Deco;
import feature.systems.LevelEditorSystem;
import feature.systems.LevelEditorSystem.Mode;
import java.util.EnumMap;
import java.util.Map;

/**
 * Bar at the top of the screen that selects the active {@link Mode} of the level editor.
 *
 * <p>Every mode is a tab with an image, its name and the number key that selects it.
 */
public class ModePanel extends Table {

  private static final float ICON_SIZE = 20f;
  private static final float TAB_HEIGHT = 34f;

  private final Map<Mode, Button> buttons = new EnumMap<>(Mode.class);
  private final Map<Mode, Label> names = new EnumMap<>(Mode.class);
  private Mode selectedMode = null;

  /** Creates the mode selection bar with one tab per {@link Mode}. */
  public ModePanel() {
    setTouchable(Touchable.enabled);
    setBackground(EditorTheme.panel());
    pad(5f);
    defaults().height(TAB_HEIGHT).padLeft(1f).padRight(1f);

    for (Mode mode : Mode.values()) {
      Button button = new Button(new Button.ButtonStyle());
      button.setUserObject(Cursors.INTERACT);
      button.pad(0f, 10f, 0f, 10f);
      Image image = new Image(icon(mode));
      image.setScaling(Scaling.fit);
      button.add(image).size(ICON_SIZE).padRight(7f);
      Label name = EditorTheme.boldLabel(name(mode), 14, EditorTheme.TEXT);
      names.put(mode, name);
      button.add(name);
      Label key =
          EditorTheme.label(String.valueOf(mode.ordinal() + 1), 12, EditorTheme.TEXT_DISABLED);
      button.add(key).padLeft(7f).padTop(2f);
      button.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              LevelEditorSystem.currentMode(mode);
            }
          });
      EditorTheme.tooltip(button, tooltip(mode));
      buttons.put(mode, button);
      add(button);
    }

    selected(LevelEditorSystem.currentMode());
  }

  /**
   * Highlights the tab of the given mode and resets all other tabs.
   *
   * @param mode the currently selected mode.
   */
  public void selected(Mode mode) {
    if (mode == selectedMode) return;
    selectedMode = mode;
    buttons.forEach(
        (buttonMode, button) -> {
          boolean selected = buttonMode == mode;
          Button.ButtonStyle style = new Button.ButtonStyle();
          Drawable up =
              selected
                  ? EditorTheme.rounded(EditorTheme.BUTTON_OVER, null, 4)
                  : EditorTheme.rounded(new Color(0f, 0f, 0f, 0f), null, 4);
          style.up = up;
          style.over = selected ? up : EditorTheme.rounded(EditorTheme.ROW_OVER, null, 4);
          style.down = up;
          button.setStyle(style);
          names.get(buttonMode).setColor(selected ? EditorTheme.SELECTION : EditorTheme.TEXT);
        });
  }

  private static String name(Mode mode) {
    return switch (mode) {
      case Tiles -> "Tiles";
      case Decos -> "Decos";
      case Points -> "Points";
      case Prefabs -> "Prefabs";
      case StartTiles -> "Start";
      case Settings -> "Settings";
    };
  }

  private static String tooltip(Mode mode) {
    return switch (mode) {
      case Tiles -> "Paint floor, walls and other tiles, and set the level design.";
      case Decos -> "Place decorations.";
      case Points -> "Place named points used by the room's code.";
      case Prefabs -> "Place and connect prefabs: switches, doors, dialogs, puzzles and logic.";
      case StartTiles -> "Choose where players start.";
      case Settings -> "Resize and shift the level, choose where it is saved.";
    };
  }

  private static Drawable icon(Mode mode) {
    return switch (mode) {
      case Tiles -> EditorTheme.asset("dungeon/default/floor/floor_1.png");
      case Decos ->
          new TextureRegionDrawable(
              new TextureRegion(
                  new DrawComponent(Deco.TreeMedium.path(), Deco.TreeMedium.config()).getSprite()));
      case Points -> EditorTheme.asset("hud/kenney/flag_square.png");
      case StartTiles -> EditorTheme.asset("items/rpg/item_compass.png");
      case Settings -> EditorTheme.asset("hud/settings.png");
      case Prefabs -> EditorTheme.asset("items/rpg/item_book_blue_square.png");
    };
  }
}
