package foundation.multiplayer.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Scaling;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogDesign;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import contrib.hud.dialogs.HeadlessDialogGroup;
import contrib.hud.elements.RichLabel;
import contrib.modules.keypad.KeypadComponent;
import contrib.modules.keypad.KeypadUI;
import contrib.systems.EventScheduler;
import core.Entity;
import core.Game;
import core.network.messages.c2s.DialogResponseMessage;
import core.utils.BaseContainerUI;
import core.utils.Scene2dElementFactory;
import core.utils.Tuple;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.SimpleIPath;
import foundation.definition.HintSeverity;
import foundation.presentation.GamePresentation;
import foundation.presentation.GamePresentation.ResourcePresentation;
import foundation.runtime.ReleasedHint;
import foundation.ui.BlackFadeCutscene;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

/** Foundation-specific views on top of Dungeon's server-owned multiplayer dialog lifecycle. */
public final class FoundationDialogs {
  private static final String RESOURCE_ASSET = "foundation.resource.asset";
  private static final String KEYPAD_DIGIT_LIMIT = "foundation.keypad.digitLimit";
  private static final String KEYPAD_SHOW_DIGIT_COUNT = "foundation.keypad.showDigitCount";
  private static final long KEYPAD_CLOSE_DELAY_MILLIS = 1_000;
  private static final float CONTENT_WIDTH = 600;
  private static final float TEXT_HEIGHT = 220;
  private static final float IMAGE_HEIGHT = 400;
  private static final int INTRO_FONT_SIZE = 32;
  private static final int MISSION_FONT_SIZE = 42;
  private static final int TERMINAL_FONT_SIZE = 32;

  private FoundationDialogs() {}

  /** Registers all Foundation dialog renderers on host and client. */
  public static void register() {
    BlackFadeCutscene.register();
    DialogFactory.register(FoundationDialogType.RESOURCE, FoundationDialogs::resourceDialog);
    DialogFactory.register(FoundationDialogType.KEYPAD, FoundationDialogs::keypadDialog);
  }

  static void showIntro(
      final GamePresentation presentation, final int targetEntityId, final Runnable afterClose) {
    Objects.requireNonNull(presentation, "presentation");
    Objects.requireNonNull(afterClose, "afterClose");
    List<Tuple<String, Integer>> pages =
        Stream.concat(
                presentation.introText().stream().map(text -> Tuple.of(text, INTRO_FONT_SIZE)),
                Stream.of(Tuple.of("EURE MISSION\n\n" + presentation.mission(), MISSION_FONT_SIZE)))
            .toList();
    BlackFadeCutscene.show(pages, true, true, afterClose, targetEntityId);
  }

