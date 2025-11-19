package mushRoom.modules.journal;

import contrib.components.InteractionComponent;
import contrib.components.InventoryComponent;
import contrib.hud.DialogUtils;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.path.SimpleIPath;

import java.util.Set;

public class JournalPageFactory {

  public static Entity createJournalPage(Point position) {
    Entity entity = new Entity();
    entity.add(new PositionComponent(position));
    entity.add(new DrawComponent(new SimpleIPath("objects/mailbox/mailbox_1.png")));
    entity.add(new InteractionComponent(1.5f, true, (e, who) -> {
      who.fetch(InventoryComponent.class).ifPresent(inventory -> {
        inventory.items(JournalItem.class).stream().findFirst().ifPresentOrElse(item -> {
          JournalItem journalItem = (JournalItem) item;
          journalItem.unlockPage();
          Game.remove(e);
          DialogUtils.showTextPopup("Auf dieser Seite ist die Beschreibung von einem Pilz! Ich schreibe das mal in mein Notizbuch...", "Yay");
        }, () -> {
          DialogUtils.showTextPopup("Ohh, hilfreiche Informationen zu Pilzen. Nur kann ich sie mir nicht merken, und etwas zum Schreiben habe ich leider auch nichts...", ":pensive:");
        });
      });
    }));
    return entity;
  }

}
