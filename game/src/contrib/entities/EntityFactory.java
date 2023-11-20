package contrib.entities;

import contrib.item.Item;

import core.Entity;
import core.utils.Point;

import java.io.IOException;
import java.util.Set;

/**
 * A utility class for building entities in the game world. The {@link EntityFactory} class provides
 * static methods to construct various types of entities with different components.
 */
public class EntityFactory {

    public static Entity newHero() throws IOException {
        return HeroFactory.newHero();
    }

    public static Entity randomMonster() throws IOException {
        return MonsterFactory.randomMonster();
    }

    public static Entity randomMonster(String pathToTexture) throws IOException {
        return MonsterFactory.randomMonster(pathToTexture);
    }

    public static Entity newChest() throws IOException {
        return MiscFactory.newChest();
    }

    public static Entity newChest(Set<Item> item, Point position) throws IOException {
        return MiscFactory.newChest(item, position);
    }

    public static Entity newCraftingCauldron() throws IOException {
        return MiscFactory.newCraftingCauldron();
    }
}
