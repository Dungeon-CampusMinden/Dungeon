package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import core.utils.components.draw.TextureGenerator;
import modules.computer.ComputerDialog;
import modules.computer.ComputerState;
import modules.computer.ComputerStateComponent;
import modules.computer.ComputerStateLocal;
import util.Scene2dElementFactory;

public class LoginMask extends ComputerTab {

  private static final String USERNAME = "test";
  private static final String PASSWORD = "1234";

  // Password feedback
  private static final String WRONG_FEEDBACK = "Invalid username or password.";
  private static final String CORRECT_FEEDBACK = "Login successful!\nWelcome Mr. So-And-So";
  private static final Color WRONG_COLOR = Color.RED;
  private static final Color CORRECT_COLOR = new Color(0, 0.5f, 0, 1);

  private TextField usernameField;
  private TextField passwordField;
  private Button loginButton;
  private Label loginFeedback;

  public LoginMask(ComputerStateComponent sharedState){
    super(sharedState, "login", "Login", false);
  }

  protected void createActors(){
    boolean completed = sharedState().state().hasReached(ComputerState.LOGGED_IN);
    if(completed){
      localState().username(USERNAME);
      localState().password(PASSWORD);
    }

    Drawable company = new TextureRegionDrawable(TextureGenerator.generateColorTexture(100, 100, new Color(0, 0, 0.7f, 1)));
    Image companyLogo = new Image(company);
    this.add(companyLogo).width(200).height(200).center().padBottom(20).row();

    Label label = Scene2dElementFactory.createLabel("Company XYZ", 64, Color.BLACK);
    this.add(label).center().padBottom(10).row();
    Label flavor = Scene2dElementFactory.createLabel("At the frontlines of science since 1984", 24, Color.GRAY);
    this.add(flavor).center().padBottom(20).row();

    Label usernameLabel = Scene2dElementFactory.createLabel("Username:", 32, Color.BLACK);
    Label passwordLabel = Scene2dElementFactory.createLabel("Password:", 32, Color.BLACK);
    loginFeedback = Scene2dElementFactory.createLabel("", 24, Color.WHITE);
    loginFeedback.setColor(WRONG_COLOR);
    loginFeedback.setAlignment(Align.center);

    usernameField = Scene2dElementFactory.createTextField(localState().username());
    Scene2dElementFactory.addTextFieldChangeListener(usernameField, (text) -> {
      localState()
          .username(usernameField.getText());
    });

    passwordField = Scene2dElementFactory.createTextField(localState().password());
    passwordField.setPasswordMode(true);
    passwordField.setPasswordCharacter('*');
    passwordField.setText(""); // Clear + set again because changing to password mode breaks the cache
    passwordField.setText(localState().password());
    Scene2dElementFactory.addTextFieldChangeListener(passwordField, (text) -> {
      localState()
          .password(passwordField.getText());
    });

    loginButton = Scene2dElementFactory.createButton("Login", "clean-green");
    loginButton.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        String username = localState().username();
        String password = localState().password();
        if(username.equals(USERNAME) && password.equals(PASSWORD)){
          ComputerStateComponent.setState(ComputerState.LOGGED_IN);
          ComputerDialog.getInstance().ifPresent(computer -> {
            computer.addTabsForState(ComputerState.LOGGED_IN);
          });
          onLoginSuccess();
        } else {
          onWrongCredentials();
        }
      }
    });

    if(completed){
      onLoginSuccess();
    }

    Table form = new Table();
    form.add(usernameLabel).right().padBottom(10).padRight(10);
    form.add(usernameField).width(300).padBottom(10).row();
    form.add(passwordLabel).right().padRight(10);
    form.add(passwordField).width(300).row();
    form.add(loginButton).colspan(2).growX().center().padTop(20).row();
    form.add(loginFeedback).colspan(2).center().padTop(10).row();

    this.add(form).center();
  }

  @Override
  protected void updateState(ComputerStateComponent newStateComp) {
    ComputerState oldState = sharedState().state();
    ComputerState newState = newStateComp.state();
    if(oldState != newState && newState == ComputerState.LOGGED_IN){
      onLoginSuccess();
    }
  }

  private void onLoginSuccess(){
    usernameField.setDisabled(true);
    passwordField.setDisabled(true);
    loginButton.setDisabled(true);
    loginFeedback.setText(CORRECT_FEEDBACK);
    loginFeedback.setColor(CORRECT_COLOR);
  }

  private void onWrongCredentials(){
    loginFeedback.setText(WRONG_FEEDBACK);
  }
}
