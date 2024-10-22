package dungine.state;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.LightComponent;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.FreeCamSystem;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.input.MouseButtonEvent;
import de.fwatermann.dungine.graphics.scene.light.AmbientLight;
import de.fwatermann.dungine.graphics.scene.light.PointLight;
import de.fwatermann.dungine.graphics.scene.model.ModelLoader;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.components.UIComponentClickable;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.ui.layout.Position;
import de.fwatermann.dungine.ui.layout.Unit;
import de.fwatermann.dungine.window.GameWindow;
import dungine.util.DemoUI;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class StateSolarSystem extends GameState {

  public StateSolarSystem(GameWindow window) {
    super(window);
  }

  private UIText fpsText;
  private UIText copyrightText;
  private UIText speedText;

  private double speed = 1.0f;

  private Entity earth;
  private Entity sun;
  private Entity moon;

  private double time = 0;

  @Override
  public void init() {
    this.fpsText = new UIText(Font.defaultMonoFont(), "FPS: 0", 12, TextAlignment.LEFT);
    this.copyrightText =
        new UIText(
            Font.defaultMonoFont(),
            "Textures from https://solarsystemscope.com",
            12,
            TextAlignment.RIGHT);
    this.copyrightText
        .layout()
        .position(Position.FIXED)
        .top(Unit.px(10))
        .right(Unit.px(10))
        .width(Unit.vW(100));
    this.speedText = new UIText(Font.defaultMonoFont(), "Speed: 1.00", 12, TextAlignment.LEFT);
    this.speedText.layout().position(Position.FIXED).top(Unit.px(32)).left(Unit.px(10)).width(Unit.vW(100));
    this.ui.add(this.copyrightText);
    this.ui.add(this.speedText);
    DemoUI.init(
        this.window,
        this.ui,
        this.fpsText,
        "In dieser Szene wird der ModelLoader getestet. Die Kamera kann mit 'W', 'A', 'S', 'D', 'SHIFT' und ' SPACE' durch den Raum bewegt werden. Durch Klicken und ziehen kann die Kamera geschwenkt werden.");

    this.camera.position(0, 5, 5);
    this.camera.lookAt(0, 0, 0);

    this.addSystem(new RenderableSystem(this.camera));
    this.addSystem(new FreeCamSystem(this.camera, false, this));

    this.earth = new Entity();
    this.earth.position().set(5, 0, 0);
    this.earth.rotation().rotateX((float) Math.toRadians(23.5));
    this.earth.addComponent(
        new RenderableComponent(ModelLoader.loadModel(Resource.load("/models/earth.glb"))));

    this.sun = new Entity();
    this.sun.size(new Vector3f(5));
    this.sun.addComponent(
        new RenderableComponent(ModelLoader.loadModel(Resource.load("/models/sun.glb"))));
    this.sun.addComponent(
        new LightComponent(
            new PointLight(
                new Vector3f(0), new Vector3f(1.0f, 1.0f, 0.9f), 1.0f, 0.1f, 0.1f, 0.1f)));
    this.sun.addComponent(new LightComponent(new AmbientLight(new Vector3f(1.0f), 0.1f)));

    this.moon = new Entity();
    this.moon.position().set(8, 0, 0);
    this.moon.size(new Vector3f(0.33f));
    this.moon.addComponent(
        new RenderableComponent(ModelLoader.loadModel(Resource.load("/models/moon.glb"))));

    this.addEntity(this.earth);
    this.addEntity(this.sun);
    this.addEntity(this.moon);

    this.copyrightText.attachComponent(
        new UIComponentClickable(
            (element, button, action) -> {
              if (action == MouseButtonEvent.MouseButtonAction.PRESS
                  && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (Desktop.isDesktopSupported()) {
                  try {
                    Desktop.getDesktop().browse(URI.create("https://solarsystemscope.com/textures"));
                  } catch (IOException ignored) {
                  }
                }
              }
            }));
  }

  @Override
  public void updateState(float deltaTime) {
    this.time += deltaTime * this.speed;

    float earthX = (float) Math.sin(this.time * 0.2) * 20;
    float earthZ = (float) Math.cos(this.time * 0.2) * 20;
    this.earth.position().set(earthX, 0, earthZ);

    this.earth.rotation().rotateY((float) Math.toRadians(deltaTime * 1000));

    float moonX = (float) Math.sin(this.time * 2.5) * 3;
    float moonZ = (float) Math.cos(this.time * 2.5) * 3;
    this.moon.position().set(earthX + moonX, 0, earthZ + moonZ);
  }

  @Override
  public void renderState(float deltaTime) {
    this.fpsText.text("FPS: " + this.window.frameCounter().currentFPS());
    this.speedText.text(String.format("Speed: %.2f", this.speed));
  }

  @EventHandler
  public void onKeyboard(KeyboardEvent event) {
    if(event.action != KeyboardEvent.KeyAction.RELEASE) {
      if(event.key == GLFW.GLFW_KEY_UP) {
        this.speed *= 1.1f;
      } else if(event.key == GLFW.GLFW_KEY_DOWN) {
        this.speed *= 0.9f;
      }
    }
  }

  @Override
  public boolean loaded() {
    return true;
  }
}
