package util.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.*;
import core.Game;
import core.utils.*;
import java.util.List;
import java.util.stream.Stream;
import modules.computer.LastHourDialogTypes;

/**
 * A UI component that displays a black background with text messages in sequence. Messages advance
 * on click and can fade in/out on show and hide.
 */
public class BlackFadeCutscene extends Table {

  public static final String FONT_SIZES_KEY = "font_sizes";
  public static final String MESSAGE_SPLIT_TOKEN = "\n\n";
  public static final String FADE_IN_KEY = "fadeIn";
  public static final String FADE_OUT_KEY = "fadeOut";

  private static final float FADE_DURATION = 1.5f;
  private static final float TEXT_FADE_DURATION = 0.7f;
  private static final int FONT_SIZE = 32;

  private final List<String> messages;
  private final List<Integer> fontSizes;
  private final boolean fadeIn;
  private final boolean fadeOut;

  private int currentMessageIndex = 0;
  private Label messageLabel;
  private boolean isAnimating = false;

  private DialogContext ctx;

  static {
    DialogFactory.register(LastHourDialogTypes.TEXT_CUTSCENE, BlackFadeCutscene::build);
  }

  /**
   * Creates a new BlackFadeCutscene.
   *
   * @param messages The list of text messages to display in sequence
   * @param fadeIn Whether to fade in when showing
   * @param fadeOut Whether to fade out when hiding
   */
  private BlackFadeCutscene(
      List<String> messages,
      List<Integer> fontSizes,
      boolean fadeIn,
      boolean fadeOut,
      DialogContext ctx) {
    this.messages = messages;
    this.fontSizes = fontSizes;
    this.fadeIn = fadeIn;
    this.fadeOut = fadeOut;
    this.ctx = ctx;
    createActors();
  }

  /**
   * Shows the BlackFadeCutscene with the specified parameters.
   *
   * @param messages The list of text messages to display
   * @param fadeIn Whether to fade in when showing
   * @param fadeOut Whether to fade out when hiding
   * @param onComplete Callback to run when all messages have been shown
   * @return The created UIComponent
   */
  public static UIComponent show(
      List<Tuple<String, Integer>> messages,
      boolean fadeIn,
      boolean fadeOut,
      Runnable onComplete,
      int... targetIds) {
    List<String> messagesList = messages.stream().map(Tuple::a).toList();
    List<Integer> fontSizes = messages.stream().map(Tuple::b).toList();
    String joinedMessages = String.join(MESSAGE_SPLIT_TOKEN, messagesList);
    String joinedFontSizes =
        String.join(MESSAGE_SPLIT_TOKEN, fontSizes.stream().map(String::valueOf).toList());

    DialogContext ctx =
        DialogContext.builder()
            .type(LastHourDialogTypes.TEXT_CUTSCENE)
            .put(DialogContextKeys.MESSAGE, joinedMessages)
            .put(FONT_SIZES_KEY, joinedFontSizes)
            .put(FADE_IN_KEY, fadeIn)
            .put(FADE_OUT_KEY, fadeOut)
            .build();

    UIComponent ui = DialogFactory.show(ctx, targetIds);

    // Register callback
    ui.registerCallback(
        DialogContextKeys.ON_RESUME,
        data -> {
          UIUtils.closeDialog(ui, true, true);
        });
    ui.onClose(
        (uic) -> {
          if (onComplete != null) {
            onComplete.run();
          }
        });

    return ui;
  }

  /**
   * Shows the BlackFadeCutscene with default fade settings (both enabled).
   *
   * @param messages The list of text messages to display
   * @param onComplete Callback to run when all messages have been shown
   * @return The created UIComponent
   */
  public static UIComponent show(List<Tuple<String, Integer>> messages, Runnable onComplete) {
    return show(messages, true, true, onComplete);
  }

