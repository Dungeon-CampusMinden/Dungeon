package escaperoom.foundation.ui;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Game;
import engine.utils.BaseContainerUI;
import engine.utils.FontHelper;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import engine.utils.Tuple;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.hud.dialogs.HeadlessDialogGroup;
import java.util.List;
import java.util.Objects;

/**
 * A UI component that displays a black background with text messages in sequence. Messages advance
 * on click and can fade in/out on show and hide.
 */
public final class BlackFadeCutscene extends Table {

  private static final String FONT_SIZES_KEY = "font_sizes";
  private static final String FADE_IN_KEY = "fadeIn";
  private static final String FADE_OUT_KEY = "fadeOut";

  private static final float FADE_DURATION = 1.5f;
  private static final float TEXT_FADE_DURATION = 0.7f;
  private static final int FONT_SIZE = 32;

  private final String[] messages;
  private final int[] fontSizes;
  private final boolean fadeIn;
  private final boolean fadeOut;

  private int currentMessageIndex = 0;
  private Label messageLabel;
  private boolean isAnimating = false;

  private DialogContext ctx;

  /** Registers the reusable black-fade cutscene renderer. */
  public static void register() {
    DialogFactory.register(CutsceneDialogType.TEXT_CUTSCENE, BlackFadeCutscene::build);
  }

  /**
   * Creates a new BlackFadeCutscene.
   *
   * @param messages The list of text messages to display in sequence
   * @param fontSizes The list of font sizes corresponding to each message
   * @param fadeIn Whether to fade in when showing
   * @param fadeOut Whether to fade out when hiding
   * @param ctx The dialog context containing configuration for the cutscene
   */
  private BlackFadeCutscene(
      String[] messages, int[] fontSizes, boolean fadeIn, boolean fadeOut, DialogContext ctx) {
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
   * @param targetIds The target entity ids this UI should be shown for
   * @return The created UIComponent
   * @throws NullPointerException if onComplete is null
   */
  public static UIComponent show(
      List<Tuple<String, Integer>> messages,
      boolean fadeIn,
      boolean fadeOut,
      Runnable onComplete,
      int... targetIds) {
    Objects.requireNonNull(onComplete, "onComplete callback cannot be null");
    String[] messageTexts = messages.stream().map(Tuple::a).toArray(String[]::new);
    int[] fontSizes = messages.stream().mapToInt(message -> message.b()).toArray();

    DialogContext ctx =
        DialogContext.builder()
            .type(CutsceneDialogType.TEXT_CUTSCENE)
            .put(DialogContextKeys.MESSAGE, messageTexts)
            .put(FONT_SIZES_KEY, fontSizes)
            .put(FADE_IN_KEY, fadeIn)
            .put(FADE_OUT_KEY, fadeOut)
            .build();

    UIComponent ui = DialogFactory.show(ctx, true, false, targetIds);

    ui.registerCallback(
        DialogContextKeys.ON_RESUME,
        data -> {
          UIUtils.closeDialog(ui, true);
          onComplete.run();
        });

    return ui;
  }

  /**
   * Shows the BlackFadeCutscene with default fade settings (both enabled).
   *
   * @param messages The list of text messages to display
   * @param onComplete Callback to run when all messages have been shown
   * @return The created UIComponent
   * @throws NullPointerException if onComplete is null
   */
  public static UIComponent show(List<Tuple<String, Integer>> messages, Runnable onComplete) {
    Objects.requireNonNull(onComplete, "onComplete callback cannot be null");
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
  public static Group build(DialogContext ctx) {
    String[] messages = ctx.require(DialogContextKeys.MESSAGE, String[].class);
    int[] fontSizes = ctx.require(FONT_SIZES_KEY, int[].class);
    boolean fadeIn = ctx.find(FADE_IN_KEY, Boolean.class).orElse(true);
    boolean fadeOut = ctx.find(FADE_OUT_KEY, Boolean.class).orElse(true);

    // On headless server, return a placeholder
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup();
    }

    return new BaseContainerUI(
        new BlackFadeCutscene(messages, fontSizes, fadeIn, fadeOut, ctx), true, false);
  }

  private void createActors() {
    setBackground(UIUtils.defaultSkin().newDrawable("white", Color.BLACK));

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
    if (currentMessageIndex < messages.length) {
      isAnimating = true;
      String text = messages[currentMessageIndex];
      int fontSize = fontSizes[currentMessageIndex];
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

    if (currentMessageIndex < messages.length) {
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

  private enum CutsceneDialogType implements DialogType {
    TEXT_CUTSCENE("text_cutscene");

    private final String type;

    CutsceneDialogType(final String type) {
      this.type = type;
    }

    @Override
    public String type() {
      return type;
    }
  }
}
