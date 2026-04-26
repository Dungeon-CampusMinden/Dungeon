package contrib.debug.systems;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import contrib.debug.draw.DebugEntityInfoFormatter;
import core.Entity;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.SoundComponent;
import core.components.VelocityComponent;
import core.utils.Direction;
import core.utils.Point;
import core.utils.Vector2;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class DebugEntityInfoFormatterTest {

  private final DebugEntityInfoFormatter formatter = new DebugEntityInfoFormatter();

  @Test
  void formatIncludesAvailableDiagnosticLines() {
    Locale previousLocale = Locale.getDefault();
    Locale.setDefault(Locale.GERMANY);

    try {
      Entity entity = new Entity("Debug Wolf");
      PositionComponent positionComponent =
          new PositionComponent(new Point(12.345f, -7.89f), Direction.LEFT);
      VelocityComponent velocityComponent = new VelocityComponent();
      velocityComponent.currentVelocity(Vector2.of(1.25f, -3.5f));

      HealthComponent healthComponent = new HealthComponent(20);
      healthComponent.godMode(true);
      healthComponent.currentHealthpoints(0);

      AIComponent aiComponent = new AIComponent(entity1 -> {}, entity1 -> {}, entity1 -> false);
      aiComponent.active(false);

      entity.add(positionComponent);
      entity.add(velocityComponent);
      entity.add(healthComponent);
      entity.add(new SoundComponent());
      entity.add(new InventoryComponent(3));
      entity.add(aiComponent);
      entity.add(new PlayerComponent(false, "NetPlayer"));

      List<String> actualLines =
          formatter.format(entity, positionComponent, false).lines().toList();

      assertIterableEquals(
          List.of(
              entity.name() + " (" + entity.id() + ")",
              "Position: (12.35, -7.89); LEFT",
              "Velocity: (1.25, -3.50)",
              "Health: 1/20 (GOD)",
              "Sound Instances: 0",
              "Inventory: 0/3 items",
              "AI State: Inactive",
              "Player: NetPlayer (REMOTE)"),
          actualLines);
    } finally {
      Locale.setDefault(previousLocale);
    }
  }

  @Test
  void formatAppendsSortedComponentsWhenRequested() {
    Entity entity = new Entity("Inspector");
    PositionComponent positionComponent = new PositionComponent(new Point(1f, 2f), Direction.UP);

    entity.add(new PlayerComponent());
    entity.add(new HealthComponent());
    entity.add(positionComponent);

    List<String> actualLines = formatter.format(entity, positionComponent, true).lines().toList();

    assertIterableEquals(
        List.of(
            "Components:", "  - HealthComponent", "  - PlayerComponent", "  - PositionComponent"),
        actualLines.subList(actualLines.size() - 4, actualLines.size()));
  }
}
