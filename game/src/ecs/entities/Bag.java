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

public class Bag extends Item{
    private ItemComponent itemComponent;

    public Bag(){
        super();
        setupItemComponent();
        setupHitBoxComponent();
        setupPositionComponent();
        setupAnimationComponent();
    }

    public Bag(ItemData itemData, Point point){
        super();
        this.itemComponent = new ItemComponent(this, itemData);
        new PositionComponent(this, point);
        setupHitBoxComponent();
        setupAnimationComponent();
    }

    @Override
    public void setupAnimationComponent() {
        Animation idle = AnimationBuilder.buildAnimation(ItemConfig.BAG_TEXTURE.get());
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
            (you, other, direction) -> onCollect(this,other),
            (you, other, direction) -> {});
    }

    @Override
    public void setupItemComponent() {
        ItemData itemData = new ItemData(
            ItemConfig.BAG_TYPE.get(),
            new Animation(List.of(ItemConfig.BAG_TEXTURE.get()), 1),
            new Animation(List.of(ItemConfig.BAG_TEXTURE.get()), 1),
            ItemConfig.BAG_NAME.get(),
            ItemConfig.BAG_DESCRIPTION.get());

        itemData.setOnCollect(this::onCollect);
        itemData.setOnUse(this::onUse);
        itemData.setOnDrop(this::onDrop);

        this.itemComponent = new ItemComponent(this, itemData);
    }

    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollides) {
        Game.getHero()
            .ifPresent(
                hero -> {
                    if (whoCollides.equals(hero)) {
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

    @Override
    public void onUse(Entity e, ItemData item) {
        Boolean[] hasInventory = {null};
        e.getComponent(InventoryComponent.class)
            .ifPresent(
                component -> {
                    InventoryComponent invComp = (InventoryComponent) component;
                    if(item.getItemType().equals(ItemType.Bag)){
                        if(item.getInventory().size() > 0){
                            item.getInventory().get(0).triggerUse(e);
                        }
                        else{
                            System.out.println("Bag is empty");
                        }
                    }
                }
            );
    }

    @Override
    public void onDrop(Entity user, ItemData which, Point position) {
        Game.addEntity(new Bag(which, position));
        user.getComponent(InventoryComponent.class)
            .ifPresent(
                component -> {
                    InventoryComponent invComp = (InventoryComponent) component;
                    invComp.removeItem(which);
                });
    }
}
