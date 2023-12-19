package contrib.crafting;

/**
 * A crafting result. This is the base class for all crafting results.
 *
 * <p>Crafting results are used to define the result of a crafting recipe. They are executed when a
 * recipe is crafted and can be used to add items to the player's inventory, add components to the
 * player's entity, etc...
 */
public interface CraftingResult {

  /**
   * Returns the type of the crafting result.
   *
   * @return The type of the crafting result.
   */
  CraftingType resultType();
}
