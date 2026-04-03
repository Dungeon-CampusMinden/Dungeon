package contrib.platform.gdx.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.crafting.CraftingDialogInteraction;
import contrib.hud.inventory.ItemDragPayload;

/**
 * libGDX-specific drag-and-drop binder for the crafting dialog.
 *
 * <p>This class isolates the remaining Scene2D {@link DragAndDrop.Target} construction from
 * {@code CraftingGUI}. The crafting GUI itself only provides the backend-neutral interaction logic
 * and the technical anchor actor required by libGDX.
 */
public final class GdxCraftingDropTargetBinder {

  private GdxCraftingDropTargetBinder() {}

  /**
   * Registers the crafting dialog as a libGDX drag-and-drop target.
   *
   * @param dragAndDrop shared libGDX drag-and-drop instance
   * @param actor technical Scene2D anchor actor
   * @param interaction backend-neutral crafting interaction logic
   */
  public static void bind(
    DragAndDrop dragAndDrop, Actor actor, CraftingDialogInteraction interaction) {
    if (dragAndDrop == null || actor == null || interaction == null) {
      return;
    }

    dragAndDrop.addTarget(
      new DragAndDrop.Target(actor) {
        @Override
        public boolean drag(
          DragAndDrop.Source source,
          DragAndDrop.Payload payload,
          float x,
          float y,
          int pointer) {
          return payload != null
            && payload.getObject() instanceof ItemDragPayload itemDragPayload
            && interaction.acceptsDraggedItem(itemDragPayload);
        }

        @Override
        public void drop(
          DragAndDrop.Source source,
          DragAndDrop.Payload payload,
          float x,
          float y,
          int pointer) {
          if (payload != null
            && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
            interaction.handleDraggedItem(itemDragPayload);
          }
        }
      });
  }
}
