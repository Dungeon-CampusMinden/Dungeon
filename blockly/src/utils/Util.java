package utils;

import components.AmmunitionComponent;
import contrib.level.DevDungeonLoader;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import entities.HeroTankControlledFactory;
import java.io.IOException;
import java.util.Set;

/**
 * The {@code Util} class provides utility methods to manage the player's hero entity and control
 * the game state, such as restarting the game or spawning the hero.
 *
 * <p>This class includes methods to:
 *
 * <ul>
 *   <li>Create a new hero entity with player controls and ammunition
 *   <li>Restart the game by resetting entities and reloading the level
 * </ul>
 */
public class Util {

  /**
   * Creates and adds a new hero entity to the game.
   *
   * <p>Any existing entities with a {@link PlayerComponent} will first be removed. The new hero is
   * generated using the {@link HeroTankControlledFactory} and is equipped with an {@link
   * AmmunitionComponent}.
   *
   * @throws RuntimeException if an {@link IOException} occurs during hero creation
   */
  public static void createHero() {
    Game.entityStream(Set.of(PlayerComponent.class)).forEach(e -> Game.remove(e));
    Entity hero;
    try {
      hero = HeroTankControlledFactory.newTankControlledHero();
      hero.add(new AmmunitionComponent());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    Game.add(hero);
  }

  /**
   * Restarts the game by removing all entities, recreating the hero, and reloading the current
   * level.
   *
   * <p>This effectively resets the game state to its initial configuration.
   */
  public static void restart() {
    Game.removeAllEntities();
    createHero();
    DevDungeonLoader.reloadCurrentLevel();
  }
}
