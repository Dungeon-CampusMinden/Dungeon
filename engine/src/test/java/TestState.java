import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.MouseMoveEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.mesh.IndexedMesh;
import de.fwatermann.dungine.graphics.mesh.InstanceAttribute;
import de.fwatermann.dungine.graphics.mesh.InstanceAttributeList;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.mesh.VertexAttributeList;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.atlas.TextureAtlas;
import de.fwatermann.dungine.input.Keyboard;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.state.LoadStepper;
import de.fwatermann.dungine.utils.CoordinateAxis;
import de.fwatermann.dungine.window.GameWindow;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.text.NumberFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;

public class TestState extends GameState implements EventListener {

  private static final Logger LOGGER = LogManager.getLogger(TestState.class);

  protected TestState(GameWindow window) {
    super(window);
  }

  private IndexedMesh mesh;
  private ShaderProgram shaderProgram;
  private CameraPerspective camera;
  private CoordinateAxis axis;

  private TextureAtlas textureAtlas;

  private boolean done = false;

  @Override
  public void init() {
    LOGGER.info("Loading TriangleState...");

    LoadStepper stepper = new LoadStepper(this.window);

    stepper.step(
        "shader",
        true,
        () -> {
          try {
            Shader vertexShader =
                Shader.loadShader(
                    Resource.load("/shaders/tile.vsh"), Shader.ShaderType.VERTEX_SHADER);
            Shader fragmentShader =
                Shader.loadShader(
                    Resource.load("/shaders/tile.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
            this.shaderProgram = new ShaderProgram(vertexShader, fragmentShader);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    stepper.step(
        "camera",
        false,
        () -> {
          this.camera = new CameraPerspective();
        });
    stepper.step(
        "axis",
        true,
        () -> {
          this.axis = new CoordinateAxis(new Vector3f(0, 0, 0), 10.0f, true);
        });
    stepper.step(
        "textureAtlas",
        true,
        () -> {
          this.textureAtlas = new TextureAtlas();
          return this.textureAtlas;
        });
    stepper.step(
        "textures",
        true,
        (results) -> {
          TextureAtlas atlas = results.result("textureAtlas");
          for (int i = 0; i < 14; i++) {
            atlas.add(Resource.load(String.format("/textures/tiles/tile_%02d.png", i)));
          }
          try {
            atlas.saveAtlas(Path.of("atlas"));
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
    stepper.step(
        "mesh",
        true,
        (results) -> {
          VertexAttributeList vertexAttributes =
              new VertexAttributeList(
                  new VertexAttribute(
                      VertexAttribute.Usage.POSITION, 3, GL33.GL_FLOAT, "a_Position"),
                  new VertexAttribute(
                      VertexAttribute.Usage.POSITION, 2, GL33.GL_FLOAT, "a_TextureCoords"));
          InstanceAttributeList instanceAttributes =
              new InstanceAttributeList(
                  new InstanceAttribute(0, 1, GL33.GL_UNSIGNED_SHORT, "i_AtlasEntry"));
          FloatBuffer vertices = BufferUtils.createFloatBuffer(5 * 4);
          vertices.put(new float[] {
            0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f, 0.0f
          });

          vertices.flip();
          IntBuffer indices = BufferUtils.createIntBuffer(6);
          indices.put(new int[] {0, 1, 2, 2, 3, 0});
          indices.flip();

          ByteBuffer instance1 = BufferUtils.createByteBuffer(4);
          instance1.putInt(0);
          instance1.flip();

          this.mesh = new IndexedMesh(vertices, indices, GLUsageHint.DRAW_STATIC, vertexAttributes);
        });
    stepper.done(
        true,
        (result) -> {
          this.done = true;

          EventManager.getInstance().registerListener(this);
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

    if (Keyboard.keyPressed(GLFW.GLFW_KEY_W)) {
      this.camera.move(
          this.camera.front().mul(1.0f, 0.0f, 1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_S)) {
      this.camera.move(
          this.camera.front().mul(-1.0f, 0.0f, -1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_A)) {
      this.camera.move(
          this.camera.right().mul(-1.0f, 0.0f, -1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_D)) {
      this.camera.move(
          this.camera.right().mul(1.0f, 0.0f, 1.0f, new Vector3f()).normalize().mul(deltaTime));
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_SPACE)) {
      this.camera.move(0.0f, deltaTime, 0.0f);
    }
    if (Keyboard.keyPressed(GLFW.GLFW_KEY_LEFT_SHIFT)) {
      this.camera.move(0.0f, -deltaTime, 0.0f);
    }
    this.window.title(this.camera.position().toString(NumberFormat.getInstance()));

    if (this.mesh != null) {
      this.shaderProgram.bind();
      this.shaderProgram.useCamera(this.camera);
      this.textureAtlas.use(this.shaderProgram);
      this.mesh.render(this.shaderProgram, GL33.GL_TRIANGLES, 0, 6, false);
      this.shaderProgram.unbind();
    }
    if (this.axis != null) {
      this.axis.render(this.camera);
    }
  }

  @Override
  public void update(float deltaTime) {}

  @EventHandler
  private void onResize(WindowResizeEvent event) {
    this.camera.aspectRatio((float) event.to.x / (float) event.to.y);
  }

  @EventHandler
  private void onMouseMove(MouseMoveEvent event) {
    Vector2i rel = event.to.sub(event.from, new Vector2i());
    this.camera.pitchDeg((float) -rel.y);
    this.camera.yawDeg((float) -rel.x);
    if (this.window.hasFocus()) {
      event.to.set(this.window.size().x / 2, this.window.size().y / 2);
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
