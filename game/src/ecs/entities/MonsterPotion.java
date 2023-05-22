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
public class MonsterPotion extends Item{
    private ItemComponent itemComponent;

    public MonsterPotion(){
        super();
        setupItemComponent();
        setupHitBoxComponent();
        setupPositionComponent();
        setupAnimationComponent();
    }

    public MonsterPotion(ItemData itemData, Point point){
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
            (you, other, direction) -> {});
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
        Boolean[] isRemoved = {null};
        e.getComponent(InventoryComponent.class)
            .ifPresent(
                component -> {
                    InventoryComponent invComp = (InventoryComponent) component;
                    List<ItemData> itemData = invComp.getItems();
                    for (ItemData inventoryItem : itemData) {
                        if(inventoryItem.getItemType().equals(ItemType.Bag)){
                            for(int bagItemIndex = 0; bagItemIndex < inventoryItem.getInventory().size(); bagItemIndex++){
                                if(inventoryItem.getInventory().get(bagItemIndex).equals(item)){
                                    inventoryItem.getInventory().remove(item);
                                    isRemoved[0] = true;
                                    break;
                                }
                            }
                        }
                        else{
                            if(inventoryItem.equals(item)){
                                invComp.removeItem(item);
                                isRemoved[0] = true;
                                break;
                            }
                        }
                    }
                }
            );

        if(isRemoved[0]) {
            for(int x = 0; x < Game.getEntities().size(); x++){
                if((Entity)Game.getEntities().toArray()[x] instanceof Monster) {
                    Game.removeEntity((Entity) Game.getEntities().toArray()[x]);
                }
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
}
