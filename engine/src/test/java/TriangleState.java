import de.fwatermann.dungine.graphics.GLUsageHint;
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
import org.lwjgl.opengl.GL33;

public class TriangleState extends GameState {

  private static Logger logger = LogManager.getLogger();

  protected TriangleState(GameWindow window) {
    super(window);
  }

  private IndexedMesh mesh;
  private ShaderProgram shaderProgram;

  private long done = Long.MAX_VALUE;

  @Override
  public void init() {
    logger.info("Loading TriangleState...");
    this.done = System.currentTimeMillis() + 10_000;

    LoadStepper stepper = new LoadStepper(this.window);

    stepper
        .step(
            "vertexBuffer",
            () -> { // 0
              FloatBuffer vertices = BufferUtils.createFloatBuffer(9);
              vertices.put(new float[] {0.0f, 0.5f, 0.0f, -0.5f, -0.5f, 0.0f, 0.5f, -0.5f, 0.0f});
              vertices.flip();
              logger.debug("Loaded vertices");
              return vertices;
            })
        .step(
            "indexBuffer",
            () -> { // 1
              IntBuffer indices = BufferUtils.createIntBuffer(3);
              indices.put(new int[] {0, 1, 2});
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
        .done(
            true,
            (results) -> {
              this.shaderProgram = results.result("shaderProgram");
              this.mesh = results.result("mesh");
              logger.info("TriangleState loaded!");
            });
    stepper.start();
  }

  @Override
  public void render(float deltaTime) {
    if (this.mesh != null)
      this.mesh.render(this.shaderProgram, GL33.GL_TRIANGLES, 0, 3, true);
  }

  @Override
  public void dispose() {}

  @Override
  public float getProgress() {
    return 1.0f - (this.done - System.currentTimeMillis()) / 10_000.0f;
  }

  @Override
  public boolean loaded() {
    return System.currentTimeMillis() >= this.done;
  }

  @Override
  public void update(float deltaTime) {
    super.update(deltaTime);
  }
}
