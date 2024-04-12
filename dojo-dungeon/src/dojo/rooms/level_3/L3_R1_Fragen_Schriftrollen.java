package dojo.rooms.level_3;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.components.UIComponent;
import contrib.entities.EntityFactory;
import contrib.entities.WorldItemBuilder;
import contrib.hud.dialogs.OkDialog;
import contrib.hud.elements.GUICombination;
import contrib.hud.inventory.InventoryGUI;
import contrib.item.Item;
import contrib.level.generator.graphBased.RoomGenerator;
import contrib.utils.components.draw.ChestAnimations;
import core.Entity;
import core.components.DrawComponent;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import core.utils.components.draw.Animation;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import dojo.rooms.LevelRoom;
import dojo.rooms.MonsterRoom;
import dojo.rooms.Room;
import java.io.IOException;
import java.util.*;

/**
 * Informationen für den Spieler über diesen Raum:
 *
 * <p>Der Spieler muss Schriftrollen, die verschiedene Programming Patterns und Software Development
 * Principles repräsentieren, den entsprechenden Truhen zuordnen. Die Truhen sind mit den Kategorien
 * der Programming Patterns und Software Development Principles beschriftet. Erst wenn alle
 * Schriftrollen korrekt zugeordnet sind, kann der Spieler in den nächsten Raum weitergehen.
 */
public class L3_R1_Fragen_Schriftrollen extends MonsterRoom {

  HashMap<String, ArrayList<String>> sortables;
  HashMap<Entity, Boolean> doneChests = new HashMap<>();

  public L3_R1_Fragen_Schriftrollen(
      LevelRoom levelRoom,
      RoomGenerator gen,
      Room nextRoom,
      LevelSize levelSize,
      DesignLabel designLabel,
      int monsterCount,
      IPath[] monsterPaths,
      HashMap<String, ArrayList<String>> sortables) {
    super(levelRoom, gen, nextRoom, levelSize, designLabel, monsterCount, monsterPaths);

    this.sortables = sortables;

    try {
      generate();
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to generate: " + getClass().getName() + ": " + e.getMessage(), e);
    }
  }

  private void generate() throws IOException {
    // add entities to room
    Set<Entity> roomEntities = populateMonsters(getMonsterCount(), getMonsterPaths());

    // add chest for each sortable
    for (Map.Entry<String, ArrayList<String>> sortable : sortables.entrySet()) {
      Entity chest = EntityFactory.newChest();
      chest.name(sortable.getKey());

      chest.remove(InventoryComponent.class);
      chest.add(new InventoryComponent());

      InventoryComponent ic = chest.fetch(InventoryComponent.class).orElseThrow();
      chest.add(
          new InteractionComponent(
              1,
              true,
              (interacted, interactor) -> {
                OkDialog.showOkDialog(
                    "Platziere die Schriftrollen die zum Thema "
                        + sortable.getKey()
                        + " passen in die Truhe.",
                    "Schriftrollen sortieren",
                    () -> {
                      interactor
                          .fetch(InventoryComponent.class)
                          .ifPresent(
                              whoIc -> {
                                UIComponent uiComponent =
                                    new UIComponent(
                                        new GUICombination(
                                            new InventoryGUI(whoIc), new InventoryGUI(ic)),
                                        true);
                                uiComponent.onClose(
                                    () -> {
                                      interacted
                                          .fetch(DrawComponent.class)
                                          .ifPresent(
                                              interactedDC -> {
                                                // remove all
                                                // prior
                                                // opened
                                                // animations
                                                interactedDC.deQueueByPriority(
                                                    ChestAnimations.OPEN_FULL.priority());
                                                if (ic.count() > 0) {
                                                  // as long
                                                  // as
                                                  // there is
                                                  // an
                                                  // item
                                                  // inside
                                                  // the chest
                                                  // show a
                                                  // full
                                                  // chest
                                                  interactedDC.queueAnimation(
                                                      ChestAnimations.OPEN_FULL);
                                                } else {
                                                  // empty
                                                  // chest
                                                  // show the
                                                  // empty
                                                  // animation
                                                  interactedDC.queueAnimation(
                                                      ChestAnimations.OPEN_EMPTY);
                                                }
                                              });

                                      // check if all items are sorted
                                      if (ic.count() == sortable.getValue().size()) {
                                        boolean allcorrect = true;
                                        // iterate over items in inventory
                                        for (Item item : ic.items()) {
                                          if (item != null
                                              && !sortable
                                                  .getValue()
                                                  .contains(item.description())) {
                                            allcorrect = false;
                                          }
                                        }

                                        if (allcorrect) {
                                          // all items are sorted correctly
                                          // mark chest as done
                                          doneChests.put(interacted, true);
                                          OkDialog.showOkDialog(
                                              "Gut gemacht!", "Gelöst!", () -> {});
                                        } else {
                                          // not all items are sorted correctly
                                          // mark chest as not done
                                          doneChests.put(interacted, false);
                                          OkDialog.showOkDialog(
                                              "Die Truhe ist noch nicht richtig belegt!",
                                              "Schade!",
                                              () -> {});
                                        }
                                      }

                                      // check if all chests are done
                                      if (doneChests.values().stream()
                                          .allMatch(Boolean::booleanValue)) {
                                        // all chests are done
                                        // open the doors
                                        openDoors();
                                        OkDialog.showOkDialog(
                                            "Alle Truhen sind richtig belegt, die Tür ist offen!",
                                            "Geschafft!",
                                            () -> {});
                                      }
                                    });
                                interactor.add(uiComponent);
                              });
                      interacted
                          .fetch(DrawComponent.class)
                          .ifPresent(
                              interactedDC -> {
                                // only add opening animation when it is not
                                // finished
                                if (interactedDC
                                    .animation(ChestAnimations.OPENING)
                                    .map(animation -> !animation.isFinished())
                                    .orElse(true)) {
                                  interactedDC.queueAnimation(ChestAnimations.OPENING);
                                }
                              });
                    });
              }));
      roomEntities.add(chest);

      // add the sortable items to the room
      for (String item : sortable.getValue()) {
        Item newItem =
            new Item(
                "Schriftrolle",
                item,
                Animation.fromSingleImage(new SimpleIPath("items/resource/cheese.png")));
        Entity itemEntity = WorldItemBuilder.buildWorldItem(newItem);
        roomEntities.add(itemEntity);
      }
    }

    // add the entities as payload to the LevelNode
    addRoomEntities(roomEntities);
  }
}
