import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.MouseMoveEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.IndexDataType;
import de.fwatermann.dungine.graphics.mesh.InstanceAttribute;
import de.fwatermann.dungine.graphics.mesh.InstanceAttributeList;
import de.fwatermann.dungine.graphics.mesh.InstancedIndexedMesh;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
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
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Math;
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

  private InstancedIndexedMesh mesh;
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
                    Resource.load("/shaders/tiles.vsh"), Shader.ShaderType.VERTEX_SHADER);
            Shader fragmentShader =
                Shader.loadShader(
                    Resource.load("/shaders/tiles.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
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
          for (int i = 0; i < 963; i++) {
            atlas.add(Resource.load(String.format("/textures/tiles/tile_%03d.png", i)));
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
                  new VertexAttribute(3, DataType.FLOAT, "a_Position"),
                  new VertexAttribute(2, DataType.FLOAT, "a_TextureCoords"));
          InstanceAttributeList instanceAttributes =
              new InstanceAttributeList(
                  new InstanceAttribute(1, 1, DataType.INT, "i_AtlasEntry"),
                  new InstanceAttribute(0, 3, DataType.INT, "i_TilePosition"));
          ByteBuffer vertices = BufferUtils.createByteBuffer(4 * 5 * 4);
          vertices
              .asFloatBuffer()
              .put(
                  new float[] {
                    0.0f, 0.0f, 0.0f, 0.0f, 1.0f,
                    0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
                    1.0f, 0.0f, 1.0f, 1.0f, 0.0f,
                    1.0f, 0.0f, 0.0f, 0.0f, 0.0f
                  });
          vertices.flip();

          ByteBuffer indices = BufferUtils.createByteBuffer(6 * 4);
          indices.asIntBuffer().put(new int[] {0, 1, 2, 2, 3, 0});
          indices.flip();

          ByteBuffer instance1 = BufferUtils.createByteBuffer(3 * 4 * 16 * 16);
          for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
              instance1.putInt(x);
              instance1.putInt(0);
              instance1.putInt(z);
            }
          }
          instance1.flip();

          ByteBuffer instance2 = BufferUtils.createByteBuffer(4 * 16 * 16);
          for (int i = 0; i < 16 * 16; i++) {
            instance2.putInt(193);
          }
          instance2.flip();

          this.mesh =
              new InstancedIndexedMesh(
                  vertices,
                  PrimitiveType.TRIANGLES,
                  indices,
                  IndexDataType.UNSIGNED_INT,
                  new ArrayList<>(List.of(instance1, instance2)),
                  16 * 16,
                  GLUsageHint.DRAW_STATIC,
                  vertexAttributes,
                  instanceAttributes);
        });
    stepper.done(
        true,
        (result) -> {
          this.done = true;
          GL33.glEnable(GL33.GL_BLEND);
          GL33.glBlendFunc(GL33.GL_SRC_ALPHA, GL33.GL_ONE_MINUS_SRC_ALPHA);

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
      this.textureAtlas.use(this.shaderProgram);
      this.shaderProgram.setUniform3iv("uChunkPosition", 0, 0, 0);
      this.shaderProgram.setUniform3iv("uChunkSize", 16, 1, 16);
      this.mesh.render(this.camera, this.shaderProgram);
      this.shaderProgram.unbind();
    }
    if (this.axis != null) {
      this.axis.render(this.camera);
    }
  }

  @Override
  public void update(float deltaTime) {
    if (this.mesh != null) {
      int index = (int) Math.floor(Math.random() * 16 * 16);
      int entry = (int) Math.floor(Math.random() * 664);
      this.mesh.getInstanceData(1).putInt(index * 4, entry);
      this.mesh.markInstanceDataDirty(1);
    }
  }

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
