package contrib.components;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import contrib.item.Item;
import core.Entity;
import core.Game;
import core.utils.components.draw.animation.Animation;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link InventoryComponent}. */
public class InventoryComponentTest {

  /** WTF? . */
  public static final SimpleIPath MISSING_TEXTURE =
      new SimpleIPath("animation/missing_texture.png");

  /** WTF? . */
  @AfterEach
  public void cleanup() {
    Game.removeAllEntities();
  }

  /** Constructor should create the inventory with the given parameters. */
  @Test
  public void validCreation() {

    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(1);
    e.add(ic);
    assertEquals(0, ic.count());
  }

  /** Adding one valid Item. */
  @Test
  public void addItemValid() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(1);
    e.add(ic);
    Item itemData = new Item("Test item", "Test description", new Animation(MISSING_TEXTURE));
    assertTrue(ic.add(itemData));
    assertEquals(1, ic.count());
  }

  /**
   * When there is enough space in the Inventory it should be possible to add more than one Item.
   */
  @Test
  public void addItemValidMultiple() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(3);
    e.add(ic);
    ic.add(new Item("Test item", "Test description", new Animation(MISSING_TEXTURE)));
    assertTrue(ic.add(new Item("Test item", "Test description", new Animation(MISSING_TEXTURE))));

    assertEquals(2, ic.count());
  }

  /** Adding two Items to an Inventory with a size of 1 should only add the first. */
  @Test
  public void addItemOverSize() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(1);
    e.add(ic);
    ic.add(new Item("Test item", "Test description", new Animation(MISSING_TEXTURE)));
    assertFalse(ic.add(new Item("Test item", "Test description", new Animation(MISSING_TEXTURE))));
    assertEquals(1, ic.count());
  }

  /** Removing of an added Item. */
  @Test
  public void removeItemExisting() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(1);
    e.add(ic);
    Item itemData = new Item("Test item", "Test description", new Animation(MISSING_TEXTURE));
    ic.add(itemData);
    assertTrue(ic.remove(itemData));

    assertEquals(0, ic.count());
  }

  /** Removing an Item which was already removed before. */
  @Test
  public void removeItemTwice() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(1);
    e.add(ic);
    Item itemData = new Item("Test item", "Test description", new Animation(MISSING_TEXTURE));
    ic.add(itemData);
    ic.remove(itemData);
    assertFalse(ic.remove(itemData));

    assertEquals(0, ic.count());
  }

  /** {@code null} should not remove any Item. */
  @Test
  public void removeItemNull() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(1);
    e.add(ic);
    Item itemData = new Item("Test item", "Test description", new Animation(MISSING_TEXTURE));
    ic.add(itemData);
    assertFalse(ic.remove(null));

    assertEquals(1, ic.count());
  }

  /** Empty inventory should return an empty List. */
  @Test
  public void getAllItemsEmptyInventory() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(0);
    e.add(ic);
    assertEquals(0, ic.count());
  }

  /** An inventory with one Item should return a List with this Item. */
  @Test
  public void getAllItemsInventoryWithOnlyOneItem() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(1);
    e.add(ic);
    Item itemData = new Item("Test item", "Test description", new Animation(MISSING_TEXTURE));
    ic.add(itemData);
    Item[] list = ic.items();
    assertEquals(1, list.length);
    assertTrue(Arrays.asList(list).contains(itemData));
  }

  /** An inventory with one Item should return a List with this Item. */
  @Test
  public void getAllItemsInventoryWithTwoItems() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(2);
    e.add(ic);
    Item itemData1 = new Item("Test item", "Test description", new Animation(MISSING_TEXTURE));
    ic.add(itemData1);
    Item itemData2 = new Item("Test item", "Test description", new Animation(MISSING_TEXTURE));
    ic.add(itemData2);
    Item[] list = ic.items();
    assertEquals(2, list.length);
    assertTrue(Arrays.asList(list).contains(itemData1));
    assertTrue(Arrays.asList(list).contains(itemData2));
  }

  /** An inventory should only be able to return Items it contains. */
  @Test
  public void getAllItemsInventoryNoAddedItemButCreated() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(1);
    e.add(ic);
    Item itemData = new Item("Test item", "Test description", new Animation(MISSING_TEXTURE));
    Item[] list = ic.items();
    assertEquals(0, ic.count());
    assertFalse(Arrays.asList(list).contains(itemData));
  }

  /** WTF? . */
  @Test
  public void tranfserItem() {
    InventoryComponent ic = new InventoryComponent(1);
    InventoryComponent other = new InventoryComponent(1);
    Item item = mock(Item.class);
    when(item.maxStackSize()).thenReturn(1);
    when(item.stackSize()).thenReturn(1);
    ic.add(item);
    assertTrue(Arrays.asList(ic.items()).contains(item));
    assertTrue(ic.transfer(item, other));
    assertTrue(Arrays.asList(other.items()).contains(item));
    assertFalse(Arrays.asList(ic.items()).contains(item));
  }

  /** WTF? . */
  @Test
  public void tranfserItemNoSpace() {
    InventoryComponent ic = new InventoryComponent(1);
    InventoryComponent other = new InventoryComponent(0);
    Item item = mock(Item.class);
    when(item.maxStackSize()).thenReturn(1);
    when(item.stackSize()).thenReturn(1);
    ic.add(item);
    assertTrue(Arrays.asList(ic.items()).contains(item));
    assertFalse(ic.transfer(item, other));
    assertFalse(Arrays.asList(other.items()).contains(item));
    assertTrue(Arrays.asList(ic.items()).contains(item));
  }

  /** WTF? . */
  @Test
  public void tranfserItemNoItem() {
    InventoryComponent ic = new InventoryComponent(1);
    InventoryComponent other = new InventoryComponent(1);
    Item item = mock(Item.class);
    assertFalse(ic.transfer(item, other));
  }

  /** WTF? . */
  @Test
  public void transferItemToItself() {
    InventoryComponent ic = new InventoryComponent(1);
    Item item = mock(Item.class);
    when(item.maxStackSize()).thenReturn(1);
    when(item.stackSize()).thenReturn(1);
    ic.add(item);
    assertTrue(Arrays.asList(ic.items()).contains(item));
    assertFalse(ic.transfer(item, ic));
    assertTrue(Arrays.asList(ic.items()).contains(item));
  }

  /**
   * Tests adding a stack of items when no item of the same class exists in the inventory yet.
   *
   * <p>Expected behavior: - A new stack is created in the inventory. - The new stack has the same
   * stack size as the incoming item. - The method returns true.
   */
  @Test
  public void addStack_NoExistingItem() {
    InventoryComponent ic = new InventoryComponent(5);

    Item item = mock(Item.class);
    when(item.maxStackSize()).thenReturn(5);
    when(item.stackSize()).thenReturn(3);

    boolean result = ic.add(item);

    assertTrue(result);
    assertEquals(1, ic.count());

    // Verify that the inventory contains the item and it has the correct stack size
    Set<Item> storedItems = ic.items(item.getClass());
    assertEquals(1, storedItems.size());
    assertEquals(3, storedItems.iterator().next().stackSize());
  }

  /**
   * Tests adding a stack of items to an inventory where an existing stack of the same item type
   * exists and has enough space to merge completely.
   *
   * <p>Expected behavior: - The incoming stack is fully merged into the existing stack. - No new
   * stack is created. - The existing stack's size equals its maxStackSize after merge. - The
   * incoming stack size becomes zero (emptied). - The method returns true.
   */
  @Test
  public void addStack_MergeCompletely() {
    InventoryComponent ic = new InventoryComponent(5);

    DummyItem existing = new DummyItem(2, 5);
    ic.add(existing);

    DummyItem incoming = new DummyItem(3, 5);

    boolean result = ic.add(incoming);

    assertTrue(result);
    assertEquals(1, ic.count());
    assertEquals(5, existing.stackSize()); // Full stack after merge
    assertEquals(0, incoming.stackSize()); // Incoming stack emptied
  }

  /**
   * Tests adding a stack of items when multiple existing stacks of the same item type exist, each
   * partially filled, and the incoming stack needs to be split across them.
   *
   * <p>Additionally, when the incoming stack does not fit completely into existing stacks, the
   * remainder is added as a new stack in the inventory.
   *
   * <p>Expected behavior: - Existing stacks are filled up to their maxStackSize. - Remaining items
   * form a new stack if remainder > 0. - Total stack count increases accordingly. - The method
   * returns true. - The incoming item's stackSize is reduced accordingly and should be 0 after full
   * merge.
   */
  @Test
  public void addStack_SplitAcrossMultipleStacks() {
    InventoryComponent ic = new InventoryComponent(5);

    DummyItem stack1 = new DummyItem(5, 5);
    DummyItem stack2 = new DummyItem(2, 5);
    ic.add(stack1);
    ic.add(stack2);
    stack1.stackSize(1);

    assertEquals(2, ic.count());

    DummyItem incoming = new DummyItem(5, 5);
    boolean result = ic.add(incoming);
    assertTrue(result);

    assertEquals(2, ic.count());
    Set<Item> storedItems = ic.items(DummyItem.class);
    assertEquals(2, storedItems.size());

    assertEquals(5, stack1.stackSize());
    assertEquals(3, stack2.stackSize());
    assertEquals(0, incoming.stackSize());
  }

  /**
   * Tests adding an incoming stack partially merging into an existing stack, with the remainder
   * added as a new stack in the inventory.
   *
   * <p>Expected behavior: - Existing stack is filled up to maxStackSize. - Remaining items form a
   * new stack. - Inventory count increases accordingly. - The method returns true.
   */
  @Test
  public void addStack_PartialMerge_NewStackForRest() {
    InventoryComponent ic = new InventoryComponent(5);

    DummyItem stack1 = new DummyItem(4, 5);
    ic.add(stack1);

    DummyItem incoming = new DummyItem(3, 5);
    boolean result = ic.add(incoming);

    assertTrue(result);
    assertEquals(2, ic.count()); // stack1 voll, Rest als neuer Stack

    assertEquals(5, stack1.stackSize()); // stack1 wurde aufgefüllt
    assertEquals(2, incoming.stackSize()); // restliche 2 Items verbleiben im incoming
  }

  /**
   * Tests adding an incoming stack when there is partial space in existing stack, but no space left
   * for the remaining items.
   *
   * <p>Expected behavior: - Existing stack is filled up to maxStackSize. - No new stack can be
   * added due to full inventory. - The method returns false.
   */
  @Test
  public void addStack_NoSpaceForRest() {
    InventoryComponent ic = new InventoryComponent(1); // only 1 slot

    DummyItem stack1 = new DummyItem(4, 5);
    ic.add(stack1);

    DummyItem incoming = new DummyItem(3, 5);
    boolean result = ic.add(incoming);

    assertFalse(result); // no space for remainder
    assertEquals(5, stack1.stackSize()); // existing stack filled up
    assertEquals(2, incoming.stackSize()); // 2 items merged, 1 remains
  }

  /**
   * Tests that removeOne decreases the stack size of an existing item by one, and removes the item
   * from inventory if the stack size reaches zero.
   */
  @Test
  public void removeOne_DecreasesStackOrRemovesItem() {
    InventoryComponent ic = new InventoryComponent(2);

    final int[] stackSize = {1};
    Item item = mock(Item.class);
    when(item.stackSize()).thenAnswer(invocation -> stackSize[0]);
    doAnswer(
            invocation -> {
              stackSize[0] = invocation.getArgument(0);
              return null;
            })
        .when(item)
        .stackSize(anyInt());

    ic.add(item);

    // Remove one unit (should remove the item because stack size becomes 0)
    boolean result = ic.removeOne(item);

    assertTrue(result);
    assertEquals(0, stackSize[0]);
    assertEquals(0, ic.count());

    // Try removing an item not in inventory
    Item notInInventory = mock(Item.class);
    assertFalse(ic.removeOne(notInInventory));
  }

  private class DummyItem extends Item {
    public DummyItem(int stackSize, int maxStackSize) {
      super(null, null, null, null, stackSize, maxStackSize);
    }
  }
}
