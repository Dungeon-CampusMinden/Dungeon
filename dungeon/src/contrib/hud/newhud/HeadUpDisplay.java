package contrib.hud.newhud;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import core.Game;

/**
 * The HeadUpDisplay maintains a {@link Stage} for rendering all HUD elements and passes them to the
 * {@link HeadUpDisplaySystem}.
 */
public class HeadUpDisplay {

  private final Stage stage;
  private final HeadUpDisplaySystem headUpDisplaySystem;

  /** Creates a new HeadUpDisplay and initializes its internal stage and system. */
  public HeadUpDisplay() {
    this.stage = new Stage(new ScreenViewport());
    this.headUpDisplaySystem = new HeadUpDisplaySystem();
    Game.add(headUpDisplaySystem);
  }

  /**
   * Adds the given HUD element to the display and registers it with the HUD system.
   *
   * @param element The HUD element to add.
   */
  public void addElement(HUDElement element) {
    stage.addActor((com.badlogic.gdx.scenes.scene2d.Actor) element);
    headUpDisplaySystem.register(element);
  }

  /**
   * Returns the stage used to render the HUD.
   *
   * @return the HUD stage.
   */
  public Stage getStage() {
    return stage;
  }
}
