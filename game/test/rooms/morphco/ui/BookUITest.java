package rooms.morphco.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class BookUITest {

  private static final double PAGE = 445;

  // Varying heights like the jokes on a page at 1280x720; the opening joke 0 and joke 100 are
  // higher than a page.
  private static double height(int joke) {
    return joke == 0 || joke == 100 ? 500 : 40 + joke * 37 % 160;
  }

  private static double sum(List<Integer> page) {
    return page.stream().mapToDouble(BookUITest::height).sum();
  }

  private static List<Integer> jokes(int first, int last) {
    return IntStream.rangeClosed(first, last).boxed().toList();
  }

  @Test
  void everyJokeLandsOnFullPagesOnceAndInOrder() {
    List<List<Integer>> sections = new ArrayList<>(List.of(List.of(0)));
    sections.addAll(
        List.of(
            jokes(1, 20),
            jokes(21, 40),
            jokes(41, 60),
            jokes(61, 75),
            jokes(76, 90),
            jokes(91, 100)));

    List<List<Integer>> pages = BookUI.pages(sections, BookUITest::height, PAGE);

    assertEquals(jokes(0, 100), pages.stream().flatMap(List::stream).toList());
    assertTrue(pages.stream().noneMatch(List::isEmpty));
    List<Integer> sectionStarts = sections.stream().map(List::getFirst).toList();
    for (int start : sectionStarts)
      assertTrue(pages.stream().anyMatch(page -> page.getFirst() == start), "section " + start);
    for (int index = 0; index < pages.size(); index++) {
      List<Integer> page = pages.get(index);
      if (page.size() > 1) assertTrue(sum(page) <= PAGE, "page too high: " + page);
      if (index + 1 == pages.size()) continue;
      int next = pages.get(index + 1).getFirst();
      if (!sectionStarts.contains(next))
        assertTrue(sum(page) + height(next) > PAGE, "joke " + next + " would still fit: " + page);
    }
    assertEquals(List.of(0), pages.getFirst());
    assertEquals(List.of(100), pages.getLast());
  }
}
