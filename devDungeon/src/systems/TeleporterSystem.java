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

public class TeleporterSystem extends System {

  private static TeleporterSystem INSTANCE;

  private final Set<Teleporter> teleporters = new HashSet<>();
  private Coordinate lastHeroPos = new Coordinate(0, 0);
  private Coordinate justTeleportedFrom = new Coordinate(0, 0);

  private TeleporterSystem() {}

  public static TeleporterSystem getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new TeleporterSystem();
    }
    return INSTANCE;
  }

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
    if (destination != null
        && !destination.equals(this.justTeleportedFrom)) { // Prevent teleporting back and forth
      this.teleportHero(destination);
    } else {
      this.justTeleportedFrom = new Coordinate(0, 0);
    }
  }

  private boolean heroMoved() {
    return this.lastHeroPos != null && !this.lastHeroPos.equals(getHeroCoordinate());
  }

  private void teleportHero(Coordinate destination) {
    Entity hero = Game.hero().orElse(null);
    if (hero == null) {
      return;
    }
    PositionComponent heroPosition =
        hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));
    heroPosition.position(destination.toPoint());
    this.justTeleportedFrom = this.lastHeroPos;
  }

  public void registerTeleporter(Teleporter... teleporter) {
    Collections.addAll(this.teleporters, teleporter);
  }

  public void removeTeleporter(Teleporter teleporter) {
    this.teleporters.remove(teleporter);
  }

  public Collection<Teleporter> teleporter() {
    return this.teleporters;
  }
}
