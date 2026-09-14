package rooms.systemRecovery.riddles;

import engine.Entity;
import engine.utils.Point;
import feature.components.CharacterClassComponent;
import feature.systems.PositionSync;
import rooms.lasthour.starter.LastHourClient;

/** Shared presentation helpers; puzzle state stays in its owning riddle. */
final class RiddleSupport {
  private RiddleSupport() {}

  static void moveSortEntity(Entity entity, Point point) {
    entity
        .fetch(engine.components.PositionComponent.class)
        .ifPresent(position -> position.position(point));
    PositionSync.syncPosition(entity);
  }

  /** Returns the first available named point, allowing old editor spellings during migration. */
  static Point point(engine.level.DungeonLevel level, String... names) {
    for (String name : names) {
      try {
        return level.getPoint(name);
      } catch (RuntimeException ignored) {
        // Try the next alias. Older level files contain a few misspelled point names.
      }
    }
    throw new IllegalArgumentException(
        "None of the named points exists: " + String.join(", ", names));
  }

  static String portraitPathFor(Entity player) {
    return player
        .fetch(CharacterClassComponent.class)
        .map(CharacterClassComponent::characterClass)
        .map(
            characterClass ->
                switch (characterClass) {
                  case THE_LAST_HOUR_CHAR03 -> LastHourClient.CHAR03_PORTRAIT_PATH;
                  case THE_LAST_HOUR_ROGUE -> LastHourClient.ROGUE_PORTRAIT_PATH;
                  default -> "other/unknown.png";
                })
        .orElse("other/unknown.png");
  }
}
