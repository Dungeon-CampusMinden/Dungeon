import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.input.MouseMoveEvent;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.GLUsageHint;
import de.fwatermann.dungine.graphics.camera.CameraPerspective;
import de.fwatermann.dungine.graphics.camera.CameraViewport;
import de.fwatermann.dungine.graphics.mesh.ArrayMesh;
import de.fwatermann.dungine.graphics.mesh.DataType;
import de.fwatermann.dungine.graphics.mesh.PrimitiveType;
import de.fwatermann.dungine.graphics.mesh.VertexAttribute;
import de.fwatermann.dungine.graphics.mesh.VertexAttributeList;
import de.fwatermann.dungine.graphics.shader.Shader;
import de.fwatermann.dungine.graphics.shader.ShaderProgram;
import de.fwatermann.dungine.graphics.texture.atlas.TextureAtlas;
import de.fwatermann.dungine.input.Keyboard;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.utils.CoordinateAxis;
import de.fwatermann.dungine.window.GameWindow;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.NumberFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Math;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;

public class ChunkTest extends GameState implements EventListener {

  private static final Logger LOGGER = LogManager.getLogger(ChunkTest.class);

  protected ChunkTest(GameWindow window) {
    super(window);
  }

  private ArrayMesh mesh;
  private ShaderProgram shaderProgram;
  private CameraPerspective camera;
  private CoordinateAxis axis;

  private TextureAtlas textureAtlas;

  private boolean done = false;

  @Override
  public void init() {
    LOGGER.info("Loading TriangleState...");

    try {
      Shader vertexShader =
          Shader.loadShader(Resource.load("/shaders/chunk.vsh"), Shader.ShaderType.VERTEX_SHADER);
      Shader geometryShader =
          Shader.loadShader(Resource.load("/shaders/chunk.gsh"), Shader.ShaderType.GEOMETRY_SHADER);
      Shader fragmentShader =
          Shader.loadShader(Resource.load("/shaders/chunk.fsh"), Shader.ShaderType.FRAGMENT_SHADER);
      this.shaderProgram = new ShaderProgram(vertexShader, geometryShader, fragmentShader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    this.camera = new CameraPerspective(new CameraViewport(this.window.size().x, this.window.size().y, 0, 0));
    this.camera.position(0.0f, 2.0f, 0.0f);

    this.axis = new CoordinateAxis(new Vector3f(0, 0, 0), 16.0f, true);

    this.textureAtlas = new TextureAtlas();
    this.textureAtlas.add(Resource.load("/textures/tiles/tile_00.png"));
    this.textureAtlas.add(Resource.load("/textures/tiles/tile_01.png"));
    this.textureAtlas.add(Resource.load("/textures/tiles/tile_02.png"));
    this.textureAtlas.add(Resource.load("/textures/tiles/tile_03.png"));
    this.textureAtlas.add(Resource.load("/textures/tiles/tile_04.png"));
    this.textureAtlas.add(Resource.load("/textures/tiles/tile_05.png"));

    VertexAttributeList vertexAttributes =
        new VertexAttributeList(
            new VertexAttribute(3, DataType.UNSIGNED_BYTE, "a_Position"),
            new VertexAttribute(1, DataType.UNSIGNED_BYTE, "a_Faces"),
            new VertexAttribute(3, DataType.UNSIGNED_INT, "a_FaceAtlasEntries"));

    // |  0 - 7  | 8 - 15 | 16 - 23 | 24 - 31 |
    //    PosX     PosY      PosZ      Faces
    //
    // |     32 - 47      |      48 - 63      |      64 - 79      |
    //   FaceAtlasEntryU     FaceAtlasEntryD     FaceAtlasEntryF
    //
    // |     80 - 95      |      96 - 111     |     112 - 127     |
    //   FaceAtlasEntryB     FaceAtlasEntryL     FaceAtlasEntryR

    ByteBuffer vertices = BufferUtils.createByteBuffer(128 * 16 * 16 * 16);
    for (int x = 0; x < 16; x++) {
      for (int y = 0; y < 16; y++) {
        for (int z = 0; z < 16; z++) {
          vertices.put((byte) x).put((byte) y).put((byte) z);

          byte faces = 0b00000000;
          if (x == 0) {
            faces |= 0b00001000; // Front
          }
          if (x == 15) {
            faces |= 0b00000100; // Back
          }
          if (y == 0) {
            faces |= 0b00010000; // Down
          }
          if (y == 15) {
            faces |= 0b00100000; // Up
          }
          if (z == 0) {
            faces |= 0b00000010; // Left
          }
          if (z == 15) {
            faces |= 0b00000001; // Right
          }
          vertices.put(faces); // All Faces (0b00UDFBLR)

          if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            vertices.putShort((short) 0); // Up
            vertices.putShort((short) 1); // Down
            vertices.putShort((short) 2); // Front
            vertices.putShort((short) 3); // Back
            vertices.putShort((short) 4); // Left
            vertices.putShort((short) 5); // Right
          } else {
            vertices.putShort((short) 1); // Down
            vertices.putShort((short) 0); // Up
            vertices.putShort((short) 3); // Back
            vertices.putShort((short) 2); // Front
            vertices.putShort((short) 5); // Right
            vertices.putShort((short) 4); // Left
          }
        }
      }
    }
    vertices.flip();
    this.mesh = new ArrayMesh(vertices, PrimitiveType.POINTS, GLUsageHint.DRAW_STATIC, vertexAttributes);

    this.done = true;
    EventManager.getInstance().registerListener(this);
  }

  @Override
  public void renderState(float deltaTime) {
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
      this.shaderProgram.setUniform3iv("uChunkSize", 16, 16, 16);
      this.mesh.render(this.camera, this.shaderProgram);
      this.shaderProgram.unbind();
    }
    if (this.axis != null) {
      this.axis.render(this.camera);
    }
  }

  @Override
  public void updateState(float deltaTime) {
    if (this.mesh != null) {
      int index = (int) Math.floor(Math.random() * 16 * 16);
      int entry = (int) Math.floor(Math.random() * 664);
    }
  }

  @EventHandler
  private void onResize(WindowResizeEvent event) {
    this.camera.updateViewport(event.to.x, event.to.y, 0, 0);
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
  public void disposeState() {
    EventManager.getInstance().unregisterListener(this);
  }
}
