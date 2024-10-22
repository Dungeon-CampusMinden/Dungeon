package de.fwatermann.dungine.ecs.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.ecs.components.LightComponent;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.graphics.Renderable;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.scene.SceneRenderer;
import de.fwatermann.dungine.graphics.scene.light.Light;
import de.fwatermann.dungine.graphics.scene.model.Model;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The `RenderableSystem` class is responsible for managing and rendering all entities with
 * `RenderableComponent` in the ECS. It extends the `System` class and provides methods to update
 * and render the entities.
 */
public class RenderableSystem extends System<RenderableSystem> {

  /** Logger for the `RenderableSystem` class. */
  private static final Logger LOGGER = LogManager.getLogger(RenderableSystem.class);

  /** The shader program used for rendering the scene. */
  private ShaderProgram sceneShader;

  /** The camera used for rendering. */
  private Camera<?> camera;

  /** The count of the latest rendered entities. */
  private int latestRenderCount = 0;

  /** The count of entities to be rendered. */
  private int renderCount = 0;

  /**
   * Constructs a new `RenderableSystem` with the specified camera.
   *
   * @param camera the camera used for rendering
   */
  public RenderableSystem(Camera<?> camera) {
    super(1, true, RenderableComponent.class);
    this.camera = camera;
  }

  /**
   * Updates the system by iterating over all entities and rendering them.
   *
   * @param ecs the ECS instance
   */
  @Override
  public void update(ECS ecs) {
    Set<Model> models = new HashSet<>();
    Set<Light<?>> lights = new HashSet<>();
    Set<Renderable<?>> renderables = new HashSet<>();
    ecs.forEachEntity(
        e -> {
          e.components(RenderableComponent.class)
              .forEach(
                  c -> {
                    c.renderable.transformation(e.position(), e.rotation(), e.size());

                    if (this.camera instanceof CameraPerspective pCam) {
                      if (!c.renderable.shouldRender(pCam.frustum())) return;
                    }
                    if (c.renderable instanceof Model model) {
                      models.add(model);
                    } else {
                      renderables.add(c.renderable);
                    }
                    this.renderCount++;
                  });
          e.components(LightComponent.class)
              .forEach(
                  c -> {
                    lights.add(c.light());
                  });
        });
    this.latestRenderCount = this.renderCount;
    this.renderCount = 0;

    renderables.stream().sorted((r1, r2) -> Integer.compare(r1.order(), r2.order())).forEach(r -> r.render(this.camera));

    if (this.sceneShader != null) {
      SceneRenderer.renderScene(this.camera, models, lights, this.sceneShader);
    } else {
      SceneRenderer.renderScene(this.camera, models, lights);
    }
  }

  /**
   * Gets the camera used for rendering.
   *
   * @return the camera used for rendering
   */
  public Camera<?> camera() {
    return this.camera;
  }

  /**
   * Sets the camera used for rendering.
   *
   * @param camera the new camera to be used for rendering
   * @return this `RenderableSystem` instance for method chaining
   */
  public RenderableSystem camera(Camera<?> camera) {
    this.camera = camera;
    return this;
  }

  /**
   * Gets the count of the latest rendered entities.
   *
   * @return the count of the latest rendered entities
   */
  public int latestRenderCount() {
    return this.latestRenderCount;
  }

  /**
   * Sets the shader program used for rendering the scene.
   *
   * @param sceneShader the shader program to be used for rendering the scene
   * @return this `RenderableSystem` instance for method chaining
   */
  public RenderableSystem sceneShader(ShaderProgram sceneShader) {
    this.sceneShader = sceneShader;
    return this;
  }

  /**
   * Gets the shader program used for rendering the scene.
   *
   * @return the shader program used for rendering the scene
   */
  public ShaderProgram sceneShader() {
    return this.sceneShader;
  }
}
