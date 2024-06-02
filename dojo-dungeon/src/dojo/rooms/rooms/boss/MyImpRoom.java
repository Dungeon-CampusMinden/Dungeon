package dojo.rooms.rooms.boss;

import contrib.components.*;
import contrib.entities.AIFactory;
import contrib.entities.EntityFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.level.generator.graphBased.RoomGenerator;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.path.SimpleIPath;
import dojo.compiler.DojoCompiler;
import dojo.rooms.LevelRoom;
import dojo.rooms.Room;
import java.io.IOException;
import java.util.*;
import javax.tools.*;
import studentTasks.modifyEntities.ModifyEntities;

/**
 * Informationen für den Spieler über diesen Raum:
 *
 * <p>In diesem Raum erhält man eine Beschreibung des erwarteten Verhaltens, die implementiert
 * werden muss. Der Dämon erteilt die Aufgabe, Methoden zu schreiben, die dieses Verhalten
 * implementieren. Danach muss der Dämon angegriffen werden.
 */
public class MyImpRoom extends Room {
  private static final Class<?> CLASS_TO_TEST = ModifyEntities.class;
  private static final String CLASS_TO_TEST_FQ_NAME = CLASS_TO_TEST.getName();
  private static final String PATH_TO_TEST_CLASS = "src/studentTasks/modifyEntities/";
  private static final String FRIENDLY_NAME =
      PATH_TO_TEST_CLASS + CLASS_TO_TEST.getSimpleName() + ".java";

  /**
   * Generate a new room.
   *
   * @param levelRoom the level node
   * @param gen the room generator
   * @param nextRoom the rooms next room
   * @param levelSize the size of this room
   * @param designLabel the design label of this room
   */
  public MyImpRoom(
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

  private void generate() throws IOException {
    final Entity myImp = createEntityMyImp(this);

    final Entity chest = EntityFactory.newChest();
    chest.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) -> {
              try {
                Class<?> cls =
                    DojoCompiler.compileClassDependentOnOthers(
                        PATH_TO_TEST_CLASS, CLASS_TO_TEST_FQ_NAME);
                cls.getDeclaredMethod("disableGodMode", Entity.class).invoke(null, myImp);
                cls.getDeclaredMethod("setHealthTo25", Entity.class).invoke(null, myImp);
                cls.getDeclaredMethod("increaseSpeed", Entity.class).invoke(null, myImp);
                cls.getDeclaredMethod("addNewHealthPotionToInventory", Entity.class)
                    .invoke(null, Game.hero().orElseThrow());
                OkDialog.showOkDialog("Die Entitäten wurden angepasst.", "Ok:", () -> {});
              } catch (Exception e) {
                OkDialog.showOkDialog(e.getMessage(), "Fehler:", () -> {});
              }
            }));

    addRoomEntities(Set.of(myImp, chest));
  }

  private static Entity createEntityMyImp(Room currentRoom) {
    Entity entity = new Entity();

    entity.name("MyImp");

    InventoryComponent ic = new InventoryComponent(5);
    entity.add(ic);

    entity.add(
        new HealthComponent(
            1000,
            (e) ->
                OkDialog.showOkDialog(
                    "Danke, du hast die Aufgabe in diesem Raum gelöst!",
                    "Aufgabe gelöst:",
                    currentRoom::openDoors)));
    entity.fetch(HealthComponent.class).orElseThrow().godMode(true);

    entity.add(new PositionComponent());

    entity.add(new AIComponent(AIFactory.randomFightAI(), AIFactory.randomIdleAI(), e -> false));

    try {
      entity.add(new DrawComponent(new SimpleIPath("character/monster/imp")));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    entity.add(new VelocityComponent(0, 0));

    entity.add(new CollideComponent());

    // Tell the tasks of this room ...
    entity.add(
        new InteractionComponent(
            1,
            true,
            (entity1, entity2) ->
                OkDialog.showOkDialog(
                    "Du findest eine Klasse zum Anpassen in " + FRIENDLY_NAME + ".",
                    "Aufgabe in diesem Raum:",
                    () ->
                        OkDialog.showOkDialog(
                            "Implementiere die Methoden disableGodMode(), setHealthTo25(), increaseSpeed() und addNewHealthPotionToInventory().",
                            "Aufgabe in diesem Raum:",
                            () ->
                                OkDialog.showOkDialog(
                                    "Sprich dann mit der Truhe, um mich erneut zu laden, und greife an!",
                                    "Aufgabe in diesem Raum:",
                                    () -> {})))));

    return entity;
  }
}
