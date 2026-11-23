package feature.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Texture;
import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.Vector2;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.IPath;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class EntityUtilsTest {
  private static final IPath SPRITE = new SimpleIPath("test_assets/textures/mailbox.png");

  @AfterEach
  void cleanup() {
    TextureMap.instance().clear();
  }

  @Test
  void pointOnTheSpriteAboveTheColliderIsOverTheEntity() {
    // Drawn as 16x32 pixels: one tile wide, two tiles high, collider only on the lower half tile
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(16);
    when(texture.getHeight()).thenReturn(32);
    TextureMap.instance().put(SPRITE.pathString(), texture);
    Entity entity = new Entity();
    entity.add(new PositionComponent(new Point(0, 0)));
    entity.add(new DrawComponent(SPRITE));
    entity.add(new CollideComponent(Vector2.of(0, 0), Vector2.of(1, 0.5f)));

    assertTrue(EntityUtils.isPointOverEntity(entity, new Point(0.5f, 1.5f)));
  }

  @Test
  void entityWithColliderButNoSpriteGetsNoRadiusFallback() {
    Entity entity = new Entity();
    entity.add(new PositionComponent(new Point(0, 0)));
    entity.add(new CollideComponent(Vector2.of(0, 0), Vector2.of(0.2f, 0.2f)));

    // Outside the collider, but within the fallback radius of its center (0.1, 0.1)
    assertFalse(EntityUtils.isPointOverEntity(entity, new Point(0.4f, 0.1f)));
  }
}
