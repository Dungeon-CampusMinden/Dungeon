package feature.interaction.keypad;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.language.Translation;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.logging.DungeonLogger;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** The UI for a text keypad entity, allowing the player to input a code. */
public class TextKeypadUI extends Group {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TextKeypadUI.class);

  private static final float BACKGROUND_SCALE = 1.5f;
  private static final float BACKGROUND_OFFSET_Y = 60;
  private static final String ACTION_BACK = "Back";
  private static final String ACTION_SPACE = "Space";
  private static final String ACTION_SUBMIT = "Submit";
  private static final Translation trans = new Translation("dialog.keypad_dialog");

  private final Entity keypad;
  private final String dialogId;

  private Image background;
  private final List<Cell<TextButton>> buttonCells = new ArrayList<>();
  private Label numberLabel;

  /**
   * Creates a new TextKeypadUI for the given keypad entity.
   *
   * @param keypad The keypad entity this UI is associated with.
   * @param dialogId The dialog ID for callback resolution.
   */
  public TextKeypadUI(Entity keypad, String dialogId) {
    this.keypad = keypad;
    this.dialogId = dialogId;
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

    List<String> actions =
        Arrays.asList(
            "A",
            "B",
            "C",
            "D",
            "E",
            "F",
            "G",
            "H",
            "I",
            "J",
            "K",
            "L",
            "M",
            "N",
            "O",
            "P",
            "Q",
            "R",
            "S",
            "T",
            "U",
            "V",
            "W",
            "X",
            "Y",
            "Z",
            ACTION_BACK,
            ACTION_SPACE,
            ACTION_SUBMIT);

    for (int i = 0; i < actions.size(); i++) {
      String action = actions.get(i);
      String label = displayLabelForAction(action);
      TextButton btn = new TextButton(label, getSkin(), "keypad");
      if (!action.equals(ACTION_BACK) && !action.equals(ACTION_SUBMIT) && !action.equals(ACTION_SPACE)) {
        btn.getLabel().setFontScale(2f);
      } else {
        btn.getLabel().setFontScale(1.25f);
      }
      btn.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
              DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM)
                  .accept(new DialogResponseMessage.StringValue(action));
            }
          });
      Cell<TextButton> c = tableButtons.add(btn).height(100).width(100).pad(10);
      if (i == 9 || i == 18 || i == 25) {
        c.row();
      }
      buttonCells.add(c);
    }
  }

  /**
   * Builds a TextKeypadUI from the given DialogContext.
   *
   * @param context The dialog context containing the text keypad entity.
   * @return A new TextKeypadUI instance.
   */
  public static Group build(DialogContext context) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new TextKeypadUI(context.requireEntity(DialogContextKeys.ENTITY), context.dialogId());
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    this.setScale(0.75f);
    this.setOrigin(Align.center);
    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());

    TextKeyPadComponent kc = keypad.fetch(TextKeyPadComponent.class).orElseThrow();

    background.setPosition(getX(Align.center), getY(Align.center), Align.center);
    background.setDrawable(getSkin(), kc.isUnlocked() ? "keypad-ui-on" : "keypad-ui-off");
    numberLabel.setText(kc.enteredText());

    super.draw(batch, parentAlpha);
  }

  static void onButtonPress(Entity keypadEntity, Entity caller, String action) {
    LOGGER.info("Clicked button: " + action);

    var drawComp = keypadEntity.fetch(DrawComponent.class).orElseThrow();
    var keypadComp = keypadEntity.fetch(TextKeyPadComponent.class).orElseThrow();

    switch (action) {
      case ACTION_BACK -> keypadComp.backspace();
      case ACTION_SPACE -> keypadComp.addCharacter(" ");
      case ACTION_SUBMIT -> onSubmit(keypadComp, drawComp, caller);
      default -> keypadComp.addCharacter(action);
    }

    int number = 5;
    if (!action.equals(ACTION_SUBMIT)) {
      float pitch = 1 + (number - 5) * 0.05f;
      Game.audio().playGlobal(SoundSpec.builder("retro_beep_01").pitch(pitch).targets(caller.id()));
    }
  }

  private String displayLabelForAction(String action) {
    return switch (action) {
      case ACTION_BACK -> trans.text("back");
      case ACTION_SPACE -> trans.text("space");
      case ACTION_SUBMIT -> trans.text("submit");
      default -> action;
    };
  }

  private static void onSubmit(
      TextKeyPadComponent keypadComp, DrawComponent drawComp, Entity caller) {
    if (keypadComp.isUnlocked()) return;
    keypadComp.checkUnlock(caller);
    if (keypadComp.isUnlocked()) {
      drawComp.sendSignal("open");
      Game.audio().playGlobal(SoundSpec.builder("retro_event_correct"));
    } else {
      Game.audio().playGlobal(SoundSpec.builder("retro_event_wrong"));
    }
  }

  private Skin getSkin() {
    return UIUtils.defaultSkin();
  }
}
