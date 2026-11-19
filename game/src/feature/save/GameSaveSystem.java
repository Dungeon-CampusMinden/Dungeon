package feature.save;

import engine.Entity;
import engine.Game;
import engine.System;
import engine.components.PlayerComponent;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Server system of the {@link GameSaves save system}: restores joining players from the loaded save
 * and saves the game when it progressed.
 */
final class GameSaveSystem extends System {

  // Joined players whose saved state is not restored yet.
  private final Queue<Entity> joined = new ConcurrentLinkedQueue<>();

  GameSaveSystem() {
    super(PlayerComponent.class);
    onEntityAdd = joined::add;
  }

  @Override
  public void execute() {
    // A player whose components are not set up yet is retried in the next tick.
    for (int pending = joined.size(); pending > 0; pending--) {
      Entity player = joined.poll();
      if (player == null) break;
      if (Game.findEntityById(player.id()).orElse(null) != player) continue;
      if (!GameSaves.restorePlayer(player)) joined.add(player);
    }
    GameSaves.saveIfProgressed();
  }
}
