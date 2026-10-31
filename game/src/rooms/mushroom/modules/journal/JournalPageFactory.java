package rooms.mushroom.modules.journal;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.components.InventoryComponent;
import feature.hud.DialogUtils;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import rooms.mushroom.Sounds;

/** Factory class for creating journal page entities. */
public class JournalPageFactory {

  private static final String TEXTURE_PATH = "items/rpg/item_paper.png";

  /**
   * Creates a journal page entity at the specified position.
   *
   * @param position the position to place the journal page
   * @param unlockPage called when a player holding a {@link JournalItem} picks up the page
   * @return the created journal page entity
   */
  public static Entity createJournalPage(Point position, Runnable unlockPage) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(position));
    entity.add(new DrawComponent(new SimpleIPath(TEXTURE_PATH)));
    entity.add(
        new InteractionComponent(
            new Interaction((page, player) -> handlePickup(page, player, unlockPage))));
    return entity;
  }

  private static void handlePickup(Entity journalPage, Entity player, Runnable unlockPage) {
    player
        .fetch(InventoryComponent.class)
        .ifPresent(
            inventory -> {
              inventory
                  .itemOfClass(JournalItem.class)
                  .ifPresentOrElse(
                      item -> {
                        unlockPage.run();
                        Game.remove(journalPage);
                        DialogUtils.showTextPopup(
                            "Auf dieser Seite ist die Beschreibung von zwei Pilzen! Ich schreibe das mal in mein Notizbuch...",
                            "Yay");
                        Sounds.KEY_ITEM_PICKUP_SOUND.play();
                      },
                      () -> {
                        DialogUtils.showTextPopup(
                            "Ohh, hilfreiche Informationen zu Pilzen. Nur kann ich sie mir nicht merken, und etwas zum Aufschreiben habe ich leider auch nicht...",
                            ":pensive:");
                      });
            });
  }
}
