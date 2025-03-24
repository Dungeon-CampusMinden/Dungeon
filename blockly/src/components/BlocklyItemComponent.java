package components;

import core.Component;

/**
 * Marks an entity as an Item in the Blockly world.
 *
 * <p>Items in Blockly differ from {@link contrib.item.Item Dungeon items} in that they are not
 * designed for an inventory system. Blockly items exist only in the game world, not in the
 * inventory.
 *
 * <p>This component is used to mark entities as collectible for the "pick up" block.
 */
public final class BlocklyItemComponent implements Component {}
