package modules.computer.content;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import core.utils.FontHelper;
import core.utils.components.draw.TextureGenerator;
import util.Scene2dElementFactory;

public class LoginMask extends Table {

  public LoginMask(){
//    this.setFillParent(true);
    this.top();
    createLayout();
  }

  private void createLayout(){
    Drawable company = new TextureRegionDrawable(TextureGenerator.generateColorTexture(100, 100, new Color(0, 0, 0.7f, 1)));
    Image companyLogo = new Image(company);
    this.add(companyLogo).width(200).height(200).center().padBottom(20).row();

    Label label = Scene2dElementFactory.createLabel("Company XYZ", 64, Color.WHITE);
    this.add(label).center().padBottom(10).row();
    Label flavor = Scene2dElementFactory.createLabel("At the frontlines of science since 1984", 24, Color.GRAY);
    this.add(flavor).center().padBottom(20).row();

    Label usernameLabel = Scene2dElementFactory.createLabel("Username:", 32, Color.WHITE);
    Label passwordLabel = Scene2dElementFactory.createLabel("Password:", 32, Color.WHITE);
    TextField usernameField = Scene2dElementFactory.createTextField("");
    TextField passwordField = Scene2dElementFactory.createTextField("");

    // left label, right field
    Table form = new Table();
    form.add(usernameLabel).right().padRight(10);
    form.add(usernameField).width(300).padBottom(10).row();
    form.add(passwordLabel).right().padRight(10);
    form.add(passwordField).width(300).row();

    this.add(form).center();
  }
}
