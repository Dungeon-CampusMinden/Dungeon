package contrib.utils.components.item;

import contrib.components.InventoryComponent;
import contrib.components.ItemComponent;

import core.Entity;
import core.Game;
import core.utils.components.MissingComponentException;

import java.util.function.BiConsumer;

public class DefaultCollect implements BiConsumer<Entity, Entity> {
    @Override
    public void accept(Entity worldItem, Entity whoCollected) {
        // check if the Game has a Hero
        Game.hero()
                .ifPresent(
                        hero -> {
                            // check if entity picking up Item is the Hero
                            if (whoCollected.equals(hero)) {
                                // check if Hero has an Inventory Component
                                hero.fetch(InventoryComponent.class)
                                        .ifPresent(
                                                (x) -> {
                                                    // check if Item can be added to hero Inventory
                                                    if ((x)
                                                            .addItem(
                                                                    worldItem
                                                                            .fetch(
                                                                                    ItemComponent
                                                                                            .class)
                                                                            .orElseThrow(
                                                                                    () ->
                                                                                            MissingComponentException
                                                                                                    .build(
                                                                                                            worldItem,
                                                                                                            ItemComponent
                                                                                                                    .class))
                                                                            .itemData()))
                                                        // if added to hero Inventory
                                                        // remove Item from World
                                                        Game.removeEntity(worldItem);
                                                });
                            }
                        });
    }
}
