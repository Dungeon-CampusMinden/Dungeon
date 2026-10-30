package feature.prefabs.types;

import engine.Entity;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.prefabs.ArtifactInput;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
}
