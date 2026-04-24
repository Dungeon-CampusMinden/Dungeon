package contrib.debug.systems;

import contrib.components.AIComponent;
import contrib.components.HealthComponent;
import contrib.components.InventoryComponent;
import core.Entity;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.SoundComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Formats the hover information shown by {@link DebugEntityRenderSystem}. */
final class DebugEntityInfoFormatter {

  String format(Entity entity, PositionComponent positionComponent, boolean includeComponents) {
    Point position = positionComponent.position();

    StringBuilder info = new StringBuilder();
    info.append(entity.name()).append(" (").append(entity.id()).append(")\n");
    info.append("Position: (")
      .append(formatFloat(position.x()))
      .append(", ")
      .append(formatFloat(position.y()))
      .append("); ")
      .append(positionComponent.viewDirection())
      .append("\n");

    entity
      .fetch(VelocityComponent.class)
      .ifPresent(
        velocityComponent -> {
          String velocity =
            "("
              + formatFloat(velocityComponent.currentVelocity().x())
              + ", "
              + formatFloat(velocityComponent.currentVelocity().y())
              + ")";
          info.append("Velocity: ").append(velocity).append("\n");
        });

    entity
      .fetch(HealthComponent.class)
      .ifPresent(
        healthComponent ->
          info.append("Health: ")
            .append(healthComponent.currentHealthpoints())
            .append("/")
            .append(healthComponent.maximalHealthpoints())
            .append(healthComponent.isDead() ? " (DEAD)" : "")
            .append(healthComponent.godMode() ? " (GOD)" : "")
            .append("\n"));

    entity
      .fetch(DrawComponent.class)
      .ifPresent(
        drawComponent ->
          info.append("Animation State: ")
            .append(drawComponent.currentStateName())
            .append(" (")
            .append(drawComponent.currentState().getData())
            .append(")\n"));

    entity
      .fetch(SoundComponent.class)
      .ifPresent(
        soundComponent ->
          info.append("Sound Instances: ").append(soundComponent.sounds().size()).append("\n"));

    entity
      .fetch(InventoryComponent.class)
      .ifPresent(
        inventoryComponent ->
          info.append("Inventory: ")
            .append(Arrays.stream(inventoryComponent.items()).filter(Objects::nonNull).count())
            .append("/")
            .append(inventoryComponent.items().length)
            .append(" items\n"));

    entity
      .fetch(AIComponent.class)
      .ifPresent(
        aiComponent ->
          info.append("AI State: ")
            .append(aiComponent.active() ? "Active" : "Inactive")
            .append("\n"));

    entity
      .fetch(PlayerComponent.class)
      .ifPresent(
        playerComponent ->
          info.append("Player: ")
            .append(playerComponent.playerName())
            .append(playerComponent.isLocal() ? " (LOCAL)" : " (REMOTE)")
            .append("\n"));

    if (includeComponents) {
      List<String> componentNames =
        entity
          .componentStream()
          .map(component -> component.getClass().getSimpleName())
          .sorted(String::compareToIgnoreCase)
          .toList();

      if (!componentNames.isEmpty()) {
        info.append("Components:\n");
        for (String componentName : componentNames) {
          info.append("  - ").append(componentName).append("\n");
        }
      }
    }

    return info.toString().trim();
  }

  private static String formatFloat(float value) {
    return String.format(Locale.ROOT, "%.2f", value);
  }
}
