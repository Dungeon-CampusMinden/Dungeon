package contrib.entities;

import contrib.components.*;
import contrib.item.Item;
import contrib.utils.components.interaction.DropItemsInteraction;
import core.Entity;
import core.components.CameraComponent;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.utils.Point;
import core.utils.components.path.IPath;
import java.io.IOException;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A utility class for building entities in the game world. The {@link EntityFactory} class provides
 * static methods to construct various types of entities with different components.
 *
 * <p>This class only references Methods of the {@link HeroFactory},{@link MonsterFactory} and
 * {@link MiscFactory}
 */
public final class EntityFactory {

  /**
   * Get an Entity that can be used as a monster.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link PositionComponent}, {@link HealthComponent}, {@link AIComponent} with
   * random AIs from the {@link AIFactory} class, {@link DrawComponent} with a randomly set
   * Animation, {@link VelocityComponent}, {@link CollideComponent} and a 10% chance for an {@link
   * InventoryComponent}. If it has an Inventory it will use the {@link DropItemsInteraction} on
   * death.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity randomMonster() throws IOException {
    return MonsterFactory.randomMonster();
  }

  /**
   * Get an Entity that can be used as a monster.
   *
   * <p>The Entity is not added to the game yet. *
   *
   * <p>It will have a {@link PositionComponent}, {@link HealthComponent}, {@link AIComponent} with
   * random AIs from the {@link AIFactory} class, {@link DrawComponent} with a randomly set
   * Animation, {@link VelocityComponent}, {@link CollideComponent} and a 10% chance for an {@link
   * InventoryComponent}. If it has an Inventory it will use the {@link DropItemsInteraction} on
   * death.
   *
   * @param pathToTexture Textures to use for the monster.
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity randomMonster(IPath pathToTexture) throws IOException {
    return MonsterFactory.randomMonster(pathToTexture);
  }

  /**
   * Get an Entity that can be used as a chest.
   *
   * <p>Will contain some random items.
   *
   * <p>The Entity is not added to the game yet. *
   *
   * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
   * core.components.DrawComponent}, {@link contrib.components.CollideComponent} and {@link
   * contrib.components.InventoryComponent}. It will use the {@link
   * contrib.utils.components.interaction.DropItemsInteraction} on interaction.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newChest() throws IOException {
    return MiscFactory.newChest(MiscFactory.FILL_CHEST.RANDOM);
  }

  /**
   * Get an Entity that can be used as a chest.
   *
   * <p>It will contain the given items.
   *
   * <p>The Entity is not added to the game yet.
   *
   * <p>It will have a {@link InteractionComponent}. {@link PositionComponent}, {@link
   * core.components.DrawComponent}, {@link contrib.components.CollideComponent} and {@link
   * contrib.components.InventoryComponent}. It will use the {@link
   * contrib.utils.components.interaction.DropItemsInteraction} on interaction.
   *
   * @param item Items that should be in the chest.
   * @param position Where should the chest be placed?
   * @return A new Entity.
   * @throws IOException If the animation could not be loaded.
   */
  public static Entity newChest(final Set<Item> item, final Point position) throws IOException {
    return MiscFactory.newChest(item, position);
  }

  /**
   * Get an Entity that can be used as a crafting cauldron.
   *
   * <p>The Entity is not added to the game yet.
   *
   * @return A new Entity.
   * @throws IOException if the animation could not been loaded.
   */
  public static Entity newCraftingCauldron() throws IOException {
    return MiscFactory.newCraftingCauldron();
  }
}
