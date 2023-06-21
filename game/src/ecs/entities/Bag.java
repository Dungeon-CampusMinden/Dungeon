package ecs.entities;
/**
 * The Bag is a entity in the ECS. This class helps to setup bags with all its
 * components and attributes .
 * It is a abstract class, so it can be extended by other items.
 * It has the onUse, onDrop and onCollect methods, which are called when the item is used, dropped or collected.
 * It implements the IOnUse, IOnCollect and IOnDrop interfaces.
 * The Bag can be used, collected and dropped.
 */
import configuration.ItemConfig;
import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.items.ItemData;
import ecs.items.ItemType;
import graphic.Animation;
import starter.Game;
import tools.Point;

import java.util.List;
import java.util.logging.Logger;

public class Bag extends Item {
    private static ItemData itemData;
    private ItemComponent itemComponent;
    private transient final Logger bagLogger = Logger.getLogger(this.getClass().getName());

    public Bag() {
        super();
        setupItemComponent();
        setupHitBoxComponent();
        setupPositionComponent();
        setupAnimationComponent();
        bagLogger.info("Bag created");
    }

    /**
     * This constructor creates a Bag with the given itemData and point.
     * @param itemData The itemData of the Bag.
     * @param point The point of the Bag.
     */
    public Bag(ItemData itemData, Point point) {
        super();
        this.itemComponent = new ItemComponent(this, itemData);
        new PositionComponent(this, point);
        setupHitBoxComponent();
        setupAnimationComponent();
        bagLogger.info(itemData.getItemName() + " created at " + point.toString());
    }

    private Bag(ItemData itemData){
        if(itemData != null)
            return;
        setupItemComponent();
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
                (you, other, direction) -> onCollect(this, other),
                (you, other, direction) -> {
                });
    }

    @Override
    public void setupItemComponent() {
        itemData = new ItemData(
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
    public void onCollect(Entity worldItemEntity, Entity whoCollides) {
        bagLogger.info(worldItemEntity.toString() + " collected by " + whoCollides.toString());
        if (!Game.getHero().isPresent())
            return;
        if (!whoCollides.equals(Game.getHero().get()))
            return;
        if (!whoCollides.getComponent(InventoryComponent.class).isPresent())
            return;
        InventoryComponent ic = (InventoryComponent) whoCollides.getComponent(InventoryComponent.class).get();
        if (ic.addItem(
                worldItemEntity.getComponent(ItemComponent.class)
                        .map(ItemComponent.class::cast)
                        .get()
                        .getItemData()))
            Game.removeEntity(worldItemEntity);
    }

    @Override
    public void onUse(Entity e, ItemData item) {
        bagLogger.info(e.toString() + " used " + item.getItemName());
        if (!e.getComponent(InventoryComponent.class).isPresent())
            return;
        InventoryComponent ic = (InventoryComponent) e.getComponent(InventoryComponent.class).get();
        if (!item.getItemType().equals(ItemType.Bag))
            bagLogger.info(item.getItemType().toString());
        if (item.getInventory().size() < 1) {
            bagLogger.info("Bag is empty");
            return;
        }
        item.getInventory().get(0).triggerUse(e);
    }

    @Override
    public void onDrop(Entity user, ItemData which, Point position) {
        bagLogger.info(user.toString() + " dropped " + which.getItemName() + " at " + position.toString());
        Game.addEntity(new Bag(which, position));
        user.getComponent(InventoryComponent.class)
                .ifPresent(
                        component -> {
                            InventoryComponent invComp = (InventoryComponent) component;
                            invComp.removeItem(which);
                        });
    }

    /**
     * Returns ItemData object
     * @return ItemData
     */
    public static ItemData getItemData(){
        new Bag(itemData);
        return itemData;
    }
}
