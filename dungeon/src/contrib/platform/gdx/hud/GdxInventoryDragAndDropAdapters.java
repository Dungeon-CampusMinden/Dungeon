package contrib.platform.gdx.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import contrib.components.InventoryComponent;
import contrib.hud.inventory.ItemDragPayload;
import contrib.item.Item;
import core.Entity;
import core.Game;
import core.platform.gdx.render.GdxAnimationFrames;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;

/**
 * libGDX-only adapters for Scene2D inventory drag-and-drop.
 *
 * <p>This isolates the remaining {@link DragAndDrop.Source}/{@link DragAndDrop.Target}
 * construction from {@code InventoryGUI}. The GUI keeps the semantic inventory actions,
 * while this class provides the technical Scene2D wrapper around the shared actor anchor.
 */
public final class GdxInventoryDragAndDropAdapters {

  private GdxInventoryDragAndDropAdapters() {}

  /**
   * Creates a Scene2D drag source for an inventory grid.
   *
   * @param actor technical Scene2D actor anchor
   * @param inventoryComponent source inventory
   * @param slotResolver resolves local drag coordinates to an inventory slot index
   * @param isPlayersInventory determines whether the given inventory belongs to the given player
   * @param slotSizeSupplier supplies the current rendered slot size
   * @param dragAndDropSupplier supplies the shared libGDX drag-and-drop context
   * @param onDropOutside callback when the payload is released without a valid target
   * @return configured libGDX drag source
   */
  public static DragAndDrop.Source itemSource(
    Actor actor,
    InventoryComponent inventoryComponent,
    BiFunction<Float, Float, Integer> slotResolver,
    BiPredicate<Entity, InventoryComponent> isPlayersInventory,
    IntSupplier slotSizeSupplier,
    Supplier<Optional<DragAndDrop>> dragAndDropSupplier,
    Consumer<ItemDragPayload> onDropOutside) {

    return new DragAndDrop.Source(actor) {
      @Override
      public DragAndDrop.Payload dragStart(InputEvent event, float x, float y, int pointer) {
        int draggedSlot = slotResolver.apply(x, y);
        Optional<Item> item = inventoryComponent.get(draggedSlot);
        if (item.isEmpty()) {
          return null;
        }

        Entity player = Game.player().orElseThrow();
        Item itemToTransfer = item.get();
        boolean isHeroInv = isPlayersInventory.test(player, inventoryComponent);

        DragAndDrop.Payload payload = new DragAndDrop.Payload();
        payload.setObject(
          new ItemDragPayload(inventoryComponent, isHeroInv, draggedSlot, itemToTransfer));

        Image image =
          new Image(
            new SpriteDrawable(
              GdxAnimationFrames.toSprite(itemToTransfer.inventoryAnimation().update())));
        image.setSize(slotSizeSupplier.getAsInt(), slotSizeSupplier.getAsInt());
        payload.setDragActor(image);

        dragAndDropSupplier
          .get()
          .ifPresent(
            dragAndDrop ->
              dragAndDrop.setDragActorPosition(
                image.getWidth() / 2f, -image.getHeight() / 2f));

        return payload;
      }

      @Override
      public void dragStop(
        InputEvent event,
        float x,
        float y,
        int pointer,
        DragAndDrop.Payload payload,
        DragAndDrop.Target target) {
        if (target == null
          && payload != null
          && payload.getObject() instanceof ItemDragPayload itemDragPayload) {
          onDropOutside.accept(itemDragPayload);
        }
      }
    };
  }

  /**
   * Creates a Scene2D drag target for an inventory grid.
   *
   * @param actor technical Scene2D actor anchor
   * @param slotResolver resolves local drop coordinates to an inventory slot index
   * @param canAccept neutral acceptance predicate for dragged item payloads
   * @param onDrop neutral drop handler with resolved slot index
   * @return configured libGDX drag target
   */
  public static DragAndDrop.Target itemTarget(
    Actor actor,
    BiFunction<Float, Float, Integer> slotResolver,
    java.util.function.Predicate<ItemDragPayload> canAccept,
    ObjIntConsumer<ItemDragPayload> onDrop) {

    return new DragAndDrop.Target(actor) {
      @Override
      public boolean drag(
        DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        ItemDragPayload itemPayload = extract(payload);
        return itemPayload != null && canAccept.test(itemPayload);
      }

      @Override
      public void drop(
        DragAndDrop.Source source, DragAndDrop.Payload payload, float x, float y, int pointer) {
        ItemDragPayload itemPayload = extract(payload);
        if (itemPayload == null || !canAccept.test(itemPayload)) {
          return;
        }

        int slot = slotResolver.apply(x, y);
        onDrop.accept(itemPayload, slot);
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
