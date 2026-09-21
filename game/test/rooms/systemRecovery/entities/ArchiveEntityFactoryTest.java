package rooms.systemRecovery.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Entity;
import engine.components.DrawComponent;
import engine.utils.Point;
import org.junit.jupiter.api.Test;

/** Verifies the combined archive module's visual operating-state signal. */
class ArchiveEntityFactoryTest {

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
