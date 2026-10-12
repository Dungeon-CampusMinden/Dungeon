package rooms.soulweaver.level;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import engine.Game;

/**
 * Local mounted camera; it never disables another player's controls or adds observation effects.
 */
final class SoulweaverRiderCamera extends Group {
  private final int golemId;
  private final SoulweaverCamera camera = new SoulweaverCamera();

  SoulweaverRiderCamera(int golemId) {
    this.golemId = golemId;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (camera.target().isPresent() || getStage() == null) return;
    Game.findEntityById(golemId).ifPresent(golem -> camera.follow(golem, Game.levelEntities()));
  }

  @Override
  protected void setStage(Stage stage) {
    if (stage == null) camera.restore();
    super.setStage(stage);
  }
}
