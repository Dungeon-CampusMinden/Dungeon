package contrib.hud.dialogs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
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
import core.network.config.NetworkConfig;
import core.network.messages.s2c.ConnectReject;
import core.utils.BaseContainerUI;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Package-private builder for the local multiplayer client connection dialog. */
final class ClientConnectionDialog {

  private static final String TITLE = "Server verbinden";
  private static final String CONNECT_BUTTON = "Verbinden";
  private static final String USERNAME_LABEL = "Spielername";
  private static final String HOST_LABEL = "IP / Host";
  private static final String PORT_LABEL = "Port";
  private static final String START_FAILED_MESSAGE = "Server nicht erreichbar.";
  private static final String INVALID_USERNAME_MESSAGE =
      "Spielername darf nicht leer sein und keinen Unterstrich enthalten.";
  private static final String REJECTED_USERNAME_MESSAGE =
      "Spielername ungültig oder bereits vergeben.";
  private static final String CONNECTING_MESSAGE = "Verbindung wird aufgebaut...";
  private static final String FALLBACK_USERNAME = "Player";
  private static final String VERSION_COLOR = "#777777";
  private static final float USERNAME_FIELD_WIDTH = 438f;
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
    RichLabel usernameLabel = new RichLabel(USERNAME_LABEL, DialogDesign.DIALOG_FONT_SPEC_NORMAL);
    String defaultUsername = defaultUsername();
    TextField usernameField = new TextField("", skin);
    usernameField.setMessageText(defaultUsername);
    TextField hostField = new TextField("", skin);
    hostField.setMessageText(ClientConnectionConfig.DEFAULT_HOST);
    TextField portField = new TextField("", skin);
    portField.setMessageText(Integer.toString(PreRunConfiguration.networkPort()));
    RichLabel errorLabel = new RichLabel("", DialogDesign.DIALOG_FONT_SPEC_NORMAL);
    errorLabel.setVisible(false);
    ConnectionForm form =
        new ConnectionForm(
            usernameField, hostField, portField, usernameLabel, errorLabel, defaultUsername);

    Dialog dialog =
        new HandledDialog(
            TITLE, skin, (ignoredDialog, button) -> handleConnect(ctx, form, button));
    DialogDesign.setDialogDefaults(dialog, TITLE);
    clearFocusOnEscape(dialog);

    Table fields = new Table();
    fields.defaults().pad(4);
    fields.add(usernameLabel).left();
    fields.add(versionLabel()).right().top().pad(0).padTop(-2f).row();
    fields.add(usernameField).width(USERNAME_FIELD_WIDTH).colspan(2).row();
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

  private static boolean handleConnect(DialogContext context, ConnectionForm form, String button) {
    if (!CONNECT_BUTTON.equals(button)) {
      return false;
    }

    resetUsernameHint(form);
    String username = usernameFromField(form);
    try {
      PreRunConfiguration.username(username);
      if (!form.usernameField().getText().isBlank()) {
        form.usernameField().setText(username);
      }
    } catch (IllegalArgumentException e) {
      showInvalidUsername(form, INVALID_USERNAME_MESSAGE);
      return false;
    }

    ClientConnectionConfig config;
    try {
      config =
          ClientConnectionConfig.fromFields(
              form.hostField().getText(),
              form.portField().getText(),
              ClientConnectionConfig.DEFAULT_HOST,
              PreRunConfiguration.networkPort());
    } catch (IllegalArgumentException e) {
      showError(form.errorLabel(), e.getMessage());
      return false;
    }

    if (!CONNECTING_DIALOGS.add(context.dialogId())) {
      showStatus(form.errorLabel(), CONNECTING_MESSAGE);
      return false;
    }

    setInputDisabled(form, true);
    showStatus(form.errorLabel(), CONNECTING_MESSAGE);
    ConnectionListener listener = connectionListener(context, form);
    Game.network().addConnectionListener(listener);
    try {
      CONNECT_EXECUTOR.execute(() -> runConnectAttempt(context, config, form, listener));
    } catch (RuntimeException e) {
      completeConnectAttempt(context, form, false, listener);
    }
    return false;
  }

  private static void runConnectAttempt(
      DialogContext context,
      ClientConnectionConfig config,
      ConnectionForm form,
      ConnectionListener listener) {
    try {
      PreRunConfiguration.networkServerAddress(config.host());
      PreRunConfiguration.networkPort(config.port());
      Game.initializeNetwork();
      Game.network().start();
    } catch (RuntimeException e) {
      postToUiThread(() -> completeConnectAttempt(context, form, false, listener));
    }
  }

