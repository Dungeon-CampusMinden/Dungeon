package systems;

import static utils.EntityUtils.getHeroCoordinate;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.utils.Coordinate;
import core.utils.components.MissingComponentException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import level.utils.Teleporter;
import utils.EntityUtils;

/**
 * This class represents a system that handles teleportation in the game. It keeps track of all
 * teleporters in the game, the last position of the hero, and whether the hero has just teleported.
 */
public class TeleporterSystem extends System {

  // A set of all teleporters in the game.
  private final Set<Teleporter> teleporters = new HashSet<>();

  private Coordinate lastHeroPos = new Coordinate(0, 0);

  // A flag indicating whether the hero has just teleported.
  private boolean justTeleported = false;

  /**
   * Executes the teleportation logic. If the hero has moved and is on a teleporter, they are
   * teleported to the teleporter's destination. The hero cannot be teleported immediately after a
   * teleportation to prevent back-and-forth teleportation.
   */
  @Override
  public void execute() {
    if (teleporters.isEmpty()) {
      return;
    }

    if (!heroMoved()) return; // Only consider teleporting if the hero has moved
    this.lastHeroPos = EntityUtils.getHeroCoordinate();
    if (lastHeroPos == null) {
      return;
    }
    Coordinate destination = null;
    for (Teleporter teleporter : teleporters) {
      destination = teleporter.getCurrentDestination(lastHeroPos);
      if (destination != null) {
        break;
      }
    }
    if (destination == null) {
      this.justTeleported = false;
      return;
    }

    if (!justTeleported) { // Prevent teleporting back and forth
      teleportHero(destination);
    }
  }

  /**
   * Checks if the hero has moved since the last execution of the system.
   *
   * @return True if the hero has moved, false otherwise.
   */
  private boolean heroMoved() {
    return lastHeroPos != null && !lastHeroPos.equals(getHeroCoordinate());
  }

  /**
   * Teleports the hero to the specified destination. The hero's position is updated and the
   * justTeleported flag is set to true.
   *
   * @param destination The destination to teleport the hero to.
   */
  private void teleportHero(Coordinate destination) {
    Entity hero = Game.hero().orElse(null);
    if (hero == null) {
      return;
    }
    PositionComponent heroPosition =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    heroPosition.position(destination.toPoint());
    this.justTeleported = true;
  }

  /**
   * Registers one or more teleporters with the system.
   *
   * @param teleporter The teleporter(s) to register.
   */
  public void registerTeleporter(Teleporter... teleporter) {
    Collections.addAll(teleporters, teleporter);
  }

  /**
   * Removes a teleporter from the system.
   *
   * @param teleporter The teleporter to remove.
   */
  public void removeTeleporter(Teleporter teleporter) {
    teleporters.remove(teleporter);
  }

  /**
   * Returns a collection of all teleporters registered with the system.
   *
   * @return A collection of all registered teleporters.
   */
  public Collection<Teleporter> teleporter() {
    return teleporters;
  }

  /** Removes all teleporters from the system. */
  public void clearTeleporters() {
    teleporters.clear();
  }
}
