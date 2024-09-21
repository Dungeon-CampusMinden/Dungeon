package de.fwatermann.dungine.ui;

import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.MouseButtonEvent;
import de.fwatermann.dungine.event.input.MouseScrollEvent;
import de.fwatermann.dungine.event.window.FrameBufferResizeEvent;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.graphics.camera.CameraOrthographic;
import de.fwatermann.dungine.graphics.camera.CameraViewport;
import de.fwatermann.dungine.input.Mouse;
import de.fwatermann.dungine.ui.components.UIComponentClickable;
import de.fwatermann.dungine.ui.components.UIComponentHoverable;
import de.fwatermann.dungine.ui.components.UIComponentScrollable;
import de.fwatermann.dungine.ui.layout.UILayouter;
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
  private final GameWindow window;
  private boolean initialized = false;

  private Vector2i lastMousePos = new Vector2i(0, 0);

  public UIRoot(GameWindow window, int pixelWidth, int pixelHeight) {
    this.window = window;
    this.uiCamera = new CameraOrthographic(new CameraViewport(pixelWidth, pixelHeight, 0, 0));
    this.size.set(pixelWidth, pixelHeight, 0);
  }

  private void init() {
    if (this.initialized) return;
    EventManager.getInstance().registerListener(this);
    this.initialized = true;
    UILayouter.layout(this, this.window.size(), true);
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
    List<UIElement<?>> elements = this.allChildElements(true);
    elements.sort((a, b) -> Float.compare(b.position.z, a.position.z));
    elements.forEach(e -> e.render(this.camera()));

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
  public void onResize(FrameBufferResizeEvent event) {
    this.uiCamera.updateViewport(event.width(), event.height(), 0, 0);
    this.size().set(event.width(), event.height(), 0);
    UILayouter.layout(this, new Vector2i(event.width(), event.height()), true);
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
    UIElement<?> element = this.getElementAtMouse(true);
    while(element != null && !element.hasComponent(UIComponentClickable.class)) {
      element = element.parent;
    }
    if(element != null) {
      UIElement<?> finalElement = element;
      element.component(UIComponentClickable.class).ifPresent(c -> c.onClick().run(finalElement, button, action));
    }
  }

  private boolean isAncestor(UIElement<?> ancestor, UIElement<?> element) {
    while (element != null) {
      if (element == ancestor) {
        return true;
      }
      element = element.parent;
    }
    return false;
  }

  private void hover() {
    if(this.lastMousePos.equals(Mouse.getMousePosition())) {
      return;
    }
    this.lastMousePos.set(Mouse.getMousePosition());
    UIElement<?> element = this.getElementAtMouse(true);
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
    UIElement<?> element = this.getElementAtMouse(true);
    if (element != null) {
      element
          .component(UIComponentScrollable.class)
          .ifPresent(c -> c.onScroll().run(element, x, y));
    }
  }

  private float isOver(UIElement<?> element, Vector3f origin, Vector3f direction) {
    Vector3f min = new Vector3f(element.absolutePosition());
    Vector3f max = element.absolutePosition().add(element.size(), new Vector3f());
    if (Math.abs(max.z - min.z) == 0) {
      Vector3f diagonal = max.sub(min, new Vector3f());
      element.rotation.transform(diagonal);
      Vector3f edge = min.sub(max.x, 0.0f, max.z, new Vector3f());
      element.rotation.transform(edge);
      Vector3f normal = diagonal.cross(edge, new Vector3f()).normalize();
      if(normal.dot(direction) == 0.0f) {
        return -1.0f;
      }
      float d = Intersectionf.intersectRayPlane(origin, direction, element.position(), normal, 0.0f);
      if(d < 0) return -1.0f;

      //Check if the intersection point is inside the rectangle
      Vector3f intersection = origin.add(direction.mul(d, new Vector3f()), new Vector3f());
      Vector3f local = intersection.sub(element.absolutePosition(), new Vector3f());
      element.rotation.invert().transform(local);
      if(local.x >= 0 && local.x <= diagonal.x && local.y >= 0 && local.y <= diagonal.y && local.z >= 0 && local.z <= diagonal.z) {
        return d;
      }
      return -1.0f;
    }
    Vector2f result = new Vector2f();
    boolean intersects = Intersectionf.intersectRayAab(origin, direction, min, max, result);
    if (intersects) {
      return result.x;
    }
    return -1.0f;
  }

  private UIElement<?> getElementAtMouse(boolean includeContainers) {
    Vector2i mousePos = Mouse.getMousePosition();
    mousePos.y = this.window.size().y - mousePos.y;

    Vector3f origin = this.uiCamera.unproject(mousePos.x, mousePos.y);
    Vector3f direction = this.uiCamera.raycast(mousePos.x, mousePos.y);

    List<UIElement<?>> elements = this.allChildElements(includeContainers);
    float distance = Float.MAX_VALUE;
    UIElement<?> closest = null;
    for (UIElement<?> element : elements) {
      float d = this.isOver(element, origin, direction);
      if (d < 0) continue;
      if (d < distance || (d == distance && this.isAncestor(closest, element))) {
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
                if (includeContainers && (componentFilter.length == 0 || Arrays.stream(componentFilter).allMatch(c::hasComponent))) {
                  elements.add(e);
                }
                this.allChildElements(c, includeContainers, elements, componentFilter);
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
