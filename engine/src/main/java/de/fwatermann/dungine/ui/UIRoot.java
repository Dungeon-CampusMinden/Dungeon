package de.fwatermann.dungine.ui;

import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.MouseButtonEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraOrthographic;
import de.fwatermann.dungine.graphics.camera.CameraViewport;
import de.fwatermann.dungine.input.Mouse;
import de.fwatermann.dungine.window.GameWindow;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;

public class UIRoot extends UIContainer implements EventListener {

  private static Logger LOGGER = LogManager.getLogger(UIRoot.class);

  private Camera<?> uiCamera;
  private GameWindow window;

  public UIRoot(GameWindow window, int pixelWidth, int pixelHeight) {
    this.window = window;
    this.uiCamera = new CameraOrthographic(new CameraViewport(pixelWidth, pixelHeight, 0, 0));
    //this.uiCamera = new CameraPerspective(new CameraViewport(pixelWidth, pixelHeight, 0, 0));
    EventManager.getInstance().registerListener(this);
  }

  public void render() {
    this.uiCamera.update();
    GL33.glClear(GL33.GL_DEPTH_BUFFER_BIT);
    this.render(this.uiCamera);
  }

  public UIRoot camera(Camera<?> camera) {
    this.uiCamera = camera;
    return this;
  }

  public Camera<?> camera() {
    return this.uiCamera;
  }

  @EventHandler
  public void onResize(WindowResizeEvent event) {
    this.uiCamera.updateViewport(event.to.x, event.to.y, 0, 0);
    this.size().set(event.to.x, event.to.y, 0);
  }

  @EventHandler
  public void onMouseButton(MouseButtonEvent event) {
    if(event.button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
      if(event.action == MouseButtonEvent.MouseButtonAction.PRESS) {
        this.click();
      }
    }
  }

  private void click() {
    List<UIElement<?>> elements = this.allElements(false);

    Vector2i mousePos = Mouse.getMousePosition();
    mousePos.y = this.window.size().y - mousePos.y;

    Vector3f origin = this.uiCamera.unproject(mousePos.x, mousePos.y);
    Vector3f direction = this.uiCamera.raycast(mousePos.x, mousePos.y);

    UIElement<?> closest = null;
    float closestDistance = Float.MAX_VALUE;
    Vector2f result = new Vector2f();

    for(UIElement<?> element : elements) {
      Vector3f min = element.position();
      Vector3f max = element.position().add(element.size(), new Vector3f());

      if(Math.abs(max.z - min.z) == 0) {
        max.z = 0.001f;
        min.z = -0.001f;
      }

      boolean intersects = Intersectionf.intersectRayAab(origin, direction, min, max, result);
      if(!intersects) continue;

      if(result.x < closestDistance){
        closestDistance = result.x;
        closest = element;
        continue;
      }
      if(result.y < closestDistance){
        closestDistance = result.y;
        closest = element;
      }
    }

    LOGGER.debug("Clicked on {}", closest);
    //TODO: Add click event for UIElements (abstract or so)
  }

  //TODO: Add scroll handling / hover / ...

  private List<UIElement<?>> allElements(boolean includeContainers) {
    List<UIElement<?>> list = new ArrayList<>();
    if(includeContainers) list.add(this);
    this.allElements(this, list);
    return list;
  }

  private void allElements(UIContainer container, List<UIElement<?>> elements) {
    container.elements().forEach(e -> {
      if(e instanceof UIContainer c) {
        this.allElements(c, elements);
      } else {
        elements.add(e);
      }
    });
  }


}
