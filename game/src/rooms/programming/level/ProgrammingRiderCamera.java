package rooms.programming.level;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import engine.Entity;
import engine.Game;
import engine.components.CameraComponent;
import engine.systems.CameraSystem;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Local mounted camera; it never disables another player's controls or adds observation effects.
 */
final class ProgrammingRiderCamera extends Group {
  private final int golemId;
  private final Map<Entity, CameraComponent> previous = new LinkedHashMap<>();
  private Entity followed;
  private float zoom;

  ProgrammingRiderCamera(int golemId) {
    this.golemId = golemId;
  }

  @Override
  public void act(float delta) {
    super.act(delta);
    if (followed != null || getStage() == null) return;
    Game.findEntityById(golemId)
        .ifPresent(
            golem -> {
              Game.levelEntities()
                  .filter(entity -> entity.isPresent(CameraComponent.class))
                  .toList()
                  .forEach(
                      entity -> {
                        previous.put(entity, entity.fetch(CameraComponent.class).orElseThrow());
                        entity.remove(CameraComponent.class);
                      });
              followed = golem;
              zoom = CameraSystem.camera().zoom;
              CameraSystem.camera().zoom = zoom * 1.2f;
              golem.add(new CameraComponent());
            });
  }

  @Override
  protected void setStage(Stage stage) {
    if (stage == null && followed != null) {
      followed.remove(CameraComponent.class);
      previous.forEach(Entity::add);
      previous.clear();
      CameraSystem.camera().zoom = zoom;
      followed = null;
    }
    super.setStage(stage);
  }
}
