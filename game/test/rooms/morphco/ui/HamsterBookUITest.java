package rooms.morphco.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class HamsterBookUITest {

  private static final double PAGE = 445;

  // Varying heights like the jokes on a page at 1280x720; joke 100 is higher than a page.
  private static double height(int joke) {
    return joke == 100 ? 500 : 40 + joke * 37 % 160;
  }

  private static double sum(List<Integer> page) {
    return page.stream().mapToDouble(HamsterBookUITest::height).sum();
  }

  private static List<Integer> jokes(int first, int last) {
    return IntStream.rangeClosed(first, last).boxed().toList();
  }

  @Test
  void everyJokeLandsOnFullPagesOnceAndInOrder() {
    List<List<Integer>> chapters =
        List.of(
            jokes(1, 20),
            jokes(21, 40),
            jokes(41, 60),
            jokes(61, 75),
            jokes(76, 90),
            jokes(91, 100));

    List<List<Integer>> pages = HamsterBookUI.pages(chapters, HamsterBookUITest::height, PAGE);

    assertEquals(jokes(1, 100), pages.stream().flatMap(List::stream).toList());
    List<Integer> chapterStarts = chapters.stream().map(List::getFirst).toList();
    for (int chapter : chapterStarts)
      assertTrue(pages.stream().anyMatch(page -> page.getFirst() == chapter), "chapter " + chapter);
    for (int index = 0; index < pages.size(); index++) {
      List<Integer> page = pages.get(index);
      if (page.size() > 1) assertTrue(sum(page) <= PAGE, "page too high: " + page);
      if (index + 1 == pages.size()) continue;
      int next = pages.get(index + 1).getFirst();
      if (!chapterStarts.contains(next))
        assertTrue(sum(page) + height(next) > PAGE, "joke " + next + " would still fit: " + page);
    }
    assertEquals(List.of(100), pages.getLast());
  }
}
