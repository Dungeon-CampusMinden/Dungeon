package ecs.items;

import graphic.Animation;
import java.util.List;
import java.util.Random;

import configuration.ItemConfig;

/**
 * Generator which creates a random ItemData based on the Templates prepared.
 * The ItemDataGenerator is used to create random Items for the ItemGenerator.
 * It uses the Templates prepared in the ItemDataTemplates class.
 * The ItemDataGenerator is used in the ItemGenerator to create random Items.
 */
public class ItemDataGenerator {
    private static final List<String> missingTexture = List.of("animation/missingTexture.png");

    private List<ItemData> templates = List.of(
            new ItemData(
                    ItemType.Food,
                    new Animation(List.of(ItemConfig.CAKE_TEXTURE.get()), 1),
                    new Animation(List.of(ItemConfig.CAKE_TEXTURE.get()), 1),
                    ItemConfig.CAKE_NAME.get(),
                    ItemConfig.CAKE_DESCRIPTION.get()),
            new ItemData(
                    ItemType.Potion,
                    new Animation(List.of(ItemConfig.SPEED_TEXTURE.get()), 1),
                    new Animation(List.of(ItemConfig.SPEED_TEXTURE.get()), 1),
                    ItemConfig.SPEED_NAME.get(),
                    ItemConfig.SPEED_DESCRIPTION.get()),
            new ItemData(
                    ItemType.Potion,
                    new Animation(List.of(ItemConfig.MONSTER_DESPAWN_TEXTURE.get()), 1),
                    new Animation(List.of(ItemConfig.MONSTER_DESPAWN_TEXTURE.get()), 1),
                    ItemConfig.MONSTER_DESPAWN_NAME.get(),
                    ItemConfig.MONSTER_DESPAWN_DESCRIPTION.get()));
    private Random rand = new Random();

    /**
     * @return a new randomItemData
     */
    public ItemData generateItemData() {
        return templates.get(rand.nextInt(templates.size()));
    }
}
