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
 * The Cake give the player health when on use.
 * It is used to create a Cake item.
 * The Cake is an entity in the ECS.
 * The Cake can be used, collected and dropped.
 * The Cake is an Item.
 * The Cake has an ItemComponent, a PositionComponent, a HitboxComponent and an AnimationComponent.
 */
public class Cake extends Item implements IOnUse, IOnCollect, IOnDrop {
    private static ItemData itemData;
    private ItemComponent itemComponent;
    private transient final Logger cakeLogger = Logger.getLogger(this.getClass().getName());

    public Cake() {
        super();
        setupItemComponent();
        setupHitBoxComponent();
        setupPositionComponent();
        setupAnimationComponent();
        cakeLogger.info("Cake created");
    }

    /**
     * This constructor creates a Cake with the given itemData and point.
     * @param itemData The itemData of the Cake.
     * @param point The point of the Cake.
     */
    public Cake(ItemData itemData, Point point) {
        super();
        this.itemComponent = new ItemComponent(this, itemData);
        new PositionComponent(this, point);
        setupHitBoxComponent();
        setupAnimationComponent();
        this.cakeLogger.info(itemData.getItemName() + " created at " + point.toString());
    }

    private Cake(ItemData itemData){
        if(itemData != null)
            return;
        setupItemComponent();
    }

    @Override
    protected void setupAnimationComponent() {
        Animation idle = AnimationBuilder.buildAnimation(ItemConfig.KUCHEN_TEXTURE.get());
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

    /**
     * This methode is used to collect the item
     * @param WorldItemEntity is the item, that will be collected
     * @param whoCollides that collects the item
     */
    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollides) {
        cakeLogger.info(WorldItemEntity.toString() + " collected by " + whoCollides.toString());
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
        cakeLogger.info(e.toString() + " used " + item.getItemName());
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

    /**
     * This methode is used to drop an item.
     * @param user the entity, that drops the item
     * @param which item that is dropped
     * @param position where the item will be dropped
     */
    @Override
    public void onDrop(Entity user, ItemData which, Point position) {
        cakeLogger.info(user.toString() + " dropped " + which.getItemName() + " at " + position.toString());
        Game.addEntity(new Cake(which, position));
        if (!user.getComponent(InventoryComponent.class).isPresent())
            return;
        user.getComponent(InventoryComponent.class)
                .map(InventoryComponent.class::cast)
                .get()
                .removeItem(which);
    }

    /**
     * This Methode is executing the ability of the item
     * @param entity
     */
    private void heal(Entity entity) {
        cakeLogger.info(entity.toString() + " healed by " + itemComponent.getItemData().getItemName());
        if (!entity.getComponent(HealthComponent.class).isPresent())
            return;
        HealthComponent hc = (HealthComponent) entity.getComponent(HealthComponent.class).get();
        hc.setCurrentHealthpoints(hc.getMaximalHealthpoints());
    }

    /**
     * Returns ItemData object
     * @return ItemData
     */
    public static ItemData getItemData(){
        new Cake(itemData);
        return itemData;
    }
}
