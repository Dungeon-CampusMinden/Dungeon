import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.MouseMoveEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.mesh.IndexedMesh;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.input.Keyboard;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.state.LoadStepper;
import de.fwatermann.dungine.utils.CoordinateAxis;
import de.fwatermann.dungine.window.GameWindow;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.text.NumberFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;

public class TestState extends GameState implements EventListener {

  private final static Logger LOGGER = LogManager.getLogger(TestState.class);

  protected TestState(GameWindow window) {
    super(window);
  }

  private IndexedMesh mesh;
  private ShaderProgram shaderProgram;
  private CameraPerspective camera;
  private CoordinateAxis axis;

  private boolean done = false;

  @Override
  public void init() {
    LOGGER.info("Loading TriangleState...");

    LoadStepper stepper = new LoadStepper(this.window);

    stepper
        .step(
            "vertexBuffer",
            () -> { // 0
              FloatBuffer vertices = BufferUtils.createFloatBuffer(24);
              vertices.put(
                  new float[] {
                    -0.5f, -0.5f, -0.5f,
                    +0.5f, -0.5f, -0.5f,
                    +0.5f, +0.5f, -0.5f,
                    -0.5f, +0.5f, -0.5f,
                    -0.5f, -0.5f, +0.5f,
                    +0.5f, -0.5f, +0.5f,
                    +0.5f, +0.5f, +0.5f,
                    -0.5f, +0.5f, +0.5f
                  });
              vertices.flip();
              LOGGER.debug("Loaded vertices");
              return vertices;
            })
        .step(
            "indexBuffer",
            () -> { // 1
              IntBuffer indices = BufferUtils.createIntBuffer(36);
              indices.put(
                  new int[] {
                    0, 1, 2, 2, 3, 0, // Front
                    1, 5, 6, 6, 2, 1, // Right
                    4, 0, 3, 3, 7, 4, // Left
                    3, 2, 6, 6, 7, 3, // Top
                    4, 5, 1, 1, 0, 4, // Bottom
                    7, 6, 5, 5, 4, 7  // Back
                  });
              indices.flip();
              LOGGER.debug("Loaded indices");
              return indices;
            })
        .step(
            "mesh",
            true,
            (results) -> { // 2
              FloatBuffer vertices = results.result("vertexBuffer");
              IntBuffer indices = results.result("indexBuffer");
              LOGGER.debug("Created mesh");
              return new IndexedMesh(
                  vertices,
                  indices,
                  GLUsageHint.DRAW_STATIC,
                  new VertexAttribute(VertexAttribute.Usage.POSITION, 3, GL33.GL_FLOAT));
            })
        .step(
            "vertexShader",
            true,
            () -> { // 3
              LOGGER.debug("Loading vertex shader");
              try {
                return Shader.loadShader(
                    Resource.load("/shaders/cube.vsh"), Shader.ShaderType.VERTEX_SHADER);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .step(
            "fragmentShader",
            true,
            () -> { // 4
              LOGGER.debug("Loading fragment shader");
              try {
                return Shader.loadShader(
                    Resource.load("/shaders/cube.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .step(
            "shaderProgram",
            true,
            (results) -> { // 5
              LOGGER.debug("Creating shader program");
              return new ShaderProgram(
                  results.result("vertexShader"), results.result("fragmentShader"));
            })
        .step(
            "coordinateAxis",
            true,
            () -> {
              this.axis = new CoordinateAxis(new Vector3f(0, 0, 0), 5.0f, true);
            })
        .step(
            "camera",
            false,
            () -> {
              this.camera = new CameraPerspective();
              this.camera.position(0, 0, -5.0f);
              LOGGER.debug("Created camera");
            })
        .done(
            true,
            (results) -> {
              this.shaderProgram = results.result("shaderProgram");
              this.mesh = results.result("mesh");
              LOGGER.info("TriangleState loaded!");
              EventManager.getInstance().registerListener(this);
              this.done = true;
            });
    stepper.start();
  }

  @Override
  public void render(float deltaTime) {
    this.camera.update();

    if (Keyboard.keyPressed(GLFW.GLFW_KEY_I)) {
      GL33.glPolygonMode(GL33.GL_FRONT_AND_BACK, GL33.GL_LINE);
    } else {
      GL33.glPolygonMode(GL33.GL_FRONT_AND_BACK, GL33.GL_FILL);
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_K)) {
      GL33.glCullFace(GL33.GL_FRONT);
    } else {
      GL33.glCullFace(GL33.GL_BACK);
    }

    if(Keyboard.keyPressed(GLFW.GLFW_KEY_W)) {
      this.camera.move(this.camera.front().mul(1.0f, 0.0f, 1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if(Keyboard.keyPressed(GLFW.GLFW_KEY_S)) {
      this.camera.move(this.camera.front().mul(-1.0f, 0.0f, -1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if(Keyboard.keyPressed(GLFW.GLFW_KEY_A)) {
      this.camera.move(this.camera.right().mul(-1.0f, 0.0f, -1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if(Keyboard.keyPressed(GLFW.GLFW_KEY_D)) {
      this.camera.move(this.camera.right().mul(1.0f, 0.0f, 1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if(Keyboard.keyPressed(GLFW.GLFW_KEY_SPACE)) {
      this.camera.move(0.0f, deltaTime, 0.0f);
    }
    if(Keyboard.keyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
      this.camera.move(0.0f, -deltaTime, 0.0f);
    }

    if(this.axis != null) {
      this.axis.render(this.camera);
    }
    /*if (this.mesh != null) {
      this.shaderProgram.bind();
      this.shaderProgram.useCamera(this.camera);
      this.mesh.render(this.shaderProgram, GL33.GL_TRIANGLES, 0, 36, false);
      this.shaderProgram.unbind();
    }*/
  }

  @Override
  public void update(float deltaTime) { }

  @EventHandler
  private void onResize(WindowResizeEvent event) {
    this.camera.aspectRatio((float) event.to.x / (float) event.to.y);
  }

  @EventHandler
  private void onMouseMove(MouseMoveEvent event) {
    Vector2i rel = event.to.sub(event.from, new Vector2i());
    this.camera.pitchDeg((float) -rel.y);
    this.camera.yawDeg((float) -rel.x);
    LOGGER.debug(
        "Vectors: Front: {} Right: {} Up: {}",
        this.camera.front().toString(NumberFormat.getInstance()),
        this.camera.right().toString(NumberFormat.getInstance()),
        this.camera.up().toString(NumberFormat.getInstance()));
    event.setCanceled(true);
  }

  @Override
  public float getProgress() {
    return 1.0f;
  }

  @Override
  public boolean loaded() {
    return this.done;
  }

  @Override
  public void dispose() {
    EventManager.getInstance().unregisterListener(this);
  }
}
