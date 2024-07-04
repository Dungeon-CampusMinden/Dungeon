import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.mesh.IndexedMesh;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.state.LoadStepper;
import de.fwatermann.dungine.window.GameWindow;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;

public class TriangleState extends GameState implements EventListener {

  private static Logger logger = LogManager.getLogger();

  protected TriangleState(GameWindow window) {
    super(window);
  }

  private IndexedMesh mesh;
  private ShaderProgram shaderProgram;
  private CameraPerspective camera;

  private boolean done = false;

  @Override
  public void init() {
    logger.info("Loading TriangleState...");

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
                    +0.5f, -0.5f, +0.5f,
                    -0.5f, -0.5f, +0.5f,
                    -0.5f, +0.5f, -0.5f,
                    +0.5f, +0.5f, -0.5f,
                    +0.5f, +0.5f, +0.5f,
                    -0.5f, +0.5f, +0.5f
                  });
              vertices.flip();
              logger.debug("Loaded vertices");
              return vertices;
            })
        .step(
            "indexBuffer",
            () -> { // 1
              IntBuffer indices = BufferUtils.createIntBuffer(36);
              indices.put(
                  new int[] {
                    0, 1, 5,
                    5, 4, 0,
                    3, 0, 4,
                    4, 7, 3,
                    1, 2, 6,
                    6, 5, 1,
                    3, 2, 1,
                    1, 0, 3,
                    7, 6, 2,
                    2, 3, 7,
                    4, 5, 6,
                    6, 7, 4
                  });
              indices.flip();
              logger.debug("Loaded indices");
              return indices;
            })
        .step(
            "mesh",
            true,
            (results) -> { // 2
              FloatBuffer vertices = results.result("vertexBuffer");
              IntBuffer indices = results.result("indexBuffer");
              logger.debug("Created mesh");
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
              logger.debug("Loading vertex shader");
              try {
                return Shader.loadShader(
                    Resource.load("/shaders/triangle.vsh"), Shader.ShaderType.VERTEX_SHADER);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .step(
            "fragmentShader",
            true,
            () -> { // 4
              logger.debug("Loading fragment shader");
              try {
                return Shader.loadShader(
                    Resource.load("/shaders/triangle.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            })
        .step(
            "shaderProgram",
            true,
            (results) -> { // 5
              logger.debug("Creating shader program");
              return new ShaderProgram(
                  results.result("vertexShader"), results.result("fragmentShader"));
            })
        .step(
            "camera",
            () -> {
              this.camera =
                  new CameraPerspective()
                      .position(0.0f, 0.0f, -2.0f)
                      .lookAt(0, 0, 0);
              logger.debug("Created camera");
            })
        .done(
            true,
            (results) -> {
              this.shaderProgram = results.result("shaderProgram");
              this.mesh = results.result("mesh");
              logger.info("TriangleState loaded!");
              EventManager.getInstance().registerListener(this);
              this.done = true;
            });
    stepper.start();
  }

  @Override
  public void render(float deltaTime) {
    this.camera.update();
    if (this.mesh != null) {
      this.shaderProgram.bind();
      this.shaderProgram.useCamera(this.camera);
      this.mesh.render(this.shaderProgram, GL33.GL_TRIANGLES, 0, 36, false);
      this.shaderProgram.unbind();
    }
  }

  @Override
  public void update(float deltaTime) {
    if (this.mesh != null) {
      this.mesh.rotate(0, 1.0f, 0.0f, 10.0f * deltaTime);
    }
  }

  @EventHandler
  private void onResize(WindowResizeEvent event) {
    this.camera.aspectRatio((float) event.to.x / (float) event.to.y);
  }

  @EventHandler
  private void onKey(KeyboardEvent event) {
    if(event.key == GLFW.GLFW_KEY_I) {
      if(event.action == KeyboardEvent.KeyAction.PRESS) {
        this.window.runOnMainThread(() -> {
          GL33.glPolygonMode(GL33.GL_FRONT, GL33.GL_LINE);
        });
      } else if(event.action == KeyboardEvent.KeyAction.RELEASE) {
        this.window.runOnMainThread(
            () -> {
              GL33.glPolygonMode(GL33.GL_FRONT, GL33.GL_FILL);
            });
      }
    }
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
