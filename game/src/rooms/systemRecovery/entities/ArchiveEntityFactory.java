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

/** Builds the bookshelves and combined archive data modules used by riddle 7. */
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
    Entity entity =
        DecoFactory.createDeco(point.translate(-0.15f, -0.35f), Deco.DigitalArchiveShelf);
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
   * @param active whether the module represents an active archive node
   * @return configured archive data display
   */
  public static Entity archiveDataDisplay(Point point, String name, String text, boolean active) {
    Entity entity = new Entity(name);
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    DrawComponent draw = new DrawComponent(new SimpleIPath("objects/tech/Screen_info_3.png"));
    draw.tintColor(active ? 0x33FF66FF : 0xFF3333FF);
    entity.add(draw);
    entity.add(new DisplayTextComponent(text));
    entity.add(
        new InteractionComponent(
            new Interaction(
                (_, who) ->
                    DialogUtils.showTextPopup(
                        text, SystemRecoveryText.key("world.archive.title"), who.id()))));
    return entity;
  }
}