  static void showResource(
      final ResourcePresentation resource, final int targetEntityId, final Runnable afterClose) {
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(afterClose, "afterClose");
    DialogContext.Builder context =
        DialogContext.builder()
            .type(FoundationDialogType.RESOURCE)
            .put(DialogContextKeys.TITLE, resource.title())
            .put(DialogContextKeys.MESSAGE, resource.text());
    resource.runtimeAssetPath().ifPresent(path -> context.put(RESOURCE_ASSET, path));
    UIComponent dialog = DialogFactory.show(context.build(), targetEntityId);
    dialog.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        ignored -> {
          UIUtils.closeDialog(dialog);
          afterClose.run();
        });
  }

  static void showKeypad(
      final Entity keypad,
      final int digitLimit,
      final boolean showDigitCount,
      final Predicate<String> onAction,
      final int targetEntityId) {
    Objects.requireNonNull(keypad, "keypad");
    Objects.requireNonNull(onAction, "onAction");
    DialogContext context =
        DialogContext.builder()
            .type(FoundationDialogType.KEYPAD)
            .put(DialogContextKeys.ENTITY, keypad.id())
            .put(KEYPAD_DIGIT_LIMIT, digitLimit)
            .put(KEYPAD_SHOW_DIGIT_COUNT, showDigitCount)
            .build();
    UIComponent dialog = DialogFactory.show(context, targetEntityId);
    dialog.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String action)) {
            if (onAction.test(action)) {
              EventScheduler.scheduleAction(
                  () ->
                      dialog
                          .dialogContext()
                          .ownerEntity()
                          .fetch(UIComponent.class)
                          .filter(current -> current == dialog)
                          .ifPresent(UIUtils::closeDialog),
                  KEYPAD_CLOSE_DELAY_MILLIS);
            }
          }
        });
  }

  static void showHint(
      final ReleasedHint hint, final int targetEntityId, final Runnable afterClose) {
    Objects.requireNonNull(hint, "hint");
    Objects.requireNonNull(afterClose, "afterClose");
    DialogFactory.showTextDialog(
        hint.text(), hint.title(), afterClose::run, "Verstanden", targetEntityId);
  }

  static void showHintConfirmation(
      final HintSeverity severity, final int targetEntityId, final Runnable onConfirm) {
    Objects.requireNonNull(severity, "severity");
    Objects.requireNonNull(onConfirm, "onConfirm");
    String category =
        switch (severity) {
          case ORIENTATION -> "Was ist die Aufgabe? Wo kannst du anfangen?";
          case APPROACH -> "Wie kannst du die Aufgabe lösen?";
          case SOLUTION -> "Was ist die Lösung?";
        };
    DialogFactory.showYesNoDialog(
        "Der nächste Hinweis beantwortet:\n\n"
            + category
            + "\n\nMöchtest du diesen Hinweis anzeigen?",
        "Hinweis anzeigen?",
        onConfirm::run,
        () -> {},
        targetEntityId);
  }

  static void showTerminal(
      final List<String> pages, final int targetEntityId, final Runnable afterClose) {
    Objects.requireNonNull(pages, "pages");
    Objects.requireNonNull(afterClose, "afterClose");
    BlackFadeCutscene.show(
        pages.stream().map(text -> Tuple.of(text, TERMINAL_FONT_SIZE)).toList(),
        true,
        true,
        afterClose,
        targetEntityId);
  }

  private static Group resourceDialog(final DialogContext context) {
    String title = context.require(DialogContextKeys.TITLE, String.class);
    String text = context.require(DialogContextKeys.MESSAGE, String.class);
    if (Game.isHeadless()) {
      return new Group();
    }
    Skin skin = UIUtils.defaultSkin();
    Dialog dialog =
        new Dialog(title, skin) {
          @Override
          protected void result(final Object value) {
            DialogCallbackResolver.createButtonCallback(
                    context.dialogId(), DialogContextKeys.ON_CONFIRM)
                .accept(null);
          }
        };
    DialogDesign.setDialogDefaults(dialog, title);
    addScrollableText(dialog, text);
    context
        .find(RESOURCE_ASSET, String.class)
        .ifPresent(
            asset -> {
              Image image = new Image(TextureMap.instance().textureAt(new SimpleIPath(asset)));
              image.setScaling(Scaling.fit);
              dialog
                  .getContentTable()
                  .add(image)
                  .width(CONTENT_WIDTH)
                  .height(IMAGE_HEIGHT)
                  .padBottom(10)
                  .row();
            });
    dialog.button("Schliessen", true, skin.get("green", TextButton.TextButtonStyle.class));
    dialog.key(Input.Keys.ESCAPE, true);
    dialog.pack();
    return new BaseContainerUI(dialog);
  }

  private static Group keypadDialog(final DialogContext context) {
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup();
    }
    Entity keypad = context.requireEntity(DialogContextKeys.ENTITY);
    int digitLimit = context.require(KEYPAD_DIGIT_LIMIT, Integer.class);
    boolean showDigitCount = context.require(KEYPAD_SHOW_DIGIT_COUNT, Boolean.class);
    if (keypad.fetch(KeypadComponent.class).isEmpty()) {
      keypad.add(
          new KeypadComponent(
              new ArrayList<>(Collections.nCopies(digitLimit, -1)), () -> {}, showDigitCount));
    }
    return new KeypadUI(keypad, context.dialogId());
  }

  private static void addScrollableText(final Dialog dialog, final String text) {
    RichLabel label =
        new RichLabel(RichLabel.toRichText(text), DialogDesign.DIALOG_FONT_SPEC_NORMAL);
    label.setWrap(true);
    label.setMaxPrefWidth(CONTENT_WIDTH);
    Table labelTable = new Table();
    labelTable.top().left();
    labelTable.add(label).growX();
    ScrollPane scrollPane = Scene2dElementFactory.createScrollPane(labelTable, false, true);
    scrollPane.setScrollbarsOnTop(false);
    dialog
        .getContentTable()
        .add(scrollPane)
        .width(CONTENT_WIDTH)
        .maxHeight(TEXT_HEIGHT)
        .padBottom(10)
        .row();
  }

  private enum FoundationDialogType implements DialogType {
    RESOURCE("FOUNDATION_RESOURCE"),
    KEYPAD("FOUNDATION_KEYPAD");

    private final String type;

    FoundationDialogType(final String type) {
      this.type = type;
    }

    @Override
    public String type() {
      return type;
    }
  }
}
