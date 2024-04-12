import core.Entity;
import core.components.DrawComponent;
import dojo.rooms.Room;
import java.io.IOException;

public class Monster {
  /**
   * TODO:
   *
   * <p>Store the current room in the class.
   *
   * @param currentRoom the current room in dojo dungeon.
   */
  public Monster(Room currentRoom) {}

  /**
   * TODO:
   *
   * <p>Assign the health parameter to the HealthComponent of the monster and add the functionality
   * that the door from the current room to the next opens after the monster has been beaten.
   *
   * <p>Assign the position of the start tile of the current room to the PositionComponent of the
   * monster (you can use "pc.position(currentRoom.getStartTile())").
   *
   * <p>Set the AIComponent of the monster to a random fight AI.
   *
   * <p>Set the DrawComponent of the monster to the textureComponent parameter.
   *
   * <p>Assign the speed parameter to the VelocityComponent of the monster.
   *
   * <p>Assign a new CollideComponent to the Monster.
   *
   * @param textureComponent the texture component to be added to the monster.
   * @param health the health of the monster.
   * @param speed the speed of the monster.
   * @return the monster to be spawned.
   * @throws IOException if something goes wrong.
   */
  public Entity spawnMonster(DrawComponent textureComponent, int health, float speed)
      throws IOException {
    Entity monster = new Entity();

    // TODO: ...

    return monster;
  }
}
