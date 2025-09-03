package core.utils.components.draw.animation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import core.utils.components.draw.TextureMap;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.*;

class AnimationTest {

  private Texture textureA;
  private Texture textureB;

  @BeforeEach
  void setUp() {
    // mock textures with deterministic sizes
    textureA = mock(Texture.class);
    when(textureA.getWidth()).thenReturn(64);
    when(textureA.getHeight()).thenReturn(32);

    textureB = mock(Texture.class);
    when(textureB.getWidth()).thenReturn(64);
    when(textureB.getHeight()).thenReturn(32);

    // make sure TextureMap starts empty
    TextureMap.instance().clear();
  }

  @AfterEach
  void tearDown() {
    // clean up between tests
    TextureMap.instance().clear();
  }

  // ---------- ctor validation ----------

  @Test
  void singlePathCtor_null_throws() {
    assertThrows(IllegalArgumentException.class, () -> new Animation((IPath) null));
  }

  @Test
  void singlePathCtor_empty_throws() {
    IPath empty = new SimpleIPath("");
    assertThrows(IllegalArgumentException.class, () -> new Animation(empty));
  }

  @Test
  void listCtor_null_throws() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new Animation((List<IPath>) null, new AnimationConfig()));
  }

  @Test
  void listCtor_empty_throws() {
    assertThrows(
        IllegalArgumentException.class, () -> new Animation(List.of(), new AnimationConfig()));
  }

  // ---------- scaling using real AnimationConfig ----------

  @Test
  void widthHeight_respectScale_andScaleYZeroUsesScaleX() {
    AnimationConfig cfg =
        new AnimationConfig()
            .framesPerSprite(1)
            .scaleX(2.0f) // doubled
            .scaleY(0.0f) // use X for Y
            .isLooping(true);

    IPath p1 = new SimpleIPath("a.png");
    IPath p2 = new SimpleIPath("b.png");

    // preload TextureMap so no real file IO happens
    TextureMap.instance().put("a.png", textureA);
    TextureMap.instance().put("b.png", textureB);

    Animation anim = new Animation(Arrays.asList(p1, p2), cfg);

    float expectedW = 2 * 2.0f;
    float expectedH = 1 * 2.0f;

    assertEquals(expectedW, anim.getWidth(), 1e-6);
    assertEquals(expectedH, anim.getHeight(), 1e-6);
  }

  @Test
  void widthHeight_respectIndependentScaleY() {
    AnimationConfig cfg =
        new AnimationConfig().framesPerSprite(1).scaleX(1.5f).scaleY(0.5f).isLooping(true);

    IPath p1 = new SimpleIPath("a.png");
    TextureMap.instance().put("a.png", textureA);

    Animation anim = new Animation(p1, cfg);

    float expectedW = 2 * 1.5f;
    float expectedH = 1 * 0.5f;

    assertEquals(expectedW, anim.getWidth(), 1e-6);
    assertEquals(expectedH, anim.getHeight(), 1e-6);
  }

  // ---------- looping vs. non-looping ----------

  @Test
  void getSprite_loops_whenLoopingEnabled() {
    AnimationConfig cfg =
        new AnimationConfig()
            .framesPerSprite(3) // 3 frames per sprite
            .scaleX(1f)
            .scaleY(0f)
            .isLooping(true);

    IPath p1 = new SimpleIPath("a.png");
    IPath p2 = new SimpleIPath("b.png");
    TextureMap.instance().put("a.png", textureA);
    TextureMap.instance().put("b.png", textureB);

    Animation anim = new Animation(Arrays.asList(p1, p2), cfg);

    anim.frameCount(0);
    Sprite s0 = anim.getSprite(); // idx 0
    anim.frameCount(2);
    Sprite s2 = anim.getSprite(); // idx 0
    anim.frameCount(3);
    Sprite s3 = anim.getSprite(); // idx 1
    anim.frameCount(5);
    Sprite s5 = anim.getSprite(); // idx 1
    anim.frameCount(6);
    Sprite s6 = anim.getSprite(); // wraps to idx 0

    assertSame(s0, s2);
    assertNotSame(s0, s3);
    assertSame(s3, s5);
    assertSame(s0, s6);
    assertTrue(anim.isFinished());
  }

  @Test
  void getSprite_clamps_whenNotLooping_and_isFinishedReflectsThat() {
    AnimationConfig cfg =
        new AnimationConfig().framesPerSprite(2).scaleX(1f).scaleY(1f).isLooping(false);

    IPath p1 = new SimpleIPath("a.png");
    IPath p2 = new SimpleIPath("b.png");
    TextureMap.instance().put("a.png", textureA);
    TextureMap.instance().put("b.png", textureB);

    Animation anim = new Animation(Arrays.asList(p1, p2), cfg);

    anim.frameCount(0);
    Sprite f0 = anim.getSprite(); // first
    anim.frameCount(1);
    Sprite f1 = anim.getSprite(); // first
    anim.frameCount(2);
    Sprite f2 = anim.getSprite(); // second
    anim.frameCount(3);
    Sprite f3 = anim.getSprite(); // second
    assertFalse(anim.isFinished());

    anim.frameCount(4); // index would be 2 -> finished, clamp to last
    Sprite f4 = anim.getSprite();

    assertSame(f0, f1);
    assertNotSame(f0, f2);
    assertSame(f2, f3);
    assertSame(f2, f4);
    assertTrue(anim.isFinished());
  }

  // ---------- update() ----------

  @Test
  void update_incrementsFrameCount_andReturnsCurrentSprite() {
    AnimationConfig cfg =
        new AnimationConfig().framesPerSprite(2).scaleX(1f).scaleY(1f).isLooping(true);

    IPath p1 = new SimpleIPath("a.png");
    IPath p2 = new SimpleIPath("b.png");
    TextureMap.instance().put("a.png", textureA);
    TextureMap.instance().put("b.png", textureB);

    Animation anim = new Animation(Arrays.asList(p1, p2), cfg);

    int before = anim.frameCount();
    Sprite s0 = anim.getSprite();
    Sprite u0 = anim.update();

    assertEquals(before + 1, anim.frameCount());
    assertSame(s0, u0);

    anim.update(); // cross boundary
    Sprite s2 = anim.getSprite();
    assertNotSame(s0, s2);
  }

  // ---------- spritesheet path (uses single-path ctor) ----------

  @Test
  void spritesheet_slicesCorrectCount_andScalingFromSpriteSize() {
    // Mock the spritesheet texture and preload into TextureMap
    Texture sheet = mock(Texture.class);
    when(sheet.getWidth()).thenReturn(256);
    when(sheet.getHeight()).thenReturn(256);

    IPath sheetPath = new SimpleIPath("assets/sheet.png");
    TextureMap.instance().put("assets/sheet.png", sheet);

    // 2 rows x 3 cols, sprite size 32x24, with offset
    SpritesheetConfig ssc =
        new SpritesheetConfig().rows(2).columns(3).spriteWidth(32).spriteHeight(16).x(4).y(6);

    AnimationConfig cfg =
        new AnimationConfig(ssc)
            .framesPerSprite(1)
            .scaleX(2f) // width = 2 * 2 = 4
            .scaleY(0.5f) // height = 1 * 0.5 = 0.5
            .isLooping(true);

    Animation anim = new Animation(sheetPath, cfg);

    // With framesPerSprite=1 and looping, index = frameCount % 6
    Sprite s0 = anim.getSprite(); // index 0
    anim.frameCount(1);
    Sprite s1 = anim.getSprite();
    anim.frameCount(2);
    Sprite s2 = anim.getSprite();
    anim.frameCount(3);
    Sprite s3 = anim.getSprite();
    anim.frameCount(4);
    Sprite s4 = anim.getSprite();
    anim.frameCount(5);
    Sprite s5 = anim.getSprite();
    anim.frameCount(6);
    Sprite s6 = anim.getSprite(); // wraps to index 0

    assertNotSame(s0, s1);
    assertNotSame(s1, s2);
    assertNotSame(s2, s3);
    assertNotSame(s3, s4);
    assertNotSame(s4, s5);
    assertSame(s0, s6); // looped

    assertEquals(4, anim.getWidth(), 1e-6);
    assertEquals(0.5f, anim.getHeight(), 1e-6);
    assertEquals(32, anim.getSpriteWidth(), 1e-6);
    assertEquals(16, anim.getSpriteHeight(), 1e-6);
  }
}
