package ecs.entities;

import configuration.ItemConfig;
import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.items.*;
import graphic.Animation;
import starter.Game;
import tools.Point;

import java.util.List;

public class Cake extends Item implements IOnUse, IOnCollect, IOnDrop {
    private ItemComponent itemComponent;

    public Cake() {
        super();
        setupItemComponent();
        setupHitBoxComponent();
        setupPositionComponent();
        setupAnimationComponent();
    }

    public Cake(ItemData itemData, Point point) {
        super();
        this.itemComponent = new ItemComponent(this, itemData);
        new PositionComponent(this, point);
        setupHitBoxComponent();
        setupAnimationComponent();
    }

    @Override
    public void setupAnimationComponent() {
        Animation idle = AnimationBuilder.buildAnimation(ItemConfig.KUCHEN_TEXTURE.get());
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
                ItemConfig.FOOD_TYPE.get(),
                new Animation(List.of(ItemConfig.KUCHEN_TEXTURE.get()), 1),
                new Animation(List.of(ItemConfig.KUCHEN_TEXTURE.get()), 1),
                ItemConfig.KUCHEN_NAME.get(),
                ItemConfig.KUCHEN_DESCRIPTION.get());

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
                heal(e);
                break;
            }
            for (int bagIndex = 0; bagIndex < id.getInventory().size(); bagIndex++) {
                if (!id.getInventory().get(bagIndex).equals(item))
                    continue;
                id.getInventory().remove(item);
                heal(e);
                break;
            }
        }
    }

    @Override
    public void onDrop(Entity user, ItemData which, Point position) {
        Game.addEntity(new Cake(which, position));
        if (!user.getComponent(InventoryComponent.class).isPresent())
            return;
        user.getComponent(InventoryComponent.class)
                .map(InventoryComponent.class::cast)
                .get()
                .removeItem(which);
    }

    private void heal(Entity entity) {
        if (!entity.getComponent(HealthComponent.class).isPresent())
            return;
        HealthComponent hc = (HealthComponent) entity.getComponent(HealthComponent.class).get();
        hc.setCurrentHealthpoints(hc.getMaximalHealthpoints());
    }

}
