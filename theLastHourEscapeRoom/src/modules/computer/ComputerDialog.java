package modules.computer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.hud.UIUtils;
import core.utils.FontHelper;
import core.utils.components.draw.TextureGenerator;

public class ComputerDialog extends Group {

  private ComputerStateComponent state;
  private Skin skin;

  public ComputerDialog(ComputerStateComponent state) {
    this.state = state;
    this.skin = UIUtils.defaultSkin();
    createActors();
  }

  public void updateState(ComputerStateComponent newState) {
    this.state = newState;
    // Additional logic to update the dialog based on the new state
  }


  private void createActors(){
    Table container = new Table(skin);
    container.setFillParent(true);
    container.background(new TextureRegionDrawable(TextureGenerator.generateColorTexture(100, 100, Color.RED)));
    this.addActor(container);

    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, 64, Color.WHITE, 4, Color.BLACK);
    labelStyle.fontColor = Color.WHITE;
    Label label = new Label("Hello this is a very long text", labelStyle);

    container.add(label);


//    TextField tf = new TextField("", skin);
//    tf.getStyle().

    // grid of size 3 rows x 2 columns, put the label in the middle left cell. No fixed sizes, use the available space
    // spread equally among all cells

    container.row();
    container.add().expand();
    container.add(label).expand();
    container.add().expand();
    container.row();
    container.add().expand();
    container.add().expand();
    container.add().expand();

  }

}
