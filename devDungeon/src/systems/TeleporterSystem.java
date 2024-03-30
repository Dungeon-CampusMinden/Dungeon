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
 * The system is implemented as a singleton, meaning there can only be one instance of it in the
 * game.
 */
public class TeleporterSystem extends System {

  // The single instance of the TeleporterSystem.
  private static TeleporterSystem INSTANCE;

  // A set of all teleporters in the game.
  private final Set<Teleporter> teleporters = new HashSet<>();

  private Coordinate lastHeroPos = new Coordinate(0, 0);

  // A flag indicating whether the hero has just teleported.
  private boolean justTeleported = false;

  // Private constructor to enforce the singleton pattern.
  private TeleporterSystem() {}

  /**
   * Returns the single instance of the TeleporterSystem. If the instance does not exist, it is
   * created.
   *
   * @return The single instance of the TeleporterSystem.
   */
  public static TeleporterSystem getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new TeleporterSystem();
    }
    return INSTANCE;
  }

  /**
   * Executes the teleportation logic. If the hero has moved and is on a teleporter, they are
   * teleported to the teleporter's destination. The hero cannot be teleported immediately after a
   * teleportation to prevent back-and-forth teleportation.
   */
  @Override
  public void execute() {
    if (this.teleporters.isEmpty()) {
      return;
    }

    if (!this.heroMoved()) return; // Only consider teleporting if the hero has moved
    this.lastHeroPos = EntityUtils.getHeroCoordinate();
    if (this.lastHeroPos == null) {
      return;
    }
    Coordinate destination = null;
    for (Teleporter teleporter : this.teleporters) {
      destination = teleporter.getCurrentDestination(this.lastHeroPos);
      if (destination != null) {
        break;
      }
    }
    if (destination == null) {
      this.justTeleported = false;
      return;
    }

    if (!this.justTeleported) { // Prevent teleporting back and forth
      this.teleportHero(destination);
    }
  }

  /**
   * Checks if the hero has moved since the last execution of the system.
   *
   * @return True if the hero has moved, false otherwise.
   */
  private boolean heroMoved() {
    return this.lastHeroPos != null && !this.lastHeroPos.equals(getHeroCoordinate());
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
    Collections.addAll(this.teleporters, teleporter);
  }

  /**
   * Removes a teleporter from the system.
   *
   * @param teleporter The teleporter to remove.
   */
  public void removeTeleporter(Teleporter teleporter) {
    this.teleporters.remove(teleporter);
  }

  /**
   * Returns a collection of all teleporters registered with the system.
   *
   * @return A collection of all registered teleporters.
   */
  public Collection<Teleporter> teleporter() {
    return this.teleporters;
  }

  /** Removes all teleporters from the system. */
  public void clearTeleporters() {
    this.teleporters.clear();
  }
}
