package contrib.utils.components.item;

import contrib.components.InventoryComponent;
import contrib.components.ItemComponent;
import core.Entity;
import core.Game;

public class DefaultCollect implements IOnCollect{
    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollected) {
        Game.getHero()
            .ifPresent(
                hero -> {
                    if (whoCollected.equals(hero)) {
                        hero.getComponent(InventoryComponent.class)
                            .ifPresent(
                                (x) -> {
                                    if (((InventoryComponent) x)
                                        .addItem(
                                            WorldItemEntity
                                                .getComponent(
                                                    ItemComponent
                                                        .class)
                                                .map(
                                                    ItemComponent
                                                        .class
                                                        ::cast)
                                                .get()
                                                .getItemData()))
                                        Game.removeEntity(WorldItemEntity);
                                });
                    }
                });
    }
}
