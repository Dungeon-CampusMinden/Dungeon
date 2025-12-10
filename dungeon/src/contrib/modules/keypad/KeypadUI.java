package contrib.modules.keypad;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.sound.SoundSpec;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** The UI for a keypad entity, allowing the player to input a code. */
public class KeypadUI extends Group {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(KeypadUI.class);

  private static final float BACKGROUND_SCALE = 1.5f;
  private static final float BACKGROUND_OFFSET_Y = 60;

  private final Entity keypad;

  private Image background;
  private final List<Cell<TextButton>> buttonCells = new ArrayList<>();
  private Label numberLabel;

  /**
   * Creates a new KeypadUI for the given keypad entity.
   *
   * @param keypad The keypad entity this UI is associated with.
   */
  public KeypadUI(Entity keypad) {
    this.keypad = keypad;
    createActors();
  }

  private void createActors() {
    this.setScale(0.75f);
    this.setOrigin(Align.center);
    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());

    background = new Image(getSkin(), "keypad-ui-off");
    background.setOrigin(Align.center);
    background.setScale(1.5f);
    background.setPosition(getX(Align.center), getY(Align.center), Align.center);
    this.addActor(background);

    Table parentTable = new Table();
    parentTable.setFillParent(true);
    this.addActor(parentTable);

    numberLabel = new Label("12345", getSkin(), "keypad");
    numberLabel.setFontScale(1.25f);
    parentTable.add(numberLabel).height(120).padBottom(60).row();

    Table tableButtons = new Table();
    parentTable.add(tableButtons);

    List<String> buttons =
        Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "Back", "0", "Submit");

    for (int i = 0; i < buttons.size(); i++) {
      String action = buttons.get(i);
      TextButton btn = new TextButton(action, getSkin(), "keypad");
      if (!action.equals("Back") && !action.equals("Submit")) {
        btn.getLabel().setFontScale(2f);
      } else {
        btn.getLabel().setFontScale(1.25f);
      }
      btn.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
              onButtonPress(action);
            }
          });
      Cell<TextButton> c = tableButtons.add(btn).height(100).width(100).pad(10);
      if (i % 3 == 2) {
        c.row();
      }
      buttonCells.add(c);
    }
  }

  /**
   * Builds a KeypadUI from the given DialogContext.
   *
   * @param context The dialog context containing the keypad entity.
   * @return A new KeypadUI instance.
   */
  public static Group build(DialogContext context) {
    return new KeypadUI(context.requireEntity(DialogContextKeys.ENTITY));
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    this.setScale(0.75f);
    this.setOrigin(Align.center);
    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());

    KeypadComponent kc = keypad.fetch(KeypadComponent.class).orElseThrow();

    background.setPosition(getX(Align.center), getY(Align.center), Align.center);
    background.setDrawable(getSkin(), kc.isUnlocked() ? "keypad-ui-on" : "keypad-ui-off");
    numberLabel.setText(kc.enteredString());

    super.draw(batch, parentAlpha);
  }

  private void onButtonPress(String action) {
    LOGGER.info("Clicked button: " + action);
    KeypadComponent kc = keypad.fetch(KeypadComponent.class).orElseThrow();

    int number = -1;
    try {
      number = Integer.parseInt(action);
      kc.addDigit(number);
    } catch (NumberFormatException ex) {
      switch (action) {
        case "Back" -> kc.backspace();
        case "Submit" -> onSubmit();
      }
    }

    if (!action.equals("Submit")) {
      float pitch = 1 + (number - 5) * 0.05f;
      Game.audio().playGlobal(SoundSpec.builder("retro_beep_01").pitch(pitch));
    }
  }

  private void onSubmit() {
    KeypadComponent kc = keypad.fetch(KeypadComponent.class).orElseThrow();
    if (kc.isUnlocked()) return;
    kc.checkUnlock();
    if (kc.isUnlocked()) {
      keypad.fetch(DrawComponent.class).orElseThrow().sendSignal("open");
      Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
    } else {
      Game.audio().playGlobal(SoundSpec.builder("retro_event_wrong"));
    }
  }

  private Skin getSkin() {
    return UIUtils.defaultSkin();
  }
}
