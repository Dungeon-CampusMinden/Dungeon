package modules.computer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import contrib.hud.UIUtils;
import core.Game;
import core.systems.CameraSystem;
import core.utils.FontHelper;
import core.utils.components.draw.TextureGenerator;

public class ComputerDialog extends Group {

  private static final ShapeRenderer SHAPE_RENDERER = new ShapeRenderer();

  private ComputerStateComponent state;
  private Skin skin;

  public ComputerDialog(ComputerStateComponent state) {
    this.state = state;
    this.skin = UIUtils.defaultSkin();
    this.setSize(Game.windowWidth(), Game.windowHeight());
    this.setDebug(true, true);
    createActors();
  }

  public void updateState(ComputerStateComponent newState) {
    this.state = newState;
    // Additional logic to update the dialog based on the new state
  }


  private void createActors(){
    Drawable bg = new TextureRegionDrawable(TextureGenerator.generateColorTexture(100, 100, new Color(1, 0, 0, 0.2f)));

    Table container = new Table(skin);
    container.setFillParent(true);
    container.setBackground(bg);
    this.addActor(container);

    Label.LabelStyle labelStyle = new Label.LabelStyle();
    labelStyle.font = FontHelper.getFont(FontHelper.DEFAULT_FONT_PATH, 64, Color.WHITE, 4, Color.BLACK);
    labelStyle.fontColor = Color.WHITE;
    Label label = new Label("Hello this is a\nvery long text", labelStyle);


    // grid of size 3 rows x 2 columns, put the label in the middle left cell. No fixed sizes, use the available space
    // spread equally among all cells

    container.defaults().expand().uniform().space(50);
//    container.defaults().expand().fill();

    container.add();
    container.add();
    container.add().row();

    container.add();
    container.add(label).center().pad(20);
    container.add().row();

    container.add();
    container.add();
    container.add();
  }

  @Override
  public void draw(Batch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);
  }
}
