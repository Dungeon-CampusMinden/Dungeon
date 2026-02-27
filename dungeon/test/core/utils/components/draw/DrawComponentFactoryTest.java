package core.utils.components.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.components.DrawComponent;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.animation.AnimationConfig;
import core.utils.components.draw.animation.SpritesheetConfig;
import org.junit.jupiter.api.Test;

/** Tests for {@link DrawComponentFactory}. */
public class DrawComponentFactoryTest {

  private static final float DELTA = 1e-6f;
  private static final String TEXTURE_PATH = "animation/missing_texture.png";

  /** Verifies reconstruction with plain texture payloads. */
  @Test
  public void testFromDrawInfo_singleTexture() {
    DrawInfoData info =
        new DrawInfoData(
            TEXTURE_PATH,
            1.5f,
            2.0f,
            "idle",
            3,
            new DrawInfoData.AnimationConfigData(5, true, false, false),
            null);

    DrawComponent drawComponent = DrawComponentFactory.fromDrawInfo(info);
    Animation animation = drawComponent.currentAnimation();
    AnimationConfig animationConfig = animation.getConfig();

    assertFalse(animationConfig.config().isPresent());
    assertEquals(5, animationConfig.framesPerSprite());
    assertTrue(animationConfig.isLooping());
    assertFalse(animationConfig.centered());
    assertFalse(animationConfig.mirrored());
    assertEquals(1.5f, animation.getScaleX(), DELTA);
    assertEquals(2.0f, animation.getScaleY(), DELTA);
    assertEquals(15, animation.frameCount());
  }

  /** Verifies reconstruction with explicit spritesheet payloads. */
  @Test
  public void testFromDrawInfo_spritesheet() {
    DrawInfoData info =
        new DrawInfoData(
            TEXTURE_PATH,
            null,
            null,
            "idle",
            1,
            new DrawInfoData.AnimationConfigData(2, false, true, true),
            new DrawInfoData.SpritesheetConfigData(8, 12, 1, 2, 3, 4));

    DrawComponent drawComponent = DrawComponentFactory.fromDrawInfo(info);
    Animation animation = drawComponent.currentAnimation();
    AnimationConfig animationConfig = animation.getConfig();
    SpritesheetConfig spritesheetConfig = animationConfig.config().orElseThrow();

    assertTrue(animationConfig.config().isPresent());
    assertEquals(8, spritesheetConfig.spriteWidth());
    assertEquals(12, spritesheetConfig.spriteHeight());
    assertEquals(1, spritesheetConfig.x());
    assertEquals(2, spritesheetConfig.y());
    assertEquals(3, spritesheetConfig.rows());
    assertEquals(4, spritesheetConfig.columns());
    assertEquals(2, animationConfig.framesPerSprite());
    assertFalse(animationConfig.isLooping());
    assertTrue(animationConfig.centered());
    assertTrue(animationConfig.mirrored());
    assertEquals(2, animation.frameCount());
  }

  /** Verifies frames-per-sprite values below 1 are clamped during reconstruction. */
  @Test
  public void testFromDrawInfo_framesPerSpriteGuard() {
    DrawInfoData info =
        new DrawInfoData(
            TEXTURE_PATH,
            null,
            null,
            "idle",
            4,
            new DrawInfoData.AnimationConfigData(0, true, false, false),
            null);

    DrawComponent drawComponent = DrawComponentFactory.fromDrawInfo(info);
    Animation animation = drawComponent.currentAnimation();

    assertEquals(1, animation.getConfig().framesPerSprite());
    assertEquals(4, animation.frameCount());
  }
}
