package dungine.util;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.graphics.BillboardMode;
import de.fwatermann.dungine.graphics.simple.Sprite;
import de.fwatermann.dungine.graphics.texture.animation.Animation;
import de.fwatermann.dungine.graphics.texture.animation.BatchAnimation;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.window.GameWindow;
import dungine.components.CameraComponent;
import dungine.components.PlayerComponent;
import dungine.components.VelocityComponent;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

/**
 * The `HeroFactory` is a factory class that creates a hero entity with a camera component and a
 * renderable component. The hero can be made controllable with the 'W', 'A', 'S', 'D' keys.
 */
public class HeroFactory {

  /**
   * Create a new hero entity.
   *
   * @return The hero entity.
   */
  public static Entity create() {
    Entity entity = new Entity();
    Animation animation =
        new BatchAnimation(Resource.load("/animations/hero.png"), 4, BatchAnimation.Direction.DOWN)
            .frameDuration(200);
    animation.frameDuration(200);
    Sprite sprite = new Sprite(animation, BillboardMode.SPHERICAL);
    entity.addComponent(new RenderableComponent(sprite));
    entity.addComponent(new CameraComponent());
    entity.position().set(0.0f, 0.5f, 0.0f);
    entity.size(new Vector3f(0.73f, 1.0f, 0.0f));
    return entity;
  }

  /**
   * Make the hero entity controllable with the 'W', 'A', 'S', 'D' keys.
   *
   * @param window The GameWindow instance.
   * @param entity The hero entity.
   */
  public static void makeControlled(GameWindow window, Entity entity) {
    float speedMultiplier = 7.5f * 4;
    PlayerComponent playerComponent = new PlayerComponent();
    playerComponent.registerCallback(
        GLFW.GLFW_KEY_W,
        (e) -> {
          entity
              .component(VelocityComponent.class)
              .ifPresentOrElse(
                  (vc) -> {
                    vc.force.add(0, 0, -speedMultiplier * window.renderDeltaTime());
                  },
                  () -> {
                    entity.position().add(0, 0, -speedMultiplier * window.renderDeltaTime());
                  });
        });
    playerComponent.registerCallback(
        GLFW.GLFW_KEY_S,
        (e) -> {
          entity
              .component(VelocityComponent.class)
              .ifPresentOrElse(
                  (vc) -> {
                    vc.force.add(0, 0, speedMultiplier * window.renderDeltaTime());
                  },
                  () -> {
                    entity.position().add(0, 0, speedMultiplier * window.renderDeltaTime());
                  });
        });
    playerComponent.registerCallback(
        GLFW.GLFW_KEY_A,
        (e) -> {
          entity
              .component(VelocityComponent.class)
              .ifPresentOrElse(
                  (vc) -> {
                    vc.force.add(-speedMultiplier * window.renderDeltaTime(), 0, 0);
                  },
                  () -> {
                    entity.position().add(-speedMultiplier * window.renderDeltaTime(), 0, 0);
                  });
        });
    playerComponent.registerCallback(
        GLFW.GLFW_KEY_D,
        (e) -> {
          entity
              .component(VelocityComponent.class)
              .ifPresentOrElse(
                  (vc) -> {
                    vc.force.add(speedMultiplier * window.renderDeltaTime(), 0, 0);
                  },
                  () -> {
                    entity.position().add(speedMultiplier * window.renderDeltaTime(), 0, 0);
                  });
        });
    entity.addComponent(playerComponent);
    entity.addComponent(new VelocityComponent());
  }
}
