package core.utils.components.draw.animation;

import static org.junit.jupiter.api.Assertions.*;

import core.platform.Platform;
import core.resources.FileSystemResourcesAdapter;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnimationTest {

  @BeforeEach
  void setUp() {
    Platform.resources(FileSystemResourcesAdapter.autoDetect());
  }

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

    Animation anim = new Animation(new SimpleIPath("missing_a.png"), cfg);

    assertEquals(2.0f, anim.getWidth(), 1e-6);
    assertEquals(2.0f, anim.getHeight(), 1e-6);
  }

  @Test
  void widthHeight_respectIndependentScaleY() {
    AnimationConfig cfg =
        new AnimationConfig().framesPerSprite(1).scaleX(1.5f).scaleY(0.5f).isLooping(true);

    Animation anim = new Animation(new SimpleIPath("missing_a.png"), cfg);

    assertEquals(1.5f, anim.getWidth(), 1e-6);
    assertEquals(0.5f, anim.getHeight(), 1e-6);
  }

  // ---------- looping vs. non-looping ----------

  @Test
  void getFrame_loops_whenLoopingEnabled() {
    AnimationConfig cfg =
        new AnimationConfig().framesPerSprite(3).scaleX(1f).scaleY(0f).isLooping(true);

    IPath p1 = new SimpleIPath("a.png");
    IPath p2 = new SimpleIPath("b.png");

    Animation anim = new Animation(Arrays.asList(p1, p2), cfg);

    anim.frameCount(0);
    AnimationFrame f0 = anim.getFrame();

    anim.frameCount(2);
    AnimationFrame f2 = anim.getFrame();

    anim.frameCount(3);
    AnimationFrame f3 = anim.getFrame();

    anim.frameCount(5);
    AnimationFrame f5 = anim.getFrame();

    anim.frameCount(6);
    AnimationFrame f6 = anim.getFrame();

    assertSame(f0, f2);
    assertNotSame(f0, f3);
    assertSame(f3, f5);
    assertSame(f0, f6);

    assertEquals("a.png", f0.texturePath().pathString());
    assertEquals("b.png", f3.texturePath().pathString());
    assertTrue(anim.isFinished());
  }

  @Test
  void getFrame_clamps_whenNotLooping_and_isFinishedReflectsThat() {
    AnimationConfig cfg =
        new AnimationConfig().framesPerSprite(2).scaleX(1f).scaleY(1f).isLooping(false);

    IPath p1 = new SimpleIPath("a.png");
    IPath p2 = new SimpleIPath("b.png");

    Animation anim = new Animation(Arrays.asList(p1, p2), cfg);

    anim.frameCount(0);
    AnimationFrame f0 = anim.getFrame();

    anim.frameCount(1);
    AnimationFrame f1 = anim.getFrame();

    anim.frameCount(2);
    AnimationFrame f2 = anim.getFrame();

    anim.frameCount(3);
    AnimationFrame f3 = anim.getFrame();
    assertFalse(anim.isFinished());

    anim.frameCount(4);
    AnimationFrame f4 = anim.getFrame();

    assertSame(f0, f1);
    assertNotSame(f0, f2);
    assertSame(f2, f3);
    assertSame(f2, f4);
    assertTrue(anim.isFinished());
  }

  // ---------- update() ----------

  @Test
  void update_incrementsFrameCount_andReturnsCurrentFrame() {
    AnimationConfig cfg =
        new AnimationConfig().framesPerSprite(2).scaleX(1f).scaleY(1f).isLooping(true);

    IPath p1 = new SimpleIPath("a.png");
    IPath p2 = new SimpleIPath("b.png");

    Animation anim = new Animation(Arrays.asList(p1, p2), cfg);

    int before = anim.frameCount();
    AnimationFrame f0 = anim.getFrame();
    AnimationFrame u0 = anim.update();

    assertEquals(before + 1, anim.frameCount());
    assertSame(f0, u0);

    anim.update();
    AnimationFrame f2 = anim.getFrame();
    assertNotSame(f0, f2);
  }

  // ---------- spritesheet path (uses single-path ctor) ----------

  @Test
  void spritesheet_buildsFrameRegions_andScalingFromSpriteSize() {
    IPath sheetPath = new SimpleIPath("assets/sheet.png");

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

    AnimationFrame f0 = anim.getFrame();
    anim.frameCount(1);
    AnimationFrame f1 = anim.getFrame();
    anim.frameCount(2);
    AnimationFrame f2 = anim.getFrame();
    anim.frameCount(3);
    AnimationFrame f3 = anim.getFrame();
    anim.frameCount(4);
    AnimationFrame f4 = anim.getFrame();
    anim.frameCount(5);
    AnimationFrame f5 = anim.getFrame();
    anim.frameCount(6);
    AnimationFrame f6 = anim.getFrame();

    assertNotSame(f0, f1);
    assertNotSame(f1, f2);
    assertNotSame(f2, f3);
    assertNotSame(f3, f4);
    assertNotSame(f4, f5);
    assertSame(f0, f6);

    assertTrue(f0.hasRegion());
    assertEquals(4, f0.regionX());
    assertEquals(6, f0.regionY());
    assertEquals(32, f0.regionW());
    assertEquals(16, f0.regionH());

    assertEquals(36, f1.regionX());
    assertEquals(6, f1.regionY());

    assertEquals(4, f3.regionX());
    assertEquals(22, f3.regionY());

    assertEquals(4, anim.getWidth(), 1e-6);
    assertEquals(0.5f, anim.getHeight(), 1e-6);
    assertEquals(32, anim.getSpriteWidth(), 1e-6);
    assertEquals(16, anim.getSpriteHeight(), 1e-6);
  }
}
