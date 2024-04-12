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
import java.io.IOException;

public class MyImp {
  public MyImp(Entity entity, boolean isInitialized) {
    if (!isInitialized) {
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
                      () -> new DropItemsInteraction().accept(e, null))));

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
                      "Du findest meine Implementierung in \"" + getClass().getName() + "\".",
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
    }

    addPotionHealthToInventory(entity);
    decreaseImpHealthTo25(entity);
    toggleImpGodMode(entity);
    setImpMoveable(entity);
  }

  private void addPotionHealthToInventory(Entity myImp) {
    // todo
  }

  private void decreaseImpHealthTo25(Entity myImp) {
    // todo
  }

  private void toggleImpGodMode(Entity myImp) {
    // todo
  }

  private void setImpMoveable(Entity myImp) {
    // todo
  }
}
