package ecs.entities;

import configuration.ItemConfig;
import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.items.*;
import graphic.Animation;
import starter.Game;
import tools.Point;

import java.util.List;
import java.util.logging.Logger;

/**
 * This class is a subclass of Item and implements IOnUse, IOnDrop, IOnCollect.
 * It is used to create a SpeedPotion item.
 * The SpeedPotion can be used, collected and dropped.
 * The SpeedPotion is an entity in the ECS.
 * The SpeedPotion increases the speed of the player on use.
 */
public class SpeedPotion extends Item implements IOnUse, IOnDrop, IOnCollect {
    private static ItemData itemData;
    private ItemComponent itemComponent;
    private transient final Logger speedPotionLogger = Logger.getLogger(this.getClass().getName());


    /**
     * This constructor creates a SpeedPotion.
     */
    public SpeedPotion() {
        super();
        setupItemComponent();
        setupHitBoxComponent();
        setupPositionComponent();
        setupAnimationComponent();
        speedPotionLogger.info("SpeedPotion created");
    }

    /**
     * This constructor creates a SpeedPotion with the given itemData and point.
     * @param itemData The itemData of the SpeedPotion.
     * @param point The point of the SpeedPotion on the map.
     */
    public SpeedPotion(ItemData itemData, Point point) {
        super();
        this.itemComponent = new ItemComponent(this, itemData);
        new PositionComponent(this, point);
        setupHitBoxComponent();
        setupAnimationComponent();
        this.speedPotionLogger.info(itemData.getItemName() + " created at " + point.toString());
    }

    private SpeedPotion(ItemData itemData){
        if(itemData != null)
            return;
        setupItemComponent();
    }

    protected void setupAnimationComponent() {
        Animation idle = AnimationBuilder.buildAnimation(ItemConfig.SPEED_TEXTURE.get());
        new AnimationComponent(this, idle);
    }

    @Override
    protected void setupPositionComponent() {
        new PositionComponent(this);
    }

    @Override
    protected void setupHitBoxComponent() {
        new HitboxComponent(
                this,
                (you, other, direction) -> onCollect(this, other),
                (you, other, direction) -> {
                });
    }

    @Override
    protected void setupItemComponent() {
        itemData = new ItemData(
                ItemConfig.POTION_TYPE.get(),
                new Animation(List.of(ItemConfig.SPEED_TEXTURE.get()), 1),
                new Animation(List.of(ItemConfig.SPEED_TEXTURE.get()), 1),
                ItemConfig.SPEED_NAME.get(),
                ItemConfig.SPEED_DESCRIPTION.get());

        itemData.setOnCollect(this::onCollect);
        itemData.setOnUse(this::onUse);
        itemData.setOnDrop(this::onDrop);

        this.itemComponent = new ItemComponent(this, itemData);
    }

    /**
     * This methode is used to collect the item
     * @param WorldItemEntity is the item, that will be collected
     * @param whoCollides that collects the item
     */
    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollides) {
        speedPotionLogger.info(WorldItemEntity.toString() + " collected by " + whoCollides.toString());
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

    /**
     * Uses the item and removes
     * the item from the
     * inventory.
     *
     * @param e Entity that uses the item
     * @param item Item that is used
     */
    @Override
    public void onUse(Entity e, ItemData item) {
        speedPotionLogger.info(e.toString() + " used " + item.getItemName());
        if (!e.getComponent(InventoryComponent.class).isPresent())
            return;
        InventoryComponent ic = (InventoryComponent) e.getComponent(InventoryComponent.class).get();
        List<ItemData> itemData = ic.getItems();
        for (ItemData id : itemData) {
            if (!id.getItemType().equals(ItemType.Bag)) {
                if (!id.equals(item))
                    continue;
                ic.removeItem(item);
                speedUp(e);
                break;
            }
            for (int bagIndex = 0; bagIndex < id.getInventory().size(); bagIndex++) {
                if (!id.getInventory().get(bagIndex).equals(item))
                    continue;
                id.getInventory().remove(item);
                speedUp(e);
                break;
            }
        }
    }

    /**
     * This methode is used to drop an item.
     * @param user the entity, that drops the item
     * @param which item that is dropped
     * @param position where the item will be dropped
     */
    @Override
    public void onDrop(Entity user, ItemData which, Point position) {
        speedPotionLogger.info(user.toString() + " dropped " + which.getItemName() + " at " + position.toString());
        Game.addEntity(new SpeedPotion(which, position));
        user.getComponent(InventoryComponent.class)
                .ifPresent(
                        component -> {
                            InventoryComponent invComp = (InventoryComponent) component;
                            invComp.removeItem(which);
                        });
    }

    /**
     * This Methode is executing the ability of the item
     * @param entity
     */
    private void speedUp(Entity entity) {
        speedPotionLogger.info(entity.toString() + " speeded up by " + itemComponent.getItemData().getItemName());
        if (!entity.getComponent(VelocityComponent.class).isPresent())
            return;
        VelocityComponent vc = (VelocityComponent) entity.getComponent(VelocityComponent.class).get();
        vc.setXVelocity(vc.getXVelocity() * 2);
        vc.setYVelocity(vc.getYVelocity() * 2);
    }

    public static ItemData getItemData(){
        new SpeedPotion(itemData);
        return itemData;
    }
}
