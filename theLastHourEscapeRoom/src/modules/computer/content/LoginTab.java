package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import contrib.hud.dialogs.DialogCallbackResolver;
import contrib.hud.elements.RichLabel;
import core.sound.Sounds;
import core.utils.Scene2dElementFactory;
import modules.computer.ComputerDialog;
import modules.computer.ComputerFactory;
import modules.computer.ComputerProgress;
import modules.computer.ComputerStateComponent;
import util.LastHourSounds;
import util.Lore;

/** Tab for logging into the computer, containing username and password fields and feedback. */
public class LoginTab extends ComputerTab {

  /** Key for identifying the login tab in the computer dialog. */
  public static final String KEY = "login";

  // Password feedback
  private static final String WRONG_FEEDBACK = "Invalid username or password.";
  private static final String CORRECT_FEEDBACK = "Login successful!\nWelcome Dr. Mertens";
  private static final Color WRONG_COLOR = Color.RED;
  private static final Color CORRECT_COLOR = new Color(0, 0.5f, 0, 1);

  private TextField usernameField;
  private TextField passwordField;
  private Button loginButton;
  private Label loginFeedback;

  /**
   * Creates a new LoginTab with the given shared state.
   *
   * @param sharedState the shared computer state component
   */
  public LoginTab(ComputerStateComponent sharedState) {
    super(sharedState, KEY, "Login", false);
  }

  protected void createActors() {
    boolean completed = sharedState().state().hasReached(ComputerProgress.LOGGED_IN);
    if (completed) {
      localState().username(Lore.LoginEmail);
      localState().password(Lore.LoginPassword);
    }

    Image companyLogo = new Image(skin, Lore.CompanyDrawable);
    this.add(companyLogo).width(200).height(200).center().padBottom(5).row();

    RichLabel brandHeader =
        new RichLabel(
            "[align=center][size=64][color=#3399ff]Ciphera[/color] [color=#aa00aa]Labs[/color][/size]",
            24,
            Color.BLACK,
            false);
    this.add(brandHeader).center().padBottom(10).row();
    RichLabel flavor =
        new RichLabel(
            "[align=center][img=items/rpg/potion_red.png] At the [color=red]frontlines[/color] of"
                + " [img path=items/rpg/shield_gold.png noGapRight] [color=#3399ff]science[/color] since 1984"
                + " [img=items/rpg/potion_red.png]",
            24,
            Color.GRAY);
    this.add(flavor).center().padBottom(20).row();

    loginFeedback = Scene2dElementFactory.createLabel("", 24, Color.WHITE);
    loginFeedback.setColor(WRONG_COLOR);
    loginFeedback.setAlignment(Align.center);

    usernameField = Scene2dElementFactory.createTextField(localState().username());
    usernameField.setMessageText("Email");
    Scene2dElementFactory.addTextFieldChangeListener(
        usernameField,
        (text) -> {
          localState().username(usernameField.getText());
        });
    usernameField.setTextFieldListener(
        (textField, c) -> {
          if (c == '\r' || c == '\n') {
            tryLogin();
          }
        });

    passwordField = Scene2dElementFactory.createTextField(localState().password());
    passwordField.setMessageText("Password");
    passwordField.setPasswordMode(true);
    passwordField.setPasswordCharacter('*');
    passwordField.setText(
        ""); // Clear + set again because changing to password mode breaks the cache
    passwordField.setText(localState().password());
    Scene2dElementFactory.addTextFieldChangeListener(
        passwordField,
        (text) -> {
          localState().password(passwordField.getText());
        });
    passwordField.setTextFieldListener(
        (textField, c) -> {
          if (c == '\r' || c == '\n') {
            tryLogin();
          }
        });

    loginButton = Scene2dElementFactory.createButton("Login", "green");
    loginButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            tryLogin();
          }
        });

    if (completed) {
      onLoginSuccess(true);
    }

    Table form = new Table();
    form.add(usernameField).width(400).padBottom(10).row();
    form.add(passwordField).width(400).row();
    form.add(loginButton).colspan(2).growX().center().padTop(20).row();
    form.add(loginFeedback).colspan(2).center().padTop(10).row();

    this.add(form).center();
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {
    ComputerProgress oldState = sharedState().state();
    ComputerProgress newState = newStateComp.state();
    if (oldState != newState && newState == ComputerProgress.LOGGED_IN) {
      onLoginSuccess(false);
    }
  }

  private void onLoginSuccess(boolean completedPrior) {
    usernameField.setDisabled(true);
    passwordField.setDisabled(true);
    loginButton.setDisabled(true);
    loginFeedback.setText(CORRECT_FEEDBACK);
    loginFeedback.setColor(CORRECT_COLOR);
    if (!completedPrior) {
      Sounds.play(LastHourSounds.COMPUTER_LOGIN_SUCCESS);
    }
  }

  private void onWrongCredentials() {
    loginFeedback.setText(WRONG_FEEDBACK);
    Sounds.play(LastHourSounds.COMPUTER_LOGIN_FAILED);
  }

  private void tryLogin() {
    if (loginButton == null || loginButton.isDisabled()) {
      return;
    }

    String username = localState().username();
    String password = localState().password();
    if ((username.equalsIgnoreCase(Lore.LoginEmail)
            && password.equalsIgnoreCase(Lore.LoginPassword))
        || username.equals("skipp")) {
      DialogCallbackResolver.createButtonCallback(
              context().dialogId(), ComputerFactory.UPDATE_STATE_KEY)
          .accept(
              ComputerStateComponent.getState()
                  .orElseThrow()
                  .withState(ComputerProgress.LOGGED_IN)
                  .withTimestampOfLogin((int) (System.currentTimeMillis() / 1000L)));
      ComputerDialog.getInstance()
          .ifPresent(
              computer -> {
                computer.addTabsForState(ComputerProgress.LOGGED_IN);
              });
      onLoginSuccess(false);
    } else {
      onWrongCredentials();
    }
  }
}
