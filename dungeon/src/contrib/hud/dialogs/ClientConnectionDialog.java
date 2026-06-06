package contrib.hud.dialogs;

import com.badlogic.gdx.Gdx;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

/** Package-private builder for the local multiplayer client connection dialog. */
final class ClientConnectionDialog {

  private static final String TITLE = "Server verbinden";
  private static final String CONNECT_BUTTON = "Verbinden";
  private static final String HOST_LABEL = "IP / Host";
  private static final String PORT_LABEL = "Port";
  private static final String START_FAILED_MESSAGE = "Server nicht erreichbar.";
  private static final String MISSING_CALLBACK_MESSAGE =
      "Verbindung kann gerade nicht gestartet werden.";
  private static final String CONNECTING_MESSAGE = "Verbindung wird aufgebaut...";

  private static final Map<String, Function<ClientConnectionConfig, Boolean>> CONNECT_CALLBACKS =
      new ConcurrentHashMap<>();
  private static final ExecutorService CONNECT_EXECUTOR =
      Executors.newCachedThreadPool(
          runnable -> {
            Thread thread = new Thread(runnable, "client-connect");
            thread.setDaemon(true);
            return thread;
          });
  private static final java.util.Set<String> CONNECTING_DIALOGS = ConcurrentHashMap.newKeySet();

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

    if (!CONNECTING_DIALOGS.add(context.dialogId())) {
      showStatus(errorLabel, CONNECTING_MESSAGE);
      return false;
    }

    setInputDisabled(hostField, portField, true);
    showStatus(errorLabel, CONNECTING_MESSAGE);
    try {
      CONNECT_EXECUTOR.execute(
          () -> runConnectAttempt(context, callback, config, hostField, portField, errorLabel));
    } catch (RuntimeException e) {
      CONNECTING_DIALOGS.remove(context.dialogId());
      setInputDisabled(hostField, portField, false);
      showError(errorLabel, START_FAILED_MESSAGE);
    }
    return false;
  }

  private static void runConnectAttempt(
      DialogContext context,
      Function<ClientConnectionConfig, Boolean> callback,
      ClientConnectionConfig config,
      TextField hostField,
      TextField portField,
      Label errorLabel) {
    boolean success = false;
    try {
      success = Boolean.TRUE.equals(callback.apply(config));
    } catch (RuntimeException e) {
      success = false;
    }

    boolean connectionStarted = success;
    postToUiThread(
        () -> completeConnectAttempt(context, hostField, portField, errorLabel, connectionStarted));
  }

  private static void completeConnectAttempt(
      DialogContext context,
      TextField hostField,
      TextField portField,
      Label errorLabel,
      boolean success) {
    CONNECTING_DIALOGS.remove(context.dialogId());
    if (success) {
      uiComponent(context).ifPresent(ui -> UIUtils.closeDialog(ui, true));
      removeCallback(context.dialogId());
      return;
    }
    setInputDisabled(hostField, portField, false);
    showError(errorLabel, START_FAILED_MESSAGE);
  }

  private static void postToUiThread(Runnable runnable) {
    if (Gdx.app == null) {
      runnable.run();
      return;
    }
    Gdx.app.postRunnable(runnable);
  }

  private static void setInputDisabled(TextField hostField, TextField portField, boolean disabled) {
    hostField.setDisabled(disabled);
    portField.setDisabled(disabled);
  }

  private static void showStatus(Label errorLabel, String message) {
    errorLabel.setColor(Color.LIGHT_GRAY);
    errorLabel.setText(message);
    errorLabel.setVisible(true);
  }

  private static void showError(Label errorLabel, String message) {
    errorLabel.setColor(Color.RED);
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
