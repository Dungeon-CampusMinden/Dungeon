package contrib.hud.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.elements.RichLabel;
import core.Game;
import core.game.PreRunConfiguration;
import core.network.ConnectionListener;
import core.network.client.ClientConnectionConfig;
import core.utils.BaseContainerUI;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Package-private builder for the local multiplayer client connection dialog. */
final class ClientConnectionDialog {

  private static final String TITLE = "Server verbinden";
  private static final String CONNECT_BUTTON = "Verbinden";
  private static final String HOST_LABEL = "IP / Host";
  private static final String PORT_LABEL = "Port";
  private static final String START_FAILED_MESSAGE = "Server nicht erreichbar.";
  private static final String CONNECTING_MESSAGE = "Verbindung wird aufgebaut...";
  private static final String ERROR_COLOR = "#bb0000";
  private static final String STATUS_COLOR = "light_gray";

  private static final ExecutorService CONNECT_EXECUTOR =
      Executors.newCachedThreadPool(
          runnable -> {
            Thread thread = new Thread(runnable, "client-connect");
            thread.setDaemon(true);
            return thread;
          });
  private static final java.util.Set<String> CONNECTING_DIALOGS = ConcurrentHashMap.newKeySet();

  private ClientConnectionDialog() {}

  static Group build(DialogContext ctx) {
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup(TITLE, "", CONNECT_BUTTON);
    }

    Skin skin = UIUtils.defaultSkin();
    TextField hostField = new TextField("", skin);
    hostField.setMessageText(ClientConnectionConfig.DEFAULT_HOST);
    TextField portField = new TextField("", skin);
    portField.setMessageText(Integer.toString(PreRunConfiguration.networkPort()));
    RichLabel errorLabel = new RichLabel("", DialogDesign.DIALOG_FONT_SPEC_NORMAL);
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
    fields.add(new RichLabel(HOST_LABEL, DialogDesign.DIALOG_FONT_SPEC_NORMAL)).left();
    fields.add(new RichLabel(PORT_LABEL, DialogDesign.DIALOG_FONT_SPEC_NORMAL)).left().row();
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
      RichLabel errorLabel,
      String button) {
    if (!CONNECT_BUTTON.equals(button)) {
      return false;
    }

    ClientConnectionConfig config;
    try {
      config =
          ClientConnectionConfig.fromFields(
              hostField.getText(),
              portField.getText(),
              ClientConnectionConfig.DEFAULT_HOST,
              PreRunConfiguration.networkPort());
    } catch (IllegalArgumentException e) {
      showError(errorLabel, e.getMessage());
      return false;
    }

    if (!CONNECTING_DIALOGS.add(context.dialogId())) {
      showStatus(errorLabel, CONNECTING_MESSAGE);
      return false;
    }

    setInputDisabled(hostField, portField, true);
    showStatus(errorLabel, CONNECTING_MESSAGE);
    ConnectionListener listener = connectionListener(context, hostField, portField, errorLabel);
    Game.network().addConnectionListener(listener);
    try {
      CONNECT_EXECUTOR.execute(
          () -> runConnectAttempt(context, config, hostField, portField, errorLabel, listener));
    } catch (RuntimeException e) {
      completeConnectAttempt(context, hostField, portField, errorLabel, false, listener);
    }
    return false;
  }

  private static void runConnectAttempt(
      DialogContext context,
      ClientConnectionConfig config,
      TextField hostField,
      TextField portField,
      RichLabel errorLabel,
      ConnectionListener listener) {
    try {
      PreRunConfiguration.networkServerAddress(config.host());
      PreRunConfiguration.networkPort(config.port());
      Game.initializeNetwork();
      Game.network().start();
    } catch (RuntimeException e) {
      postToUiThread(
          () -> completeConnectAttempt(context, hostField, portField, errorLabel, false, listener));
    }
  }

  private static ConnectionListener connectionListener(
      DialogContext context, TextField hostField, TextField portField, RichLabel errorLabel) {
    return new ConnectionListener() {
      @Override
      public void onConnected() {
        completeConnectAttempt(context, hostField, portField, errorLabel, true, this);
      }

      @Override
      public void onDisconnected(String reason) {
        completeConnectAttempt(context, hostField, portField, errorLabel, false, this);
      }
    };
  }

  private static void completeConnectAttempt(
      DialogContext context,
      TextField hostField,
      TextField portField,
      RichLabel errorLabel,
      boolean success,
      ConnectionListener listener) {
    Game.network().removeConnectionListener(listener);
    CONNECTING_DIALOGS.remove(context.dialogId());
    if (success) {
      uiComponent(context).ifPresent(ui -> UIUtils.closeDialog(ui, true));
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

  private static void showStatus(RichLabel errorLabel, String message) {
    showRichMessage(errorLabel, STATUS_COLOR, message);
  }

  private static void showError(RichLabel errorLabel, String message) {
    showRichMessage(errorLabel, ERROR_COLOR, message);
  }

  private static void showRichMessage(RichLabel errorLabel, String color, String message) {
    errorLabel.setText("[color=" + color + "]" + RichLabel.toRichText(message) + "[/color]");
    errorLabel.setVisible(true);
  }

  private static Optional<UIComponent> uiComponent(DialogContext context) {
    return context
        .find(DialogContextKeys.OWNER_ENTITY, Integer.class)
        .flatMap(Game::findEntityById)
        .flatMap(entity -> entity.fetch(UIComponent.class));
  }
}
