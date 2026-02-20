package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import core.sound.Sounds;
import core.utils.Scene2dElementFactory;
import modules.computer.ComputerDialog;
import modules.computer.ComputerProgress;
import modules.computer.ComputerStateComponent;
import util.LastHourSounds;
import util.Lore;

public class LoginTab extends ComputerTab {

  // Password feedback
  private static final String WRONG_FEEDBACK = "Invalid username or password.";
  private static final String CORRECT_FEEDBACK = "Login successful!\nWelcome Mr. So-And-So";
  private static final Color WRONG_COLOR = Color.RED;
  private static final Color CORRECT_COLOR = new Color(0, 0.5f, 0, 1);

  private TextField usernameField;
  private TextField passwordField;
  private Button loginButton;
  private Label loginFeedback;

  public LoginTab(ComputerStateComponent sharedState) {
    super(sharedState, "login", "Login", false);
  }

  protected void createActors() {
    boolean completed = sharedState().state().hasReached(ComputerProgress.LOGGED_IN);
    if (completed) {
      localState().username(Lore.LoginEmail);
      localState().password(Lore.LoginPassword);
    }

    Image companyLogo = new Image(skin, Lore.CompanyDrawable);
    this.add(companyLogo).width(200).height(200).center().padBottom(20).row();

    Label label = Scene2dElementFactory.createLabel(Lore.CompanyName, 64, Color.BLACK);
    this.add(label).center().padBottom(10).row();
    Label flavor =
        Scene2dElementFactory.createLabel(
            "At the frontlines of science since 1984", 24, Color.GRAY);
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

    loginButton = Scene2dElementFactory.createButton("Login", "clean-green");
    loginButton.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            String username = localState().username();
            String password = localState().password();
            if ((username.equalsIgnoreCase(Lore.LoginEmail)
                    && password.equalsIgnoreCase(Lore.LoginPassword))
                || username.equals("skipp")) {
              ComputerStateComponent.setState(ComputerProgress.LOGGED_IN);
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
      Sounds.playLocal(LastHourSounds.COMPUTER_LOGIN_SUCCESS);
    }
  }

  private void onWrongCredentials() {
    loginFeedback.setText(WRONG_FEEDBACK);
    Sounds.playLocal(LastHourSounds.COMPUTER_LOGIN_FAILED);
  }
}
