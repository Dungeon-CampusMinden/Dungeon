package rooms.systemRecovery.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.path.SimpleIPath;
import org.junit.jupiter.api.Test;

/** Tests the center and progress color used by System Recovery's local light sources. */
class SystemRecoveryLightingTest {

  @Test
  void lightSourceUsesCenterOfDrawnEntityRatherThanItsOrigin() {
    Entity entity = new Entity("wide-terminal");
    entity.add(new PositionComponent(new Point(3, 7)));
    DrawComponent draw = mock(DrawComponent.class);
    when(draw.getWidth()).thenReturn(4f);
    when(draw.getHeight()).thenReturn(2f);
    entity.add(draw);

    assertEquals(new Point(5, 8), SystemRecoveryClientLevel.lightSourcePosition(entity));
  }

  @Test
  void completedLabelsCastGreenLightAndIncompleteLabelsCastRedLight() {
    assertSame(Color.GREEN, SystemRecoveryClientLevel.doorLabelLightColor(true));
    assertSame(Color.RED, SystemRecoveryClientLevel.doorLabelLightColor(false));
  }

  @Test
  void everyMovingScannerEntityIsRecognizedAsLightSource() {
    assertTrue(SystemRecoveryClientLevel.isScanner(new Entity("module_scanner")));
    assertTrue(SystemRecoveryClientLevel.isScanner(new Entity("transport_scanner")));
    assertTrue(SystemRecoveryClientLevel.isScanner(new Entity("sort_belt_scanner")));
    assertFalse(SystemRecoveryClientLevel.isScanner(new Entity("scanner_terminal")));
  }

  @Test
  void animatedArchiveShelfIsRecognizedWithoutReplicatedEntityName() {
    Entity shelf = new Entity(123);
    shelf.add(
        new DrawComponent(
            new Animation(new SimpleIPath("objects/tech/digital_archive_shelf_animated.png"))));

    assertTrue(SystemRecoveryClientLevel.isArchiveShelf(shelf));
    assertFalse(SystemRecoveryClientLevel.isArchiveShelf(new Entity(124)));
  }
}
