package contrib.hud.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import contrib.components.HealthComponent;
import contrib.components.ManaComponent;
import contrib.components.StaminaComponent;
import contrib.hud.elements.AttributeBarHandle;
import core.Entity;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.platform.GameLoopHost;
import core.platform.NullRenderAdapter;
import core.platform.Platform;
import core.platform.RenderAdapter;
import core.resources.FileSystemResourcesAdapter;
import core.ui.StageHandle;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
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
  void localPlayerHealthBarStartsHiddenAtFullHealth() {
    Entity entity = entityWithFullHealth();
    entity.add(new PlayerComponent(true));
    RecordingAttributeBarHandle handle = new RecordingAttributeBarHandle();

    Map<Class<? extends contrib.components.BarDisplayable>, AttributeBarHandle> barMapping = new HashMap<>();
    barMapping.put(HealthComponent.class, handle);

    AttributeBarUtil.updateBar(
      entity, entity.fetch(HealthComponent.class).orElseThrow(), barMapping, 0f);

    assertFalse(handle.visible);
  }

  @Test
  void localPlayerHealthBarStaysVisibleAfterDamageAndHealing() {
    Entity entity = entityWithFullHealth();
    entity.add(new PlayerComponent(true));
    HealthComponent healthComponent = entity.fetch(HealthComponent.class).orElseThrow();
    RecordingAttributeBarHandle handle = new RecordingAttributeBarHandle();

    Map<Class<? extends contrib.components.BarDisplayable>, AttributeBarHandle> barMapping = new HashMap<>();
    barMapping.put(HealthComponent.class, handle);

    healthComponent.currentHealthpoints(8);
    healthComponent.currentHealthpoints(10);
    AttributeBarUtil.updateBar(entity, healthComponent, barMapping, 0f);

    assertTrue(handle.visible);
  }

  @Test
  void localPlayerManaAndStaminaBarsAppearIndependently() {
    Entity entity = entityWithResources();
    entity.add(new PlayerComponent(true));
    RecordingAttributeBarHandle manaHandle = new RecordingAttributeBarHandle();
    RecordingAttributeBarHandle staminaHandle = new RecordingAttributeBarHandle();

    Map<Class<? extends contrib.components.BarDisplayable>, AttributeBarHandle> barMapping = new HashMap<>();
    barMapping.put(ManaComponent.class, manaHandle);
    barMapping.put(StaminaComponent.class, staminaHandle);

    ManaComponent manaComponent = entity.fetch(ManaComponent.class).orElseThrow();
    StaminaComponent staminaComponent = entity.fetch(StaminaComponent.class).orElseThrow();

    AttributeBarUtil.updateBar(entity, manaComponent, barMapping, 0f);
    AttributeBarUtil.updateBar(entity, staminaComponent, barMapping, 0f);

    assertFalse(manaHandle.visible);
    assertFalse(staminaHandle.visible);

    manaComponent.consume(5f);
    AttributeBarUtil.updateBar(entity, manaComponent, barMapping, 0f);
    AttributeBarUtil.updateBar(entity, staminaComponent, barMapping, 0f);

    assertTrue(manaHandle.visible);
    assertFalse(staminaHandle.visible);

    staminaComponent.consume(5f);
    AttributeBarUtil.updateBar(entity, manaComponent, barMapping, 0f);
    AttributeBarUtil.updateBar(entity, staminaComponent, barMapping, 0f);

    assertTrue(manaHandle.visible);
    assertTrue(staminaHandle.visible);
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

  @Test
  void localPlayerBarsArePositionedBelowTheEntity() {
    Platform.render(new IdentityRenderAdapter());
    Platform.loopHost(new StubGameLoopHost());

    Entity entity = new Entity("hero");
    entity.add(new PositionComponent(new Point(3, 4)));
    entity.add(new PlayerComponent(true));
    RecordingAttributeBarHandle handle = new RecordingAttributeBarHandle();

    AttributeBarUtil.updatePosition(handle, entity, 10f);

    assertEquals(3.5f, handle.x);
    assertEquals(20f, handle.y);
  }

  @Test
  void nonPlayerBarsStayAboveTheEntity() {
    Platform.render(new IdentityRenderAdapter());
    Platform.loopHost(new StubGameLoopHost());

    Entity entity = new Entity("enemy");
    entity.add(new PositionComponent(new Point(3, 4)));
    RecordingAttributeBarHandle handle = new RecordingAttributeBarHandle();

    AttributeBarUtil.updatePosition(handle, entity, 10f);

    assertEquals(3.5f, handle.x);
    assertEquals(-11f, handle.y);
  }

  @AfterEach
  void tearDown() {
    Platform.render(new NullRenderAdapter());
    Platform.loopHost(null);
  }

  private static Entity entityWithFullHealth() {
    Entity entity = new Entity("hero");
    entity.add(new PositionComponent(new Point(3, 4)));
    entity.add(new DrawComponent(TEST_TEXTURE));
    entity.add(new HealthComponent(10));
    return entity;
  }

  private static Entity entityWithResources() {
    Entity entity = entityWithFullHealth();
    entity.add(new ManaComponent(30, 30, 1));
    entity.add(new StaminaComponent(20, 20, 1));
    return entity;
  }

  private static final class RecordingAttributeBarHandle implements AttributeBarHandle {
    private boolean visible;
    private float x;
    private float y;

    @Override
    public void remove() {}

    @Override
    public void setVisible(boolean visible) {
      this.visible = visible;
    }

    @Override
    public void setPosition(float x, float y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public void setValue(float value) {}
  }

  private static final class IdentityRenderAdapter implements RenderAdapter {
    @Override
    public Optional<Point> projectWorldToStage(Point worldPoint, StageHandle stageHandle) {
      return Optional.of(worldPoint);
    }
  }

  private static final class StubGameLoopHost implements GameLoopHost {
    @Override
    public void run(String[] args, core.game.GameLoopCore core) {}

    @Override
    public Optional<StageHandle> stage() {
      return Optional.of(new StubStageHandle());
    }
  }

  private static final class StubStageHandle implements StageHandle {
    @Override
    public Object raw() {
      return this;
    }

    @Override
    public <T> Optional<T> unwrap(Class<T> type) {
      return Optional.empty();
    }

    @Override
    public float getWidth() {
      return 1600;
    }

    @Override
    public float getHeight() {
      return 900;
    }

    @Override
    public void addActor(Object actor) {}

    @Override
    public void setKeyboardFocus(Object actor) {}

    @Override
    public int mouseX() {
      return 0;
    }

    @Override
    public int mouseY() {
      return 0;
    }
  }
}
