package feature.interaction.keypad;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Entity;
import engine.Game;
import engine.language.Translation;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.List;
import java.util.Optional;

/**
 * The dialog of a numeric or text keypad. Each button sends its action to the server as an {@link
 * DialogContextKeys#ON_CONFIRM} response; the shown state comes from the {@link KeypadComponent} of
 * the keypad entity.
 */
public class KeypadUI extends Group {

  static final String ACTION_BACK = "Back";
  static final String ACTION_SPACE = "Space";
  static final String ACTION_SUBMIT = "Submit";

  private static final Translation TRANSLATION = new Translation("dialog.keypad_dialog");

  /**
   * Button rows and sizes of one keypad kind.
   *
   * @param rows button actions, row by row
   * @param backgroundScale scale of the keypad background
   * @param labelPadBottom space between the entered text and the buttons
   * @param buttonSize width and height of a button
   * @param buttonPad space around a button
   * @param actionFontScale font scale of the back, space and submit buttons
   */
  public record Layout(
      List<List<String>> rows,
      float backgroundScale,
      float labelPadBottom,
      float buttonSize,
      float buttonPad,
      float actionFontScale) {

    /** Digits 0-9 in a phone layout. */
    public static final Layout NUMERIC =
        new Layout(
            List.of(
                List.of("1", "2", "3"),
                List.of("4", "5", "6"),
                List.of("7", "8", "9"),
                List.of(ACTION_BACK, "0", ACTION_SUBMIT)),
            1.5f,
            60,
            100,
            10,
            1.25f);

    /** Letters A-Z and space. */
    public static final Layout TEXT =
        new Layout(
            List.of(
                List.of("A", "B", "C", "D", "E"),
                List.of("F", "G", "H", "I", "J"),
                List.of("K", "L", "M", "N", "O"),
                List.of("P", "Q", "R", "S", "T"),
                List.of("U", "V", "W", "X", "Y"),
                List.of("Z"),
                List.of(ACTION_BACK, ACTION_SPACE, ACTION_SUBMIT)),
            1.9f,
            100,
            75,
            5,
            1f);

    /**
     * Checks whether a button action belongs to this layout.
     *
     * @param action button action
     * @return true if one of the buttons has this action
     */
    public boolean hasAction(String action) {
      return rows.stream().anyMatch(row -> row.contains(action));
    }
  }

  private final Entity keypad;
  private final String dialogId;
  private final Layout layout;

  private Image background;
  private Label label;

  private KeypadUI(Entity keypad, String dialogId, Layout layout) {
    this.keypad = keypad;
    this.dialogId = dialogId;
    this.layout = layout;
    createActors();
  }

  /**
   * Builds a keypad dialog from the given DialogContext.
   *
   * @param context the dialog context containing the keypad entity
   * @param layout the buttons of the keypad
   * @return the keypad dialog, or a placeholder on a headless server
   */
  public static Group build(DialogContext context, Layout layout) {
    if (Game.isHeadless()) return new HeadlessDialogGroup();
    return new KeypadUI(
        context.requireEntity(DialogContextKeys.ENTITY), context.dialogId(), layout);
  }

  private void createActors() {
    this.setScale(0.75f);
    this.setOrigin(Align.center);
    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());

    background = new Image(UIUtils.defaultSkin(), "keypad-ui-off");
    background.setOrigin(Align.center);
    background.setScale(layout.backgroundScale());
    background.setPosition(getX(Align.center), getY(Align.center), Align.center);
    this.addActor(background);

    Table parentTable = new Table();
    parentTable.setFillParent(true);
    this.addActor(parentTable);

    label = new Label("", UIUtils.defaultSkin(), "keypad");
    label.setFontScale(1.25f);
    parentTable.add(label).height(120).padBottom(layout.labelPadBottom()).row();

    Table buttons = new Table();
    parentTable.add(buttons);
    for (List<String> row : layout.rows()) {
      for (String action : row) {
        TextButton button = new TextButton(buttonLabel(action), UIUtils.defaultSkin(), "keypad");
        button.getLabel().setFontScale(action.length() == 1 ? 2f : layout.actionFontScale());
        button.addListener(
            new ClickListener() {
              @Override
              public void clicked(InputEvent e, float x, float y) {
                DialogCallbackResolver.createButtonCallback(dialogId, DialogContextKeys.ON_CONFIRM)
                    .accept(new DialogResponseMessage.StringValue(action));
              }
            });
        buttons.add(button).size(layout.buttonSize()).pad(layout.buttonPad());
      }
      buttons.row();
    }
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    this.setScale(0.75f);
    this.setOrigin(Align.center);
    this.setBounds(0, 0, Game.windowWidth(), Game.windowHeight());

    // Before the first synchronized state arrives, the keypad is shown empty.
    Optional<KeypadComponent> component = keypad.fetch(KeypadComponent.class);
    boolean unlocked = component.map(KeypadComponent::unlocked).orElse(false);

    background.setPosition(getX(Align.center), getY(Align.center), Align.center);
    background.setDrawable(UIUtils.defaultSkin(), unlocked ? "keypad-ui-on" : "keypad-ui-off");
    label.setText(component.map(KeypadComponent::display).orElse(""));

    super.draw(batch, parentAlpha);
  }

  private static String buttonLabel(String action) {
    return switch (action) {
      case ACTION_BACK -> TRANSLATION.text("back");
      case ACTION_SPACE -> TRANSLATION.text("space");
      case ACTION_SUBMIT -> TRANSLATION.text("submit");
      default -> action;
    };
  }
}
