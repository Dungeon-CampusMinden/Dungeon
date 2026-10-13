package feature.bugreport;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import engine.Entity;
import engine.Game;
import engine.language.Translation;
import engine.network.NetworkUtils;
import engine.network.input.InputCommandRouter;
import engine.network.messages.c2s.InputMessage;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import engine.tracking.Tracking;
import engine.tracking.TrackingRuntime;
import engine.utils.Cursors;
import engine.utils.FontHelper;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import engine.utils.logging.DungeonLogger;
import feature.hud.DialogUtils;
import feature.hud.UIUtils;
import feature.hud.elements.RichLabel;
import java.nio.charset.StandardCharsets;

/**
 * Lets players report bugs from the pause menu. The authoritative side records each report as a
 * {@code BUG_REPORTED} tracking event of the reporting participant and answers with a popup that
 * says whether it was saved.
 *
 * <p>The pause menu offers the {@link #view} only while {@link #available()}. Network clients send
 * the text as the {@link #COMMAND} custom input, which the hero controller routes to {@link
 * #handle}.
 */
public final class BugReport {

  /** Custom input command carrying the report text as UTF-8. */
  public static final String COMMAND = "core:bug_report";

  /** Maximum report length; its UTF-8 form stays within the custom input payload limit. */
  public static final int MAX_LENGTH = 2_000;

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(BugReport.class);
  private static final Translation trans = new Translation("dialog.bug_report");

  private BugReport() {}

  /**
   * Returns whether tracking records this process's player, so a report can be saved.
   *
   * @return whether the pause menu offers bug reports
   */
  public static boolean available() {
    return TrackingRuntime.recordsLocalPlayer();
  }

  /**
   * Returns the pause menu label of the report button.
   *
   * @return localized button label
   */
  public static String buttonLabel() {
    return trans.text("button");
  }

  /**
   * Builds the pause menu page for writing a report.
   *
   * @param player local player entity that reports
   * @param backButton button that returns to the main pause menu
   * @param onSent runs after the report was sent
   * @return report page
   */
  public static Table view(Entity player, TextButton backButton, Runnable onSent) {
    Label title =
        Scene2dElementFactory.createLabel(
            trans.text("title"), FontSpec.of("fonts/Roboto-Bold.ttf", 48, Color.BLACK));
    RichLabel hint = new RichLabel("[color=#555555][size=18]" + trans.text("hint"));
    hint.setWrap(true);
    hint.setMaxPrefWidth(500);

    TextField.TextFieldStyle style =
        new TextField.TextFieldStyle(UIUtils.defaultSkin().get(TextField.TextFieldStyle.class));
    style.font = FontHelper.getFont(FontSpec.of("fonts/Roboto-Regular.ttf", 20, Color.WHITE));
    style.fontColor = Color.BLACK;
    TextArea input = new TextArea("", style);
    input.setMaxLength(MAX_LENGTH);
    input.setUserObject(Cursors.TEXT);

    // Shows how many of the allowed characters the report uses, such as "12/2000".
    Label counter =
        Scene2dElementFactory.createLabel(
            "0/" + MAX_LENGTH,
            FontSpec.of("fonts/Roboto-Regular.ttf", 16, Color.valueOf("555555")));

    TextButton sendButton = Scene2dElementFactory.createButton(trans.text("send"), "green", 32);
    sendButton.setDisabled(true);
    Scene2dElementFactory.addTextFieldChangeListener(
        input,
        text -> {
          sendButton.setDisabled(text.isBlank());
          counter.setText(text.length() + "/" + MAX_LENGTH);
        });
    sendButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            if (input.getText().isBlank()) return;
            submit(player, input.getText());
            Sounds.playUi(CoreSounds.INTERFACE_BUTTON_CLICKED);
            onSent.run();
          }
        });

    Table page = new Table();
    page.add(title).padBottom(15).align(Align.center).row();
    page.add(Scene2dElementFactory.createHorizontalDivider()).growX().padBottom(10).row();
    page.add(hint).width(500).padBottom(10).row();
    page.add(input).width(500).height(250).row();
    page.add(counter).right().padTop(4).row();
    page.add(Scene2dElementFactory.createHorizontalDivider()).growX().padTop(6).row();
    page.add(sendButton).width(300).align(Align.center).padTop(15).padBottom(10).row();
    page.add(backButton).width(300).align(Align.center).padBottom(15).row();
    Game.stage().ifPresent(stage -> stage.setKeyboardFocus(input));
    return page;
  }

  /**
   * Handles {@link #COMMAND} from a network client on the server.
   *
   * @param context routed input of the reporting client
   */
  public static void handle(InputCommandRouter.InputCommandContext context) {
    InputMessage.Custom request = context.payloadAs(InputMessage.Custom.class);
    record(context.playerEntity(), new String(request.payload(), StandardCharsets.UTF_8));
  }

  private static void submit(Entity player, String text) {
    if (NetworkUtils.isNetworkClient()) {
      Game.network()
          .send(
              (short) 0, InputMessage.custom(COMMAND, text.getBytes(StandardCharsets.UTF_8)), true);
    } else {
      record(player, text);
    }
  }

  private static void record(Entity player, String text) {
    // PostgreSQL cannot store NUL in JSON, so such a report would block the session's upload.
    boolean saved =
        !text.isBlank()
            && text.length() <= MAX_LENGTH
            && text.indexOf('\u0000') < 0
            && Tracking.participantForEntity(player.id())
                .flatMap(participant -> Tracking.bugReported(text, participant))
                .isPresent();
    if (!saved) LOGGER.warn("Bug report of entity {} was not recorded", player.id());
    DialogUtils.showTextPopup(
        trans.text(saved ? "saved" : "failed"), trans.text("title"), player.id());
  }
}
