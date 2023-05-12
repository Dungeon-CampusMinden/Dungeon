package builder;

import ecs.entities.Entity;
import ecs.items.ItemData;
import java.util.List;
import tools.Point;

/**
 * A utility class for building entities in the game world. The {@code EntityBuilder} class provides
 * static methods to construct various types of entities with different components.
 */
public class EntityBuilder {

    public static Entity buildHero() {
        return HeroBuilder.buildHero();
    }

    public static Entity buildChest() {
        return ChestBuilder.createNewChest();
    }

    public static Entity buildChest(List<ItemData> itemData, Point position) {
        return ChestBuilder.buildChest(itemData, position);
    }
}
