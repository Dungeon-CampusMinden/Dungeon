package feature.interaction.sorting;

import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Entity;
import feature.systems.EventScheduler;
import feature.tasks.TaskComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SortingFactoryTest {

  // Dog belongs with the animals, Rose with the plants, Stone in no bin.
  private final Sorting sorting =
      new Sorting(
          "",
          "",
          List.of("Dog", "Rose", "Stone"),
          List.of("Animals", "Plants"),
          List.of(0, 1, Sorting.NONE));

  @AfterEach
  void tearDown() {
    EventScheduler.clear();
  }

  @Test
  @SuppressWarnings("unchecked")
  void checkCountsWrongTermsAndSolvesOnlyTheRightPlacement() {
    Entity board = SortingFactory.createSorting(new Entity(), sorting);
    TaskComponent<Map<String, String>> task = board.fetch(TaskComponent.class).orElseThrow();
    List<Entity> solvedBy = new ArrayList<>();
    List<Entity> wrongBy = new ArrayList<>();
    task.onCorrect(solvedBy::add);
    task.onWrong(wrongBy::add);
    Entity player = new Entity();

    SortingFactory.submit(board, sorting, task, new int[] {0, 1, 0}, player);
    SortingFactory.submit(board, sorting, task, new int[] {0, 1, Sorting.NONE}, player);

    assertEquals(List.of(player), wrongBy);
    assertEquals(List.of(player), solvedBy);
    assertEquals(1, sorting.wrong(new int[] {0, 1, 0}));
  }
}
