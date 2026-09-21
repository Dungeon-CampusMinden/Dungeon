package rooms.systemRecovery.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Entity;
import engine.components.DrawComponent;
import engine.utils.Point;
import engine.utils.components.draw.DrawComponentFactory;
import engine.utils.components.draw.DrawInfoData;
import feature.components.CollideComponent;
import feature.collision.Collider;
import feature.entities.deco.Deco;
import org.junit.jupiter.api.Test;

/** Verifies the combined archive module's visual operating-state signal. */
class ArchiveEntityFactoryTest {

  @Test
  void bookshelfUsesAnimatedSheetAndRaisedCollider() {
    Entity shelf =
        ArchiveEntityFactory.archiveBookshelf(
            new Point(4, 5), "archive_shelf_energie", "world.archive.shelf-energy");

    DrawComponent draw = shelf.fetch(DrawComponent.class).orElseThrow();
    assertEquals(
        "objects/tech/digital_archive_shelf_animated.png",
        draw.currentAnimation().sourcePath().orElseThrow().pathString());
    assertEquals(4, Deco.DigitalArchiveShelf.config().config().orElseThrow().columns());
    assertEquals(8, Deco.DigitalArchiveShelf.config().framesPerSprite());
    DrawInfoData replicated = DrawComponentFactory.toDrawInfo(draw);
    assertEquals(4, replicated.spritesheetConfig().columns());
    assertEquals(128, replicated.spritesheetConfig().spriteWidth());
    assertEquals(8, replicated.animationConfig().framesPerSprite());

    Collider collider = shelf.fetch(CollideComponent.class).orElseThrow().collider();
    assertEquals(0.08f, collider.left());
    assertEquals(0.55f, collider.bottom());
    assertEquals(2.2f, collider.width());
    assertEquals(1.55f, collider.height());
  }

  @Test
  void activeArchiveNodeUsesGreenModuleTint() {
    Entity node =
        ArchiveEntityFactory.archiveDataDisplay(
            new Point(1, 2), "archive_node0", "world.archive.node", true);

    assertEquals(0x33FF66FF, node.fetch(DrawComponent.class).orElseThrow().tintColor());
  }

  @Test
  void inactiveArchiveNodeUsesRedModuleTint() {
    Entity node =
        ArchiveEntityFactory.archiveDataDisplay(
            new Point(1, 2), "archive_node1", "world.archive.node", false);

    assertEquals(0xFF3333FF, node.fetch(DrawComponent.class).orElseThrow().tintColor());
  }
}
