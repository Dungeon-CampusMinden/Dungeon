package contrib.components;

import static org.junit.jupiter.api.Assertions.*;

import contrib.item.Item;
import core.Entity;
import core.Game;
import core.utils.components.draw.Animation;
import core.utils.components.path.SimpleIPath;
import java.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
    Item itemData =
        new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE));
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
    ic.add(new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE)));
    assertTrue(
        ic.add(
            new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE))));

    assertEquals(2, ic.count());
  }

  /** Adding two Items to an Inventory with a size of 1 should only add the first. */
  @Test
  public void addItemOverSize() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(1);
    e.add(ic);
    ic.add(new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE)));
    assertFalse(
        ic.add(
            new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE))));
    assertEquals(1, ic.count());
  }

  /** Removing of an added Item. */
  @Test
  public void removeItemExisting() {
    Entity e = new Entity();
    InventoryComponent ic = new InventoryComponent(1);
    e.add(ic);
    Item itemData =
        new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE));
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
    Item itemData =
        new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE));
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
    Item itemData =
        new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE));
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
    Item itemData =
        new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE));
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
    Item itemData1 =
        new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE));
    ic.add(itemData1);
    Item itemData2 =
        new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE));
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
    Item itemData =
        new Item("Test item", "Test description", Animation.fromSingleImage(MISSING_TEXTURE));
    Item[] list = ic.items();
    assertEquals(0, ic.count());
    assertFalse(Arrays.asList(list).contains(itemData));
  }

  /** WTF? . */
  @Test
  public void tranfserItem() {
    InventoryComponent ic = new InventoryComponent(1);
    InventoryComponent other = new InventoryComponent(1);
    Item item = Mockito.mock(Item.class);
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
    Item item = Mockito.mock(Item.class);
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
    Item item = Mockito.mock(Item.class);
    assertFalse(ic.transfer(item, other));
  }

  /** WTF? . */
  @Test
  public void transferItemToItself() {
    InventoryComponent ic = new InventoryComponent(1);
    Item item = Mockito.mock(Item.class);
    ic.add(item);
    assertTrue(Arrays.asList(ic.items()).contains(item));
    assertFalse(ic.transfer(item, ic));
    assertTrue(Arrays.asList(ic.items()).contains(item));
  }
}
