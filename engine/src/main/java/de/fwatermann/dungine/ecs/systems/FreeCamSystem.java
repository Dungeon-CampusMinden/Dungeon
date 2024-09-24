package de.fwatermann.dungine.ecs.systems;

import de.fwatermann.dungine.ecs.ECS;
import de.fwatermann.dungine.ecs.System;
import de.fwatermann.dungine.graphics.camera.Camera;
import de.fwatermann.dungine.input.Keyboard;
import de.fwatermann.dungine.input.Mouse;
import de.fwatermann.dungine.state.GameState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class FreeCamSystem extends System<FreeCamSystem> {

  private static final Logger LOGGER = LogManager.getLogger(FreeCamSystem.class);

  private final Camera<?> camera;
  private final GameState state;

  private boolean catchMouse = false;
  private Vector2i lastMousePos = null;

  public FreeCamSystem(Camera<?> camera, boolean catchMouse, GameState state) {
    super(1, true);
    this.camera = camera;
    this.state = state;
    this.catchMouse = catchMouse;
  }

  @Override
  public void update(ECS ecs) {
    this.keyboard();
    this.mouse();
  }

  private void keyboard() {
    Vector3f movement = new Vector3f();
    Vector3f forward = this.camera.front().mul(1.0f, 0.0f, 1.0f, new Vector3f()).normalize();
    Vector3f right = this.camera.right().mul(1.0f, 0.0f, 1.0f, new Vector3f()).normalize();

    if (Keyboard.keyPressed(GLFW.GLFW_KEY_W)) {
      movement.add(forward);
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_S)) {
      movement.sub(forward);
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_A)) {
      movement.sub(right);
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_D)) {
      movement.add(right);
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_SPACE)) {
      movement.add(0.0f, 1.0f, 0.0f);
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
      movement.add(0.0f, -1.0f, 0.0f);
    }
    if(movement.x == 0 && movement.y == 0 && movement.z == 0) {
      return;
    }
    movement.normalize();
    movement.mul(this.state.lastFrameDeltaTime());
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_LEFT_CONTROL)) {
      movement.mul(2f);
    }
    this.camera.move(movement);
  }

  private void mouse() {

    if(!this.catchMouse && !Mouse.buttonPressed(0)) {
      this.lastMousePos = Mouse.getMousePosition();
      return;
    }

    Vector2i windowCenter = this.state.window().size().div(2, new Vector2i());
    if(this.lastMousePos == null) {
      Mouse.setMousePosition(windowCenter);
      this.lastMousePos = new Vector2i(windowCenter);
      return;
    }
    Vector2i current = Mouse.getMousePosition();
    Vector2i rel = current.sub(this.lastMousePos, new Vector2i());
    if(rel.x == 0 && rel.y == 0) {
      return;
    }

    this.camera.pitchDeg((float) -rel.y);
    this.camera.yawDeg((float) -rel.x);
    if(this.catchMouse) {
      Mouse.setMousePosition(windowCenter);
      this.lastMousePos = windowCenter;
    } else {
      this.lastMousePos = new Vector2i(current);
    }
  }

}
