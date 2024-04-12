package dojo.rooms.level_4;

import contrib.components.*;
import contrib.entities.AIFactory;
import contrib.item.concreteItem.ItemResourceBerry;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.utils.components.interaction.DropItemsInteraction;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.SimpleIPath;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import java.io.IOException;
import java.util.Random;
import java.util.Set;

/**
 * Informationen für den Spieler über diesen Raum:
 *
 * <p>In diesem Raum müssen alle Monster erledigt werden, um weiterzukommen.
 */
public class L4_R1_Monster_3 extends Room {

  private int monsterGroupsSpawned = 0;
  private int activeMonsters = 0;

  public L4_R1_Monster_3(
      LevelRoom levelRoom,
      RoomGenerator gen,
      Room nextRoom,
      LevelSize levelSize,
      DesignLabel designLabel) {
    super(levelRoom, gen, nextRoom, levelSize, designLabel);

    try {
      generate();
    } catch (IOException e) {
      throw new RuntimeException(
          "Failed to generate: " + getClass().getName() + ": " + e.getMessage(), e);
    }
  }

  public void generate() throws IOException {
    spawnNextMonsterGroup();
  }

  private void spawnNextMonsterGroup() {
    if (monsterGroupsSpawned >= 4) {
      // ~ 10 monsters in total are spawned (4 groups, each with ~ 2.5 monsters):
      if (activeMonsters == 0) {
        // All monsters died:
        openDoors();
        regeneratePlayersHealth();
      }
      return;
    }
    monsterGroupsSpawned++;
    Random r = new Random();
    // 1-4 monsters in this group
    int monstersToSpawn = r.nextInt(4) + 1;
    activeMonsters += monstersToSpawn;
    for (int i = 0; i < monstersToSpawn; i++) {
      // 50 % chance of imp or daemon
      int monsterType = r.nextInt(2);
      switch (monsterType) {
        case 0:
          spawnNewImp();
          break;
        case 1:
          spawnNewDaemon();
          break;
      }
    }
  }

  private void spawnNewImp() {
    Entity monster1 = new Entity("imp");

    InventoryComponent ic = new InventoryComponent(1);
    monster1.add(ic);
    ic.add(new ItemResourceBerry());

    monster1.add(
        new HealthComponent(
            15,
            (e) -> {
              activeMonsters--;

              // Drop items
              new DropItemsInteraction().accept(e, null);

              // Increase player health
              healPlayerIfNecessary();

              // Spawn next group
              spawnNextMonsterGroup();
            }));
    monster1.add(new PositionComponent());
    monster1.add(new AIComponent(AIFactory.randomFightAI(), AIFactory.randomIdleAI(), e -> true));
    try {
      monster1.add(new DrawComponent(new SimpleIPath("character/monster/imp")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    monster1.add(new VelocityComponent(8, 8));
    monster1.add(new CollideComponent());

    addEntityImmediately(monster1);
  }

  private void spawnNewDaemon() {
    Entity monster1 = new Entity("daemon");

    InventoryComponent ic = new InventoryComponent(1);
    monster1.add(ic);
    ic.add(new ItemResourceBerry());

    monster1.add(
        new HealthComponent(
            30,
            (e) -> {
              activeMonsters--;

              // Drop items
              new DropItemsInteraction().accept(e, null);

              // Increase player health
              healPlayerIfNecessary();

              // Spawn next group
              spawnNextMonsterGroup();
            }));
    monster1.add(new PositionComponent());
    monster1.add(new AIComponent(AIFactory.randomFightAI(), AIFactory.randomIdleAI(), e -> true));
    try {
      monster1.add(new DrawComponent(new SimpleIPath("character/monster/big_deamon")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    monster1.add(new VelocityComponent(4, 4));
    monster1.add(new CollideComponent());

    addEntityImmediately(monster1);
  }

  private void healPlayerIfNecessary() {
    HealthComponent hc =
        Game.entityStream(Set.of(PlayerComponent.class, HealthComponent.class))
            .findFirst()
            .orElseThrow()
            .fetch(HealthComponent.class)
            .orElseThrow();
    if (hc.currentHealthpoints() < hc.maximalHealthpoints() / 2) {
      // Increase player health by 10 when his health is below 50 %
      hc.currentHealthpoints(hc.currentHealthpoints() + 10);
    }
  }

  private void regeneratePlayersHealth() {
    HealthComponent hc =
        Game.entityStream(Set.of(PlayerComponent.class, HealthComponent.class))
            .findFirst()
            .orElseThrow()
            .fetch(HealthComponent.class)
            .orElseThrow();
    hc.currentHealthpoints(hc.maximalHealthpoints());
  }
}