  private static ConnectionListener connectionListener(DialogContext context, ConnectionForm form) {
    return new ConnectionListener() {
      @Override
      public void onConnected() {
        completeConnectAttempt(context, form, true, this);
      }

      @Override
      public void onDisconnected(String reason) {
        completeConnectAttempt(context, form, false, this);
      }

      @Override
      public void onRejected(ConnectReject.Reason reason) {
        if (reason == ConnectReject.Reason.INVALID_NAME) {
          completeConnectAttempt(context, form, false, this, REJECTED_USERNAME_MESSAGE, true);
          return;
        }
        completeConnectAttempt(context, form, false, this, reason.toString(), false);
      }
    };
  }

  private static void completeConnectAttempt(
      DialogContext context, ConnectionForm form, boolean success, ConnectionListener listener) {
    completeConnectAttempt(context, form, success, listener, START_FAILED_MESSAGE, false);
  }

  private static void completeConnectAttempt(
      DialogContext context,
      ConnectionForm form,
      boolean success,
      ConnectionListener listener,
      String failureMessage,
      boolean usernameError) {
    Game.network().removeConnectionListener(listener);
    CONNECTING_DIALOGS.remove(context.dialogId());
    if (success) {
      uiComponent(context).ifPresent(ui -> UIUtils.closeDialog(ui, true));
      return;
    }
    setInputDisabled(form, false);
    if (usernameError) {
      showInvalidUsername(form, failureMessage);
      return;
    }
    resetUsernameHint(form);
    showError(form.errorLabel(), failureMessage);
  }

  private static void postToUiThread(Runnable runnable) {
    if (Gdx.app == null) {
      runnable.run();
      return;
    }
    Gdx.app.postRunnable(runnable);
  }

  private static void setInputDisabled(ConnectionForm form, boolean disabled) {
    form.usernameField().setDisabled(disabled);
    form.hostField().setDisabled(disabled);
    form.portField().setDisabled(disabled);
  }

  private static void showStatus(RichLabel errorLabel, String message) {
    showRichMessage(errorLabel, STATUS_COLOR, message);
  }

  private static void showError(RichLabel errorLabel, String message) {
    showRichMessage(errorLabel, ERROR_COLOR, message);
  }

  private static void showInvalidUsername(ConnectionForm form, String message) {
    form.usernameLabel().setText("[color=" + ERROR_COLOR + "]" + USERNAME_LABEL + "[/color]");
    showError(form.errorLabel(), message);
    focusUsername(form.usernameField());
  }

  private static void resetUsernameHint(ConnectionForm form) {
    form.usernameLabel().setText(USERNAME_LABEL);
  }

  private static String usernameFromField(ConnectionForm form) {
    String username = form.usernameField().getText().trim();
    return username.isEmpty() ? form.defaultUsername() : username;
  }

  private static String defaultUsername() {
    return normalizeUsername(System.getProperty("user.name"));
  }

  private static String normalizeUsername(String username) {
    if (username == null) {
      return FALLBACK_USERNAME;
    }

    String normalized = username.trim();
    int emailSeparator = normalized.indexOf('@');
    if (emailSeparator >= 0) {
      normalized = normalized.substring(0, emailSeparator);
    }

    normalized = normalized.replace('_', ' ').replaceAll("\\s+", " ").trim();
    if (normalized.isBlank()) {
      return FALLBACK_USERNAME;
    }

    return capitalizeWords(normalized);
  }

  private static String capitalizeWords(String username) {
    String[] words = username.toLowerCase(Locale.ROOT).split(" ");
    StringBuilder result = new StringBuilder(username.length());
    for (String word : words) {
      if (word.isBlank()) {
        continue;
      }
      if (!result.isEmpty()) {
        result.append(' ');
      }
      result.append(Character.toTitleCase(word.charAt(0))).append(word.substring(1));
    }
    return result.isEmpty() ? FALLBACK_USERNAME : result.toString();
  }

  private static void focusUsername(TextField usernameField) {
    usernameField.selectAll();
    Game.stage().ifPresent(stage -> stage.setKeyboardFocus(usernameField));
  }

  private static RichLabel versionLabel() {
    return new RichLabel(
        "[color="
            + VERSION_COLOR
            + "][size=14]Client-Version "
            + NetworkConfig.PROTOCOL_VERSION
            + "[/size][/color]",
        DialogDesign.DIALOG_FONT_SPEC_NORMAL);
  }

  private static void clearFocusOnEscape(Dialog dialog) {
    dialog.addCaptureListener(
        new InputListener() {
          @Override
          public boolean keyDown(InputEvent event, int keycode) {
            if (keycode != Input.Keys.ESCAPE) {
              return false;
            }
            event.getStage().setKeyboardFocus(null);
            event.getStage().setScrollFocus(null);
            event.stop();
            return true;
          }
        });
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

  private record ConnectionForm(
      TextField usernameField,
      TextField hostField,
      TextField portField,
      RichLabel usernameLabel,
      RichLabel errorLabel,
      String defaultUsername) {}
}
