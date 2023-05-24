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
import java.util.logging.Logger;

/**
 * The MonsterPotion is a Potion that can be used to kill monster.
 * It extends the Item class. The MonsterPotion is an Item.
 * It implements the IOnUse, IOnCollect and IOnDrop interfaces.
 * The MonsterPotion can be used, collected and dropped.
 * The MonsterPotion is an entity in the ECS.
 */
public class MonsterPotion extends Item {
    private ItemComponent itemComponent;
    private transient final Logger monsterPotionLogger = Logger.getLogger(this.getClass().getName());


    public MonsterPotion() {
        super();
        setupItemComponent();
        setupHitBoxComponent();
        setupPositionComponent();
        setupAnimationComponent();
        monsterPotionLogger.info("MonsterPotion created");
    }

    /**
     * This constructor creates a MonsterPotion with the given itemData and point.
     * @param itemData The itemData of the MonsterPotion.
     * @param point The point of the MonsterPotion on the map.
     */
    public MonsterPotion(ItemData itemData, Point point) {
        super();
        this.itemComponent = new ItemComponent(this, itemData);
        new PositionComponent(this, point);
        setupHitBoxComponent();
        setupAnimationComponent();
        this.monsterPotionLogger.info(itemData.getItemName() + " created at " + point.toString());
    }

    protected void setupAnimationComponent() {
        Animation idle = AnimationBuilder.buildAnimation(ItemConfig.MONSTER_DESPAWN_TEXTURE.get());
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

    /**
     * This methode is used to collect the item
     * @param WorldItemEntity is the item, that will be collected
     * @param whoCollides that collects the item
     */
    @Override
    public void onCollect(Entity WorldItemEntity, Entity whoCollides) {
        monsterPotionLogger.info(WorldItemEntity.toString() + " collected by " + whoCollides.toString());
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
        monsterPotionLogger.info(e.toString() + " used " + item.getItemName());
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

    /**
     * This methode is used to drop an item.
     * @param user the entity, that drops the item
     * @param which item that is dropped
     * @param position where the item will be dropped
     */
    @Override
    public void onDrop(Entity user, ItemData which, Point position) {
        monsterPotionLogger.info(user.toString() + " dropped " + which.getItemName() + " at " + position.toString());
        Game.addEntity(new MonsterPotion(which, position));
        user.getComponent(InventoryComponent.class)
                .ifPresent(
                        component -> {
                            InventoryComponent invComp = (InventoryComponent) component;
                            invComp.removeItem(which);
                        });
    }

    /**
     * This Methode is executing the ability of the item
     * @param
     */
    private void slaughter() {
        monsterPotionLogger.info("MonsterPotion used");
        Game.getEntities().stream()
                // Consider only monsters
                .filter(e -> Monster.class.isAssignableFrom(e.getClass()))
                // Remove the monsters
                .forEach(Game::removeEntity);
    }

}
