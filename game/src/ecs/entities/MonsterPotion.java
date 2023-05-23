package ecs.entities;

import configuration.ItemConfig;
import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.items.ItemData;
import ecs.items.ItemType;
import graphic.Animation;
import starter.Game;
import tools.Point;

import java.util.List;

public class MonsterPotion extends Item {
    private ItemComponent itemComponent;

    public MonsterPotion() {
        super();
        setupItemComponent();
        setupHitBoxComponent();
        setupPositionComponent();
        setupAnimationComponent();
    }

    public MonsterPotion(ItemData itemData, Point point) {
        super();
        this.itemComponent = new ItemComponent(this, itemData);
        new PositionComponent(this, point);
        setupHitBoxComponent();
        setupAnimationComponent();
    }

    public void setupAnimationComponent() {
        Animation idle = AnimationBuilder.buildAnimation(ItemConfig.MONSTER_DESPAWN_TEXTURE.get());
        new AnimationComponent(this, idle);
    }

    @Override
    public void setupPositionComponent() {
        new PositionComponent(this);
    }

    @Override
    public void setupHitBoxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> onCollect(this, other),
                (you, other, direction) -> {
                });
    }

    @Override
    public void setupItemComponent() {
        ItemData itemData = new ItemData(
                ItemConfig.POTION_TYPE.get(),
                new Animation(List.of(ItemConfig.MONSTER_DESPAWN_TEXTURE.get()), 1),
                new Animation(List.of(ItemConfig.MONSTER_DESPAWN_TEXTURE.get()), 1),
                ItemConfig.MONSTER_DESPAWN_NAME.get(),
                ItemConfig.MONSTER_DESPAWN_DESCRIPTION.get());

        itemData.setOnCollect(this::onCollect);
        itemData.setOnUse(this::onUse);
        itemData.setOnDrop(this::onDrop);

        this.itemComponent = new ItemComponent(this, itemData);
    }

    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollides) {
        if (!Game.getHero().isPresent())
            return;
        if (!whoCollides.equals(Game.getHero().get()))
            return;
        if (!whoCollides.getComponent(InventoryComponent.class).isPresent())
            return;
        InventoryComponent ic = (InventoryComponent) whoCollides.getComponent(InventoryComponent.class).get();
        if (ic.addItem(
                WorldItemEntity.getComponent(ItemComponent.class)
                        .map(ItemComponent.class::cast)
                        .get()
                        .getItemData()))
            Game.removeEntity(WorldItemEntity);
    }

    @Override
    public void onUse(Entity e, ItemData item) {
        if (!e.getComponent(InventoryComponent.class).isPresent())
            return;
        InventoryComponent ic = (InventoryComponent) e.getComponent(InventoryComponent.class).get();
        List<ItemData> itemData = ic.getItems();
        for (ItemData id : itemData) {
            if (!id.getItemType().equals(ItemType.Bag)) {
                if (!id.equals(item))
                    continue;
                ic.removeItem(item);
                slaughter();
                break;
            }
            for (int bagIndex = 0; bagIndex < id.getInventory().size(); bagIndex++) {
                if (!id.getInventory().get(bagIndex).equals(item))
                    continue;
                id.getInventory().remove(item);
                slaughter();
                break;
            }
        }
    }

    @Override
    public void onDrop(Entity user, ItemData which, Point position) {
        Game.addEntity(new MonsterPotion(which, position));
        user.getComponent(InventoryComponent.class)
                .ifPresent(
                        component -> {
                            InventoryComponent invComp = (InventoryComponent) component;
                            invComp.removeItem(which);
                        });
    }

    private void slaughter() {
        Game.getEntities().stream()
                // Consider only monsters
                .filter(e -> Monster.class.isAssignableFrom(e.getClass()))
                // Remove the monsters
                .forEach(Game::removeEntity);
    }

}
