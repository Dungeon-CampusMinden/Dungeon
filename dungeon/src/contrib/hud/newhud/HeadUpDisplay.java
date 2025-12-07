package contrib.hud.newhud;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import core.Game;

public class HeadUpDisplay {

  private final Stage stage;
  private final HeadUpDisplaySystem headUpDisplaySystem;

  public HeadUpDisplay() {
    this.stage = new Stage(new ScreenViewport());
    this.headUpDisplaySystem = new HeadUpDisplaySystem();
    Game.add(headUpDisplaySystem);
  }

  public void addElement(HUDElement element) {
    element.init();
    stage.addActor((com.badlogic.gdx.scenes.scene2d.Actor) element);
    headUpDisplaySystem.register(element);
  }

  public Stage getStage() {
    return stage;
  }
}
