package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import core.utils.components.draw.TextureGenerator;
import modules.computer.ComputerDialog;
import modules.computer.ComputerStateLocal;
import util.Scene2dElementFactory;

public class LoginMask extends ComputerTab {

  public LoginMask(){
    super("login", "Login", false);
  }

  protected void createActors(){
    Drawable company = new TextureRegionDrawable(TextureGenerator.generateColorTexture(100, 100, new Color(0, 0, 0.7f, 1)));
    Image companyLogo = new Image(company);
    this.add(companyLogo).width(200).height(200).center().padBottom(20).row();

    Label label = Scene2dElementFactory.createLabel("Company XYZ", 64, Color.WHITE);
    this.add(label).center().padBottom(10).row();
    Label flavor = Scene2dElementFactory.createLabel("At the frontlines of science since 1984", 24, Color.GRAY);
    this.add(flavor).center().padBottom(20).row();

    Label usernameLabel = Scene2dElementFactory.createLabel("Username:", 32, Color.WHITE);
    Label passwordLabel = Scene2dElementFactory.createLabel("Password:", 32, Color.WHITE);

    TextField usernameField = Scene2dElementFactory.createTextField(ComputerStateLocal.Instance.username());
    Scene2dElementFactory.addTextFieldChangeListener(usernameField, (text) -> {
      ComputerStateLocal.Instance
          .username(usernameField.getText());
    });

    TextField passwordField = Scene2dElementFactory.createTextField(ComputerStateLocal.Instance.password());
    passwordField.setPasswordMode(true);
    passwordField.setPasswordCharacter('*');
    Scene2dElementFactory.addTextFieldChangeListener(passwordField, (text) -> {
      ComputerStateLocal.Instance
          .password(passwordField.getText());
    });

    Button button = Scene2dElementFactory.createButton("Login", "clean-green");
    button.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        String username = ComputerStateLocal.Instance.username();
        String password = ComputerStateLocal.Instance.password();
        System.out.println("Attempting login with username: " + username + " and password: " + password);

        if(username.equals("test") && password.equals("1234")){
          ComputerDialog.getInstance().ifPresent(computer -> {
            computer.addTab(new TestMask("test-key", "E-Mail Inbox (5)", false, Color.RED));
          });
        }

//        wrapper.addAction(Actions.sequence(
//          Actions.scaleTo(0.95f, 0.95f, 0.05f),
//          Actions.scaleTo(1f, 1f, 0.05f)
//        ));
      }
    });

    Table form = new Table();
    form.add(usernameLabel).right().padBottom(10).padRight(10);
    form.add(usernameField).width(300).padBottom(10).row();
    form.add(passwordLabel).right().padRight(10);
    form.add(passwordField).width(300).row();
    form.add(button).colspan(2).growX().center().padTop(20);

    this.add(form).center();
  }
}
