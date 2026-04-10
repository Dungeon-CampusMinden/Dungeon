package contrib.hud.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.components.HealthComponent;
import contrib.hud.elements.AttributeBarHandle;
import core.Entity;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.platform.Platform;
import core.platform.fs.FileSystemResourcesAdapter;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for player-specific visibility rules of attribute bars. */
class AttributeBarUtilTest {
  private static final SimpleIPath TEST_TEXTURE = new SimpleIPath("test_assets/textures/mailbox.png");

  @BeforeEach
  void setUp() {
    Platform.resources(FileSystemResourcesAdapter.autoDetect());
  }

  @Test
  void localPlayerHealthBarStaysVisibleAtFullHealth() {
    Entity entity = entityWithFullHealth();
    entity.add(new PlayerComponent(true));
    RecordingAttributeBarHandle handle = new RecordingAttributeBarHandle();

    Map<Class<? extends contrib.components.BarDisplayable>, AttributeBarHandle> barMapping = new HashMap<>();
    barMapping.put(HealthComponent.class, handle);

    AttributeBarUtil.updateBar(
      entity, entity.fetch(HealthComponent.class).orElseThrow(), barMapping, 0f);

    assertTrue(handle.visible);
  }

  @Test
  void nonPlayerHealthBarStaysHiddenAtFullHealth() {
    Entity entity = entityWithFullHealth();
    RecordingAttributeBarHandle handle = new RecordingAttributeBarHandle();

    Map<Class<? extends contrib.components.BarDisplayable>, AttributeBarHandle> barMapping = new HashMap<>();
    barMapping.put(HealthComponent.class, handle);

    AttributeBarUtil.updateBar(
      entity, entity.fetch(HealthComponent.class).orElseThrow(), barMapping, 0f);

    assertFalse(handle.visible);
  }

  private static Entity entityWithFullHealth() {
    Entity entity = new Entity("hero");
    entity.add(new PositionComponent(new Point(3, 4)));
    entity.add(new DrawComponent(TEST_TEXTURE));
    entity.add(new HealthComponent(10));
    return entity;
  }

  private static final class RecordingAttributeBarHandle implements AttributeBarHandle {
    private boolean visible;

    @Override
    public void remove() {}

    @Override
    public void setVisible(boolean visible) {
      this.visible = visible;
    }

    @Override
    public void setPosition(float x, float y) {}

    @Override
    public void setValue(float value) {}
  }
}
