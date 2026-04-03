package contrib.platform.gdx.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import contrib.hud.inventory.ItemDragPayload;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Small libGDX-only helpers for adapting Scene2D drag-and-drop targets to backend-neutral dialog
 * interaction logic.
 */
public final class GdxDragDropTargets {

  private GdxDragDropTargets() {}

  /**
   * Creates a DragAndDrop target that unwraps {@link ItemDragPayload} and delegates the acceptance
   * and drop behavior to neutral callbacks.
   *
   * @param actor the Scene2D actor used by libGDX for the target
   * @param canAccept neutral acceptance predicate
   * @param onDrop neutral drop handler
   * @return configured libGDX target
   */
  public static DragAndDrop.Target itemPayloadTarget(
    Actor actor,
    Predicate<ItemDragPayload> canAccept,
    Consumer<ItemDragPayload> onDrop) {

    return new DragAndDrop.Target(actor) {
      @Override
      public boolean drag(
        DragAndDrop.Source source,
        DragAndDrop.Payload payload,
        float x,
        float y,
        int pointer) {
        ItemDragPayload itemPayload = extract(payload);
        return itemPayload != null && canAccept.test(itemPayload);
      }

      @Override
      public void drop(
        DragAndDrop.Source source,
        DragAndDrop.Payload payload,
        float x,
        float y,
        int pointer) {
        ItemDragPayload itemPayload = extract(payload);
        if (itemPayload != null && canAccept.test(itemPayload)) {
          onDrop.accept(itemPayload);
        }
      }
    };
  }

  private static ItemDragPayload extract(DragAndDrop.Payload payload) {
    if (payload == null || !(payload.getObject() instanceof ItemDragPayload itemPayload)) {
      return null;
    }
    return itemPayload;
  }
}
