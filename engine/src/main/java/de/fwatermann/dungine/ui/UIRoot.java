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
import de.fwatermann.dungine.ui.components.UIComponentClickable;
import de.fwatermann.dungine.ui.components.UIComponentHoverable;
import de.fwatermann.dungine.ui.components.UIComponentScrollable;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.window.GameWindow;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.joml.Intersectionf;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

public class UIRoot extends UIContainer<UIRoot> implements EventListener, Disposable {

  private UIElement<?> lastHovered = null;

  private Camera<?> uiCamera;
  private GameWindow window;
  private boolean initialized = false;

  public UIRoot(GameWindow window, int pixelWidth, int pixelHeight) {
    this.window = window;
    this.uiCamera = new CameraOrthographic(new CameraViewport(pixelWidth, pixelHeight, 0, 0));
  }

  private void init() {
    if(this.initialized) return;
    EventManager.getInstance().registerListener(this);
    this.initialized =  true;
  }

  public void render() {
    this.init();
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
    UIElement<?> element = this.getElementAtMouse(true, UIComponentClickable.class);
    if (element != null) {
      element.component(UIComponentClickable.class).ifPresent(c -> c.onClick().run(element));
    }
  }

  private void hover() {
    UIElement<?> element = this.getElementAtMouse(true, UIComponentHoverable.class);
    if (this.lastHovered != element) {
      if (this.lastHovered != null) {
        this.lastHovered
            .component(UIComponentHoverable.class)
            .ifPresent(c -> c.onLeave().run(this.lastHovered));
      }
      this.lastHovered = null;
    } else {
      return;
    }
    if (element != null) {
      element.component(UIComponentHoverable.class).ifPresent(c -> c.onEnter().run(element));
      this.lastHovered = element;
    }
  }

  private void scroll(int x, int y) {
    UIElement<?> element = this.getElementAtMouse(true, UIComponentScrollable.class);
    if (element != null) {
      element
          .component(UIComponentScrollable.class)
          .ifPresent(c -> c.onScroll().run(element, x, y));
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

  private UIElement<?> getElementAtMouse(
      boolean includeContainers, Class<? extends UIComponent<?>> clazzFilter) {
    Vector2i mousePos = Mouse.getMousePosition();
    mousePos.y = this.window.size().y - mousePos.y;

    Vector3f origin = this.uiCamera.unproject(mousePos.x, mousePos.y);
    Vector3f direction = this.uiCamera.raycast(mousePos.x, mousePos.y);

    List<UIElement<?>> elements = this.allChildElements(includeContainers, clazzFilter);
    float distance = Float.MAX_VALUE;
    UIElement<?> closest = null;
    for (UIElement<?> element : elements) {
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
  @SafeVarargs
  public final List<UIElement<?>> allChildElements(
      boolean includeContainers, Class<? extends UIComponent<?>>... componentFilter) {
    List<UIElement<?>> list = new ArrayList<>();
    this.allChildElements(this, includeContainers, list, componentFilter);
    return list;
  }

  @SafeVarargs
  private void allChildElements(
      UIContainer<?> container,
      boolean includeContainers,
      List<UIElement<?>> elements,
      Class<? extends UIComponent<?>>... componentFilter) {
    container
        .elements()
        .forEach(
            e -> {
              if (e instanceof UIContainer<?> c) {
                if (includeContainers && Arrays.stream(componentFilter).allMatch(c::hasComponent)) {
                  elements.add(e);
                }
                this.allChildElements(c, includeContainers, elements);
              } else {
                if (Arrays.stream(componentFilter).allMatch(e::hasComponent)) {
                  elements.add(e);
                }
              }
            });
  }

  @Override
  public void dispose() {
    EventManager.getInstance().unregisterListener(this);
  }
}
