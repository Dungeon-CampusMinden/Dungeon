package feature.interaction.keypad;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Entity;
import engine.Game;
import engine.language.Translation;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.logging.DungeonLogger;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.HeadlessDialogGroup;
import feature.tasks.TaskComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/** The UI for a text keypad entity, allowing the player to input a code. */
public class TextKeypadUI extends Group {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TextKeypadUI.class);

  private static final String ACTION_BACK = "Back";
  private static final String ACTION_SPACE = "Space";
  private static final String ACTION_SUBMIT = "Submit";
  private static final Translation trans = new Translation("dialog.keypad_dialog");

  private final Entity keypad;
  private final String dialogId;

  private Image background;
  private Label textLabel;

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
    background.setScale(1.9f);
    background.setPosition(getX(Align.center), getY(Align.center), Align.center);
    this.addActor(background);

    Table parentTable = new Table();
    parentTable.setFillParent(true);
    this.addActor(parentTable);

    textLabel = new Label("12345", getSkin(), "keypad");
    textLabel.setFontScale(1.25f);
    parentTable.add(textLabel).height(120).padBottom(100).row();

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
      if (!action.equals(ACTION_BACK)
          && !action.equals(ACTION_SUBMIT)
          && !action.equals(ACTION_SPACE)) {
        btn.getLabel().setFontScale(2f);
      } else {
        btn.getLabel().setFontScale(1f);
      }
      btn.addListener(
          new ClickListener() {
            @Override
            public void clicked(InputEvent e, float x, float y) {
              DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM)
                  .accept(new DialogResponseMessage.StringValue(action));
            }
          });
      Cell<TextButton> c = tableButtons.add(btn).height(75).width(75).pad(5);
      if (i % 5 == 4 || i == 25) {
        c.row();
      }
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

    // Before the first synchronized state arrives, the keypad is shown empty.
    Optional<KeypadComponent> kc = keypad.fetch(KeypadComponent.class);
    boolean unlocked = kc.map(KeypadComponent::isUnlocked).orElse(false);

    background.setPosition(getX(Align.center), getY(Align.center), Align.center);
    background.setDrawable(getSkin(), unlocked ? "keypad-ui-on" : "keypad-ui-off");
    textLabel.setText(kc.map(KeypadComponent::enteredText).orElse(""));

    super.draw(batch, parentAlpha);
  }

  /**
   * Handles a button pressed on the text keypad dialog. Runs on the server; actions other than a
   * single letter, space, back or submit are ignored.
   *
   * @param keypadEntity the keypad entity
   * @param task the task checking submitted answers
   * @param caller the player that pressed the button
   * @param action the pressed button
   */
  static void onButtonPress(
      Entity keypadEntity, TaskComponent<String> task, Entity caller, String action) {
    LOGGER.info("Clicked button: " + action);

    var keypadComp = keypadEntity.fetch(KeypadComponent.class).orElseThrow();

    if (action.matches("[A-Z]")) {
      keypadComp.add(action);
    } else if (action.equals(ACTION_SPACE)) {
      keypadComp.add(" ");
    } else if (action.equals(ACTION_BACK)) {
      keypadComp.backspace();
    } else if (action.equals(ACTION_SUBMIT)) {
      KeypadFactory.submit(keypadEntity, task, caller);
      return;
    } else {
      return;
    }

    Game.audio().playGlobal(SoundSpec.builder("retro_beep_01").targets(caller.id()));
  }

  private String displayLabelForAction(String action) {
    return switch (action) {
      case ACTION_BACK -> trans.text("back");
      case ACTION_SPACE -> trans.text("space");
      case ACTION_SUBMIT -> trans.text("submit");
      default -> action;
    };
  }

  private Skin getSkin() {
    return UIUtils.defaultSkin();
  }
}
