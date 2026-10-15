package feature.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import feature.components.InventoryComponent;
import feature.inventory.Item;
import feature.inventory.ItemRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Tests the item placement check of assignment tasks. */
final class AssignmentCheckerTest {

  private static final String SPRITE = "items/rpg/food_bread.png";

  private final AssignmentTask task =
      new AssignmentTask("", Set.of("food", "magic"), Set.of("bread", "scroll", "cat"), targets());
  private final Entity food = container();
  private final Entity magic = container();
  private final Map<String, Entity> containers = Map.of("food", food, "magic", magic);
  private final AssignmentComponent assignment =
      new AssignmentComponent(
          new TaskComponent<>(task), task, containers, AssignmentChecker.CONTENTS);

  @Test
  void itemsWithoutTargetMustStayOutOfAllContainers() {
    add(food, "bread");
    add(magic, "scroll");
    assertTrue(AssignmentChecker.CONTENTS.check(assignment));

    add(food, "cat");
    assertFalse(AssignmentChecker.CONTENTS.check(assignment));
  }

  @Test
  void foreignItemsAreIgnoredAndDuplicateIdsFail() {
    add(food, "bread");
    add(magic, "scroll");
    add(magic, "unrelated");
    assertEquals(
        Optional.of(Map.of("bread", "food", "scroll", "magic")),
        AssignmentChecker.placement(task, containers));

    add(magic, "bread");
    assertEquals(Optional.empty(), AssignmentChecker.placement(task, containers));
  }

  @Test
  void answerIdSurvivesItemDataRoundTrip() {
    AnswerItem item = new AnswerItem("bread", "Brot", SPRITE);

    Item restored = ItemRegistry.create(ItemRegistry.idFor(item), item.itemData()).orElseThrow();

    assertEquals("bread", ((AnswerItem) restored).answerId());
    assertEquals(item, restored);
  }

  private static Map<String, String> targets() {
    return Map.of("bread", "food", "scroll", "magic");
  }

  private static Entity container() {
    Entity container = new Entity();
    container.add(new InventoryComponent(8));
    return container;
  }

  private static void add(final Entity container, final String answerId) {
    container
        .fetch(InventoryComponent.class)
        .orElseThrow()
        .add(new AnswerItem(answerId, answerId, SPRITE));
  }
}
