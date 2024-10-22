package dungine.state;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.LightComponent;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.FreeCamSystem;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.graphics.scene.light.AmbientLight;
import de.fwatermann.dungine.graphics.scene.light.PointLight;
import de.fwatermann.dungine.graphics.simple.Cuboid;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.window.GameWindow;
import dungine.util.DemoUI;
import org.joml.Vector3f;

public class StateLighting extends GameState {

  private UIText fpsText;

  Entity redEntity, blueEntity, greenEntity, yellowEntity;

  public StateLighting(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {
    this.fpsText = new UIText(Font.defaultMonoFont(), "FPS: 0", 12, TextAlignment.LEFT);
    DemoUI.init(this.window, this.ui, this.fpsText, "In dieser Szene wird die Beleuchtung getestet. Die Kamera kann mit 'W', 'A', 'S', 'D', 'SHIFT' und ' SPACE' durch den Raum bewegt werden. Durch Klicken und ziehen kann die Kamera geschwenkt werden.");

    this.camera.position(0, 5, 5);
    this.camera.lookAt(0, 0, 0);

    this.addSystem(new RenderableSystem(this.camera));
    this.addSystem(new FreeCamSystem(this.camera, false, this));

    Entity floorEntity = new Entity();
    floorEntity.size(new Vector3f(20, 1, 20));
    floorEntity.addComponent(new RenderableComponent(new Cuboid(0xFFFFFFFF)));

    this.blueEntity = new Entity();
    this.blueEntity.position().set(0, 2, 5);
    this.blueEntity.size(new Vector3f(0.1f, 0.1f, 0.1f));
    this.blueEntity.addComponent(new RenderableComponent(new Cuboid(0x0000FFFF)));
    this.blueEntity.addComponent(new LightComponent(new PointLight(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), 2f, 0.8f, 0.9f, 0.1f)));

    this.redEntity = new Entity();
    this.redEntity.position().set(0, 2, -5);
    this.redEntity.size(new Vector3f(0.1f, 0.1f, 0.1f));
    this.redEntity.addComponent(new RenderableComponent(new Cuboid(0xFF0000FF)));
    this.redEntity.addComponent(new LightComponent(new PointLight(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), 2f, 0.8f, 0.9f, 0.1f)));

    this.greenEntity = new Entity();
    this.greenEntity.position().set(-5, 2, 0);
    this.greenEntity.size(new Vector3f(0.1f, 0.1f, 0.1f));
    this.greenEntity.addComponent(new RenderableComponent(new Cuboid(0x00FF00FF)));
    this.greenEntity.addComponent(new LightComponent(new PointLight(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), 2f, 0.8f, 0.9f, 0.1f)));

    this.yellowEntity = new Entity();
    this.yellowEntity.position().set(5, 2, 0);
    this.yellowEntity.size(new Vector3f(0.1f, 0.1f, 0.1f));
    this.yellowEntity.addComponent(new RenderableComponent(new Cuboid(0xFFFF00FF)));
    this.yellowEntity.addComponent(new LightComponent(new PointLight(new Vector3f(0, 0, 0), new Vector3f(1, 1, 0), 2f, 0.8f, 0.9f, 0.1f)));

    AmbientLight ambientLight = new AmbientLight(new Vector3f(0.8f, 0.8f, 1.0f), 0.15f);
    Entity lightEntity = new Entity();
    lightEntity.addComponent(new LightComponent(ambientLight));

    this.addEntity(floorEntity);
    this.addEntity(this.blueEntity);
    this.addEntity(this.redEntity);
    this.addEntity(this.greenEntity);
    this.addEntity(this.yellowEntity);

    this.addEntity(lightEntity);

    this.grid(true);

  }

  @Override
  public void updateState(float deltaTime) {
    this.blueEntity.position().z = (float) Math.sin(System.currentTimeMillis() / 2000.0) * 5;
    this.redEntity.position().z = (float) Math.sin(System.currentTimeMillis() / 2000.0) * -5;
    this.greenEntity.position().x = (float) Math.sin(System.currentTimeMillis() / 2000.0) * 5;
    this.yellowEntity.position().x = (float) Math.sin(System.currentTimeMillis() / 2000.0) * -5;
    System.out.println(this.redEntity.position().z);
  }

  @Override
  public void renderState(float deltaTime) {
    this.fpsText.text("FPS: " + this.window.frameCounter().currentFPS());
  }

  @Override
  public boolean loaded() {
    return true;
  }

}
