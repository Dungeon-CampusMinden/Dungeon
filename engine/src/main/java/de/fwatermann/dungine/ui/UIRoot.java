package de.fwatermann.dungine.ui;

import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.MouseButtonEvent;
import de.fwatermann.dungine.event.input.MouseScrollEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraOrthographic;
import de.fwatermann.dungine.graphics.camera.CameraViewport;
import de.fwatermann.dungine.input.Mouse;
import de.fwatermann.dungine.window.GameWindow;
import java.util.ArrayList;
import java.util.List;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

public class UIRoot extends UIContainer<UIRoot> implements EventListener {

  private UIElement<?> lastHovered = null;

  private Camera<?> uiCamera;
  private GameWindow window;

  public UIRoot(GameWindow window, int pixelWidth, int pixelHeight) {
    this.window = window;
    this.uiCamera = new CameraOrthographic(new CameraViewport(pixelWidth, pixelHeight, 0, 0));
    EventManager.getInstance().registerListener(this);
  }

  public void render() {
    this.uiCamera.update();

    boolean depthEnabled = GL33.glIsEnabled(GL33.GL_DEPTH_TEST);
    int depthFunc = GL33.glGetInteger(GL33.GL_DEPTH_FUNC);

    boolean blendEnabled = GL33.glIsEnabled(GL33.GL_BLEND);
    int blendFunc_src = GL33.glGetInteger(GL33.GL_BLEND_SRC);
    int blendFunc_dst = GL33.glGetInteger(GL33.GL_BLEND_DST);

    GL33.glEnable(GL33.GL_DEPTH_TEST);
    GL33.glDepthFunc(GL33.GL_LEQUAL);
    GL33.glClear(GL33.GL_DEPTH_BUFFER_BIT);

    GL33.glEnable(GL33.GL_BLEND);
    GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);

    this.hover();
    this.render(this.uiCamera);

    if (!depthEnabled) GL33.glDisable(GL33.GL_DEPTH_TEST);
    GL33.glDepthFunc(depthFunc);
    if (!blendEnabled) GL33.glDisable(GL33.GL_BLEND);
    GL33.glBlendFunc(blendFunc_src, blendFunc_dst);
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
    if (event.action == MouseButtonEvent.MouseButtonAction.PRESS) {
      this.click(event.button, event.action);
    }
  }

  @EventHandler
  public void onMouseScroll(MouseScrollEvent event) {
    this.scroll(event.x, event.y);
  }

  private void click(int button, MouseButtonEvent.MouseButtonAction action) {
    UIElement<?> element = this.getElementAtMouse(true, IUIClickable.class);
    if (element instanceof IUIClickable clickable) {
      clickable.click(button, action);
    }
  }

  private void hover() {
    UIElement<?> element = this.getElementAtMouse(true, IUIHoverable.class);
    if (this.lastHovered instanceof IUIHoverable hoverable && this.lastHovered != element) {
      hoverable.leave();
      this.lastHovered = null;
    } else if (this.lastHovered == element) return;
    if (element instanceof IUIHoverable hoverable) {
      hoverable.enter();
      this.lastHovered = element;
    }
  }

  private void scroll(int x, int y) {
    UIElement<?> element = this.getElementAtMouse(true, IUIScrollable.class);
    if (element instanceof IUIScrollable scrollable) {
      scrollable.scroll(x, y);
    }
  }

  private float isOver(UIElement<?> element, Vector3f origin, Vector3f direction) {
    Vector3f min = element.position();
    Vector3f max = element.position().add(element.size(), new Vector3f());
    if (Math.abs(max.z - min.z) == 0) {
      max.z = 0.001f;
      min.z = -0.001f;
    }
    Vector2f result = new Vector2f();
    boolean intersects = Intersectionf.intersectRayAab(origin, direction, min, max, result);
    if (intersects) {
      return result.x;
    }
    return -1.0f;
  }

  private UIElement<?> getElementAtMouse(boolean includeContainers, Class<?> clazzFilter) {
    Vector2i mousePos = Mouse.getMousePosition();
    mousePos.y = this.window.size().y - mousePos.y;

    Vector3f origin = this.uiCamera.unproject(mousePos.x, mousePos.y);
    Vector3f direction = this.uiCamera.raycast(mousePos.x, mousePos.y);

    List<UIElement<?>> elements = this.allChildElements(includeContainers);
    float distance = Float.MAX_VALUE;
    UIElement<?> closest = null;
    for (UIElement<?> element : elements) {
      if (clazzFilter != null && !clazzFilter.isInstance(element)) continue;
      float d = this.isOver(element, origin, direction);
      if (d < 0) continue;
      if (d < distance) {
        distance = d;
        closest = element;
      }
    }
    return closest;
  }

  /**
   * Returns all child elements of this container.
   *
   * @param includeContainers If true, containers will be included in the list.
   * @return List of all child elements.
   */
  public List<UIElement<?>> allChildElements(boolean includeContainers) {
    List<UIElement<?>> list = new ArrayList<>();
    this.allChildElements(this, includeContainers, list);
    return list;
  }

  private void allChildElements(
      UIContainer<?> container, boolean includeContainers, List<UIElement<?>> elements) {
    container
        .elements()
        .forEach(
            e -> {
              if (e instanceof UIContainer<?> c) {
                if (includeContainers) elements.add(e);
                this.allChildElements(c, includeContainers, elements);
              } else {
                elements.add(e);
              }
            });
  }
}
