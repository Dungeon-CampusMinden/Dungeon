package dojo.monster;

import contrib.components.*;
import contrib.entities.AIFactory;
import contrib.hud.dialogs.OkDialog;
import contrib.utils.components.interaction.DropItemsInteraction;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.components.path.SimpleIPath;
import dojo.rooms.Room;
import java.io.IOException;

/** Class to create and initialize the entity "MyImp". */
public class MyImp {
  /**
   * Creates and initializes a new {@link Entity} "MyImp".
   *
   * @param currentRoom the current room the entity is placed in.
   * @return the new entity.
   */
  public static Entity createEntity(Room currentRoom) {
    Entity entity = new Entity();

    entity.name("MyImp");

    InventoryComponent ic = new InventoryComponent(5);
    entity.add(ic);

    entity.add(
        new HealthComponent(
            1000,
            (e) ->
                OkDialog.showOkDialog(
                    "Danke, du hast das Dojo-Dungeon gelöst!",
                    "Alle Aufgaben gelöst:",
                    () -> {
                      new DropItemsInteraction().accept(e, null);
                      currentRoom.openDoors();
                    })));
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
                    "Du findest meine Implementierung in \"" + MyImp.class.getName() + "\".",
                    "Aufgabe in diesem Raum:",
                    () ->
                        OkDialog.showOkDialog(
                            "Implementiere die Methoden addPotionHealthToInventory(), decreaseImpHealthTo25(), toggleImpGodMode() und setImpMoveable().",
                            "Aufgabe in diesem Raum:",
                            () ->
                                OkDialog.showOkDialog(
                                    "Sprich dann mit der Truhe, um mich erneut zu laden, und greife an!",
                                    "Aufgabe in diesem Raum:",
                                    () -> {})))));

    return entity;
  }

  /**
   * Static method to modify the entity MyImp.
   *
   * <p>Warning: Don't modify this method.
   *
   * @param myImp the entity to modify
   */
  public static void modifyMyImp(Entity myImp) {
    addPotionHealthToInventory(myImp);
    decreaseImpHealthTo25(myImp);
    toggleImpGodMode(myImp);
    setImpMoveable(myImp);
  }

  /**
   * Modifies the inventory of MyImp.
   *
   * @param myImp the entity to modify
   */
  public static void addPotionHealthToInventory(Entity myImp) {
    // todo
  }

  /**
   * Sets the health of MyImp to 25.
   *
   * @param myImp the entity to modify
   */
  public static void decreaseImpHealthTo25(Entity myImp) {
    // todo
  }

  /**
   * Sets the god mode of MyImp to false.
   *
   * @param myImp the entity to modify
   */
  public static void toggleImpGodMode(Entity myImp) {
    // todo
  }

  /**
   * Makes MyImp moveable (increases its speed).
   *
   * @param myImp the entity to modify
   */
  public static void setImpMoveable(Entity myImp) {
    // todo
  }
}
