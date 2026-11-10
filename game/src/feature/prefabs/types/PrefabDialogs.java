package feature.prefabs.types;

import engine.Entity;
import engine.network.messages.c2s.DialogResponseMessage;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.ChoiceOptions;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.prefabs.ArtifactInput;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;

/** Shared dialog behavior of prefabs that show images or dialogs to players. */
final class PrefabDialogs {

  private static final Pattern MARKUP = Pattern.compile("\\[[^\\]]*]");
  private static final int PREVIEW_LENGTH = 28;

  private PrefabDialogs() {}

  /**
   * Shows an image dialog to one player.
   *
   * <p>The image path is resolved through {@link engine.language.Localization#asset(String)} by the
   * displaying client, so a variant for its language (e.g. {@code image_en.png}) is used if it
   * exists, falling back to the given path otherwise.
   *
   * @param imagePath image to show
   * @param who player to show the image to
   * @param onClosed called once the player closed the image
   * @return the shown dialog
   */
  static UIComponent showImage(String imagePath, Entity who, Runnable onClosed) {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.IMAGE)
            .put(DialogContextKeys.IMAGE, imagePath)
            .build();
    UIComponent ui = DialogFactory.show(context, who.id());
    ui.registerCallback(DialogContextKeys.ON_CLOSE, data -> onClosed.run());
    return ui;
  }

  /**
   * Shows a dialog script ending in a question to one player. A cancellable question also calls
   * {@code onCancelled} when it is closed without an answer, otherwise the player has to answer.
   *
   * @param question shown question
   * @param player player answering the question
   * @param open dialogs still shown by the asking instance; holds the dialog until it was answered
   * @param onChosen called with the zero-based index of the chosen answer
   * @param onCancelled called when the player cancelled the question
   */
  static void ask(
      Question question,
      Entity player,
      List<UIComponent> open,
      IntConsumer onChosen,
      Runnable onCancelled) {
    List<ChoiceOption> options = new ArrayList<>();
    for (int index = 0; index < question.answers().size(); index++) {
      options.add(ChoiceOption.of(question.answers().get(index), String.valueOf(index)));
    }
    DialogContext.Builder dialog =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.MULTIPLE_CHOICE)
            .put(DialogContextKeys.DIALOG, question.script())
            .put(DialogContextKeys.OPTIONS, new ChoiceOptions(options))
            .put(DialogContextKeys.CAN_CANCEL, question.canCancel());
    if (question.translate()) dialog.put(DialogContextKeys.DIALOG_IS_TRANSLATION_KEY, true);
    // The displaying client replaces the placeholders after resolving translation keys.
    dialog.placeholders(question.placeholders());

    UIComponent ui = DialogFactory.show(dialog.build(), true, question.canCancel(), player.id());
    open.add(ui);
    // Choosing closes the dialog, which must not count as cancelling it. A dialog that is no
    // longer open was answered already or closed by the despawn of its instance.
    ui.registerCallback(
        DialogContextKeys.ON_OPTION_SELECTED,
        data -> {
          if (!open.remove(ui)) return;
          // Forced, since a dialog without cancel option cannot be closed by the player.
          UIUtils.closeDialog(ui, true);
          chosenIndex(data, options.size()).ifPresent(onChosen);
        });
    if (!question.canCancel()) return;
    Runnable cancel =
        () -> {
          if (open.remove(ui)) onCancelled.run();
        };
    ui.registerCallback(
        DialogContextKeys.ON_CANCEL,
        data -> {
          cancel.run();
          UIUtils.closeDialog(ui);
        });
    ui.registerCallback(DialogContextKeys.ON_CLOSE, data -> cancel.run());
  }

  /**
   * Returns the default text of an answer of a new question.
   *
   * @param index zero-based answer index
   * @return default answer text
   */
  static String defaultAnswer(int index) {
    return switch (index) {
      case 0 -> "Yes";
      case 1 -> "No";
      default -> "Answer " + (index + 1);
    };
  }

  /**
   * Closes the dialogs a despawned instance still shows, without raising their events. The list is
   * cleared first, so callbacks of the closed dialogs see them as handled.
   *
   * @param open dialogs shown by the instance
   */
  static void closeAll(List<UIComponent> open) {
    List<UIComponent> dialogs = List.copyOf(open);
    open.clear();
    dialogs.forEach(ui -> UIUtils.closeDialog(ui, true));
  }

  /**
   * Returns the placeholder values of a dialog. Linked values that are not available are shown as
   * {@code ???}.
   *
   * @param values linked placeholder values
   * @return values by alias
   */
  static Map<String, String> placeholders(ArtifactInput<String> values) {
    Map<String, String> available = values.values();
    Map<String, String> result = new LinkedHashMap<>();
    for (String alias : values.aliases()) result.put(alias, available.getOrDefault(alias, "???"));
    return result;
  }

  /**
   * Shortens a dialog script to a single line for editor previews, without its markup.
   *
   * @param script dialog script or text
   * @return the first characters of the text
   */
  static String preview(String script) {
    String text = MARKUP.matcher(script).replaceAll(" ").replaceAll("\\s+", " ").strip();
    return text.length() <= PREVIEW_LENGTH ? text : text.substring(0, PREVIEW_LENGTH - 3) + "...";
  }

  private static OptionalInt chosenIndex(DialogResponseMessage.Payload data, int count) {
    if (!(data instanceof DialogResponseMessage.StringValue(String value))) {
      return OptionalInt.empty();
    }
    try {
      int index = Integer.parseInt(value);
      return index >= 0 && index < count ? OptionalInt.of(index) : OptionalInt.empty();
    } catch (NumberFormatException exception) {
      return OptionalInt.empty();
    }
  }

  /**
   * A dialog script ending in a question.
   *
   * @param script dialog script, or its translation key
   * @param translate whether the script and the answers are translation keys
   * @param answers selectable answers, in display order
   * @param canCancel whether the question has a cancel option
   * @param placeholders values of the {@code {name}} placeholders of the script and the answers
   */
  record Question(
      String script,
      boolean translate,
      List<String> answers,
      boolean canCancel,
      Map<String, String> placeholders) {}
}
