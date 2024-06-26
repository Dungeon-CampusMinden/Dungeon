package dojo.rooms.rooms.riddle;

import contrib.components.*;
import contrib.entities.AIFactory;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.item.concreteItem.ItemResourceBerry;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.utils.components.draw.ChestAnimations;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
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
import dojo.compiler.DojoCompiler;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import dojo.rooms.TaskRoom;
import java.io.IOException;
import java.util.Set;
import studenttasks.cuboid.Cuboid;

/**
 * Informationen für den Spieler über diesen Raum:
 *
 * <p>In diesem Raum muss eine Klasse mit mathematischen Funktionen verbessert werden. Der Spieler
 * hat jedoch nur eine begrenzte Anzahl an Versuchen, symbolisiert durch seine Lebenspunkte. Wenn
 * die Klasse richtig verbessert wurde, gilt der Imp als besiegt und der Spieler kann in den
 * nächsten Raum weitergehen.
 */
public class QuaderRoom extends TaskRoom {
  private static final Class<?> CLASS_TO_TEST = Cuboid.class;
  private static final String CLASS_TO_TEST_FQ_NAME = CLASS_TO_TEST.getName();
  private static final String PATH_TO_TEST_CLASS = "src/studenttasks/cuboid/";
  private static final String FRIENDLY_NAME =
      PATH_TO_TEST_CLASS + CLASS_TO_TEST.getSimpleName() + ".java";

  private int impHealth = 10;

  /**
   * Generate a new room.
   *
   * @param levelRoom the level node
   * @param gen the room generator
   * @param nextRoom the rooms next room
   * @param levelSize the size of this room
   * @param designLabel the design label of this room
   */
  public QuaderRoom(
      LevelRoom levelRoom,
      RoomGenerator gen,
      Room nextRoom,
      LevelSize levelSize,
      DesignLabel designLabel) {
    super(levelRoom, gen, nextRoom, levelSize, designLabel);

    try {
      generate();
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to generate: " + getClass().getName() + ": " + e.getMessage(), e);
    }
  }

  private void generate() throws IOException {
    addRoomEntities(Set.of(generateChest()));
  }

  private Entity generateChest() throws IOException {
    Entity chest = EntityFactory.newChest();
    chest.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                OkDialog.showOkDialog(
                    "Bitte sprich mit dem Imp, aber besiege ihn noch nicht.",
                    "Mit Imp sprechen",
                    () -> addEntityImmediately(generateImp()))));
    chest.fetch(DrawComponent.class).orElseThrow().currentAnimation(ChestAnimations.OPEN_FULL);
    return chest;
  }

  /**
   * Genrates a new imp.
   *
   * <p>The imp can be talked to.
   *
   * <p>If the imps dies, a new one will be spawned, and the player health will be decreased by 25,
   * and the new imp health will be increased by 10, and a berry item will be dropped.
   *
   * @return the new generated imp.
   */
  private Entity generateImp() {
    Entity monster1 = new Entity("imp");

    InventoryComponent ic = new InventoryComponent(1);
    monster1.add(ic);
    ic.add(new ItemResourceBerry());

    monster1.add(
        new HealthComponent(
            impHealth,
            (e) -> {
              // Test players solution
              DojoCompiler.TestResult testResult =
                  new DojoCompiler()
                      .testMathematicalClass(PATH_TO_TEST_CLASS, CLASS_TO_TEST_FQ_NAME);
              if (testResult.passed()) {
                OkDialog.showOkDialog(
                    "Danke, du hast die Aufgabe gelöst.",
                    "Aufgabe in diesem Raum:",
                    this::openDoors);
              } else {
                OkDialog.showOkDialog(
                    "Leider nicht gelöst. 25 HP verloren. Versuch's noch einmal. ... "
                        + testResult.messages(),
                    "Aufgabe in diesem Raum:",
                    () -> {});
              }

              // Decrease player health
              Game.entityStream(Set.of(PlayerComponent.class, HealthComponent.class))
                  .findFirst()
                  .orElseThrow()
                  .fetch(HealthComponent.class)
                  .orElseThrow()
                  .receiveHit(new Damage(25, DamageType.MAGIC, null));

              // Increase new imp health
              new DropItemsInteraction().accept(e, null);
              impHealth += 10;
              addEntityImmediately(generateImp());
            }));
    monster1.add(new PositionComponent());
    monster1.add(new AIComponent(AIFactory.randomFightAI(), AIFactory.randomIdleAI(), e -> false));
    try {
      monster1.add(new DrawComponent(new SimpleIPath("character/monster/imp")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    monster1.add(new VelocityComponent(4, 4));
    monster1.add(new CollideComponent());

    // Tell the tasks of this room ...
    monster1.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                OkDialog.showOkDialog(
                    "Finde und verbessere die Fehler in der Klasse: " + FRIENDLY_NAME,
                    "Aufgabe in diesem Raum:",
                    () -> {
                      OkDialog.showOkDialog(
                          "Besiege mich, wenn du das fertig hast.",
                          "Aufgabe in diesem Raum:",
                          () -> {});
                    })));

    return monster1;
  }
}
