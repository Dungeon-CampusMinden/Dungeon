package contrib.hud.dialogs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import core.Game;
import core.network.client.ClientConnectionConfig;
import core.utils.BaseContainerUI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/** Package-private builder for the local multiplayer client connection dialog. */
final class ClientConnectionDialog {

  private static final String TITLE = "Server verbinden";
  private static final String CONNECT_BUTTON = "Verbinden";
  private static final String HOST_LABEL = "IP / Host";
  private static final String PORT_LABEL = "Port";
  private static final String START_FAILED_MESSAGE =
      "Verbindung fehlgeschlagen. Bitte Host und Port prüfen.";
  private static final String MISSING_CALLBACK_MESSAGE = "Verbindungs-Callback fehlt.";

  private static final Map<String, Function<ClientConnectionConfig, Boolean>> CONNECT_CALLBACKS =
      new ConcurrentHashMap<>();

  private ClientConnectionDialog() {}

  static void registerCallback(
      String dialogId, Function<ClientConnectionConfig, Boolean> callback) {
    CONNECT_CALLBACKS.put(dialogId, callback);
  }

  static void removeCallback(String dialogId) {
    CONNECT_CALLBACKS.remove(dialogId);
  }

  static Group build(DialogContext ctx) {
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup(TITLE, "", CONNECT_BUTTON);
    }

    Skin skin = UIUtils.defaultSkin();
    TextField hostField = new TextField("", skin);
    hostField.setMessageText(ClientConnectionConfig.DEFAULT_HOST);
    TextField portField = new TextField("", skin);
    portField.setMessageText(Integer.toString(ClientConnectionConfig.DEFAULT_PORT));
    Label errorLabel = new Label("", skin);
    errorLabel.setColor(Color.RED);
    errorLabel.setVisible(false);

    Dialog dialog =
        new HandledDialog(
            TITLE,
            skin,
            (ignoredDialog, button) ->
                handleConnect(ctx, hostField, portField, errorLabel, button));
    DialogDesign.setDialogDefaults(dialog, TITLE);

    Table fields = new Table();
    fields.defaults().pad(4);
    fields.add(new Label(HOST_LABEL, skin)).left();
    fields.add(new Label(PORT_LABEL, skin)).left().row();
    fields.add(hostField).width(320);
    fields.add(portField).width(110).row();

    dialog.getContentTable().add(fields).padBottom(8).row();
    dialog.getContentTable().add(errorLabel).left().minHeight(24).padBottom(8).row();
    dialog.button(
        CONNECT_BUTTON, CONNECT_BUTTON, skin.get("clean-green", TextButton.TextButtonStyle.class));
    dialog.pack();

    return new BaseContainerUI(dialog);
  }

  private static boolean handleConnect(
      DialogContext context,
      TextField hostField,
      TextField portField,
      Label errorLabel,
      String button) {
    if (!CONNECT_BUTTON.equals(button)) {
      return false;
    }

    ClientConnectionConfig config;
    try {
      config = ClientConnectionConfig.fromFields(hostField.getText(), portField.getText());
    } catch (IllegalArgumentException e) {
      showError(errorLabel, e.getMessage());
      return false;
    }

    Function<ClientConnectionConfig, Boolean> callback = CONNECT_CALLBACKS.get(context.dialogId());
    if (callback == null) {
      showError(errorLabel, MISSING_CALLBACK_MESSAGE);
      return false;
    }

    if (Boolean.TRUE.equals(callback.apply(config))) {
      uiComponent(context).ifPresent(ui -> UIUtils.closeDialog(ui, true));
      removeCallback(context.dialogId());
    } else {
      showError(errorLabel, START_FAILED_MESSAGE);
      return false;
    }
    return true;
  }

  private static void showError(Label errorLabel, String message) {
    errorLabel.setText(message);
    errorLabel.setVisible(true);
  }

  private static Optional<UIComponent> uiComponent(DialogContext context) {
    return context
        .find(DialogContextKeys.OWNER_ENTITY, Integer.class)
        .flatMap(Game::findEntityById)
        .flatMap(entity -> entity.fetch(UIComponent.class));
  }
}
