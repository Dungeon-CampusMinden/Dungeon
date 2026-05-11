package client.hud;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import contrib.hud.UIUtils;
import core.Game;

public class StageConfig {

  /* text above velocity slider */
  private static Label velocityLabel;

  /* Method for layout config of stage
  * makes stage clickable
  * creates a table as root, table can define position of her elements
  * add concrete labels and slider to stage
  * */

  public static void setupStage(){

    Game.stage().ifPresent(
      stage->{

        Gdx.input.setInputProcessor(stage);
        Table rootTable = new Table();

        // table with size of parent(root), table has all the screen size
        rootTable.setFillParent(true);


        velocityLabel = new Label("velocity: 1x",UIUtils.defaultSkin());
        Slider slider = new Slider(1.0f,5f,0.5f,false,UIUtils.defaultSkin());

        Table topRight = new Table();
        topRight.add(velocityLabel).row();
        topRight.add(slider).width(150);

        rootTable.add(topRight).expand().top().right();

        stage.addActor(rootTable);


      }
    );
  }
}
