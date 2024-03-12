package entities;

import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.MissingHeroException;
import core.utils.Point;
import core.utils.components.MissingComponentException;
import java.io.IOException;
import java.util.logging.Logger;

public class EntityUtils {

  private static final Logger LOGGER = Logger.getLogger(EntityUtils.class.getName());

  /**
   * Spawns a monster of the given type at the given position and adds it to the game. The Position
   * is cast to a Tile and the monster is spawned at the center of the tile.
   *
   * @param monsterType the type of monster to spawn
   * @param position the position to spawn the monster; the tile at the given point must be
   *     accessible else the monster will not be spawned
   * @throws MissingComponentException if the monster does not have a PositionComponent
   * @throws RuntimeException if an error occurs while spawning the monster
   * @return the spawned monster
   * @see Game#add(Entity)
   * @see MonsterType
   */
  public static Entity spawnMonster(MonsterType monsterType, Point position) {
    Tile tile = Game.tileAT(position);
    if (tile == null || !tile.isAccessible()) {
      LOGGER.warning(
          "Cannot spawn monster at "
              + position
              + " because the tile is not accessible or does not exist");
      return null;
    }
    return spawnMonster(monsterType, tile.coordinate());
  }

  /**
   * Spawns a monster of the given type at the given coordinate and adds it to the game.
   *
   * @param monsterType the type of monster to spawn
   * @param coordinate the coordinate to spawn the monster; the tile at the given coordinate must be
   *     accessible else the monster will not be spawned
   * @throws MissingComponentException if the monster does not have a PositionComponent
   * @throws RuntimeException if an error occurs while spawning the monster
   * @return the spawned monster
   * @see Game#add(Entity)
   * @see MonsterType
   */
  public static Entity spawnMonster(MonsterType monsterType, Coordinate coordinate) {
    Tile tile = Game.tileAT(coordinate);
    if (tile == null || !tile.isAccessible()) {
      LOGGER.warning(
          "Cannot spawn monster at "
              + coordinate
              + " because the tile is not accessible or does not exist");
      return null;
    }
    try {
      Entity newMob = monsterType.buildMonster();
      PositionComponent positionComponent =
          newMob
              .fetch(PositionComponent.class)
              .orElseThrow(() -> MissingComponentException.build(newMob, PositionComponent.class));
      positionComponent.position(tile.position());
      Game.add(newMob);
      return newMob;
    } catch (IOException e) {
      throw new RuntimeException("Error spawning monster", e);
    }
  }

  /**
   * This method is used to spawn a sign entity in the game at a given position. It uses the
   * SignFactory class to create a new sign with the provided text and title. The sign is then added
   * to the game. If an IOException occurs during the creation of the sign, it is caught and a
   * RuntimeException is thrown.
   *
   * @param text The text to be displayed on the sign.
   * @param title The title of the sign.
   * @param pos The position where the sign should be spawned.
   * @return The spawned sign entity.
   * @throws RuntimeException if an error occurs while spawning the sign.
   */
  public static Entity spawnSign(String text, String title, Point pos) {
      Entity sign = SignFactory.createSign(text, title, pos);
      Game.add(sign);
      return sign;
  }

  /**
   * This method is used to get the coordinates of the hero in the game. It uses the SkillTools
   * class to get the hero's position as a point and then converts it to a coordinate. If the hero
   * is missing, it catches the MissingHeroException and returns null.
   *
   * @return Coordinate of the hero's position. If the hero is missing, returns null.
   */
  public static Coordinate getHeroCoords() {
    try {
      return SkillTools.heroPositionAsPoint().toCoordinate();
    } catch (MissingHeroException e) {
      return null;
    }
  }
}
