package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.DialogUtils;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import rooms.systemRecovery.modules.display.DisplayTextComponent;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Builds the bookshelves, archive data display and status lamps used by riddle 7. */
public final class ArchiveEntityFactory {

  private ArchiveEntityFactory() {}

  /**
   * Creates an interactable bookshelf that presents one archive hint.
   *
   * @param point bookshelf position
   * @param name stable entity name
   * @param text hint key shown when the bookshelf is examined
   * @return configured bookshelf entity
   */
  public static Entity archiveBookshelf(Point point, String name, String text) {
    Entity entity = DecoFactory.createDeco(point, Deco.BookshelfLarge);
    entity.name(name);
    entity.remove(DecoComponent.class);
    entity.add(
        new InteractionComponent(
            new Interaction((_, who) -> DialogFactory.showDialogDialog(text, () -> {}, who.id()))));
    return entity;
  }

  /**
   * Creates an interactable display for one archived data node.
   *
   * @param point display position
   * @param name stable entity name
   * @param text data key shown by the display
   * @return configured archive data display
   */
  public static Entity archiveDataDisplay(Point point, String name, String text) {
    Entity entity = new Entity(name);
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(new DrawComponent(new SimpleIPath("objects/tech/Screen_info_3.png")));
    entity.add(new DisplayTextComponent(text));
    entity.add(
        new InteractionComponent(
            new Interaction(
                (_, who) ->
                    DialogUtils.showTextPopup(
                        text, SystemRecoveryText.key("world.archive.title"), who.id()))));
    return entity;
  }

  /**
   * Creates an interactable status lamp for one archived data node.
   *
   * @param point lamp position
   * @param name stable entity name
   * @param index archive node index shown in the popup
   * @param active whether the lamp represents an active state
   * @return configured archive status lamp
   */
  public static Entity archiveStatusLight(Point point, String name, int index, boolean active) {
    Entity entity = new Entity(name);
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    DrawComponent draw = new DrawComponent(new SimpleIPath("objects/tech/Screen_info_3.png"));
    draw.tintColor(active ? 0x33FF66FF : 0xFF3333FF);
    entity.add(draw);
    String statusKey =
        SystemRecoveryText.key(
            active ? "world.archive.status-active" : "world.archive.status-inactive");
    entity.add(
        new InteractionComponent(
            new Interaction(
                (_, who) ->
                    DialogUtils.showTextPopup(
                        SystemRecoveryText.key("world.archive.status", index, statusKey),
                        SystemRecoveryText.key("world.archive.status-title"),
                        who.id()))));
    return entity;
  }
}