  /**
   * Builds a pause menu from the given dialog context.
   *
   * <p>On headless servers, returns a {@link HeadlessDialogGroup} placeholder.
   *
   * @param ctx The dialog context containing the message, title, and confirmation callback
   * @return A fully configured pause menu or HeadlessDialogGroup
   */
  static Group build(DialogContext ctx) {
    String messages = ctx.require(DialogContextKeys.MESSAGE, String.class);
    // Split by special token into individual messages
    List<String> messageList = List.of(messages.split(MESSAGE_SPLIT_TOKEN));
    List<Integer> fontSizes =
        Stream.of(ctx.require(FONT_SIZES_KEY, String.class).split(MESSAGE_SPLIT_TOKEN))
            .map(Integer::valueOf)
            .toList();
    boolean fadeIn = ctx.find(FADE_IN_KEY, Boolean.class).orElse(true);
    boolean fadeOut = ctx.find(FADE_OUT_KEY, Boolean.class).orElse(true);

    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup();
    }

    return new BaseContainerUI(
        new BlackFadeCutscene(messageList, fontSizes, fadeIn, fadeOut, ctx), true, false);
  }

  private void createActors() {
    // Create black background texture
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.BLACK);
    pixmap.fill();
    Texture blackTexture = new Texture(pixmap);
    pixmap.dispose();
    setBackground(new TextureRegionDrawable(blackTexture));

    this.setTouchable(Touchable.enabled);

    // Create centered message label
    messageLabel = Scene2dElementFactory.createLabel("", FontSpec.of(FONT_SIZE, Color.WHITE));
    messageLabel.setAlignment(Align.center);
    messageLabel.setWrap(true);
    this.add(messageLabel).width(Game.windowWidth() * 0.5f).center();

    // Add click listener to advance messages
    this.addListener(
        new ClickListener(Input.Buttons.LEFT) {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            if (!isAnimating) {
              advanceMessage();
            }
          }
        });

    // Initial setup
    if (fadeIn) {
      this.getColor().a = 0f;
      messageLabel.getColor().a = 0f;
      this.addAction(
          Actions.sequence(Actions.fadeIn(FADE_DURATION), Actions.run(this::showCurrentMessage)));
    } else {
      messageLabel.getColor().a = 0f;
      showCurrentMessage();
    }
  }

  private void showCurrentMessage() {
    if (currentMessageIndex < messages.size()) {
      isAnimating = true;
      String text = messages.get(currentMessageIndex);
      Integer fontSize = fontSizes.get(currentMessageIndex);
      Label.LabelStyle style = messageLabel.getStyle();
      style.font = FontHelper.getFont(FontSpec.of(fontSize));
      messageLabel.setStyle(style);
      messageLabel.setText(text);
      messageLabel.addAction(
          Actions.sequence(
              Actions.fadeIn(TEXT_FADE_DURATION), Actions.run(() -> isAnimating = false)));
    }
  }

  private void advanceMessage() {
    currentMessageIndex++;

    if (currentMessageIndex < messages.size()) {
      // Fade out current message, then show next
      isAnimating = true;
      messageLabel.addAction(
          Actions.sequence(
              Actions.fadeOut(TEXT_FADE_DURATION), Actions.run(this::showCurrentMessage)));
    } else {
      // All messages shown, complete the cutscene
      completeCutscene();
    }
  }

  private void completeCutscene() {
    isAnimating = true;
    if (fadeOut) {
      // Fade out text first, then fade out background, then run callback
      messageLabel.addAction(Actions.fadeOut(TEXT_FADE_DURATION));
      this.addAction(
          Actions.sequence(
              Actions.delay(TEXT_FADE_DURATION),
              Actions.fadeOut(FADE_DURATION),
              Actions.run(
                  () -> {
                    DialogCallbackResolver.createButtonCallback(
                            ctx.dialogId(), DialogContextKeys.ON_RESUME)
                        .accept(null);
                  })));
    } else {
      DialogCallbackResolver.createButtonCallback(ctx.dialogId(), DialogContextKeys.ON_RESUME)
          .accept(null);
    }
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    // Update label width on resize
    if (this.getCell(messageLabel) != null) {
      this.getCell(messageLabel).width(Game.windowWidth() * 0.5f);
    }
  }
}
