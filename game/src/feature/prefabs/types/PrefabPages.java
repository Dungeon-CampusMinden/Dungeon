package feature.prefabs.types;

import engine.utils.Tuple;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Authored pages of a text cutscene, e.g. an intro: the number of pages, one text per page and the
 * font size.
 *
 * <p>A page can have a font size of its own: its text starts with the size and a bar, e.g. {@code
 * "48|The End"}.
 *
 * @param count number of used pages
 * @param pages text of every possible page, only shown up to {@link #count}
 * @param fontSize font size of the pages without a size of their own
 */
record PrefabPages(
    PrefabProperty<Integer> count,
    List<PrefabProperty<String>> pages,
    PrefabProperty<Integer> fontSize) {

  /** Maximum number of pages. */
  static final int MAX_PAGES = 12;

  private static final int MIN_FONT_SIZE = 12;
  private static final int MAX_FONT_SIZE = 96;
  // The size of a page with a size of its own, and its text.
  private static final Pattern OWN_SIZE = Pattern.compile("\\s*(\\d+)\\s*\\|(.*)", Pattern.DOTALL);
  private static final String PAGE_DESCRIPTION =
      "For another font size, start with the size and a bar, e.g. 48|The End. Sizes from "
          + MIN_FONT_SIZE
          + " to "
          + MAX_FONT_SIZE
          + ".";

  /**
   * Creates the properties of a page list.
   *
   * @return the page properties
   */
  static PrefabPages create() {
    PrefabProperty<Integer> count =
        PrefabProperty.integer("page-count", "Page Count", 2, 1, MAX_PAGES);
    List<PrefabProperty<String>> pages =
        PrefabTextList.entries(
            count, MAX_PAGES, "page", "Page", index -> "Page " + (index + 1), values -> true);
    for (PrefabProperty<String> page : pages) page.describedAs(PAGE_DESCRIPTION);
    return new PrefabPages(
        count,
        pages,
        PrefabProperty.integer("font-size", "Font Size", 32, MIN_FONT_SIZE, MAX_FONT_SIZE)
            .describedAs("Font size of the pages that do not start with a size of their own."));
  }

  /**
   * Returns all properties, in the order the editor shows them.
   *
   * @return count, pages and font size
   */
  List<PrefabProperty<?>> properties() {
    List<PrefabProperty<?>> properties = new ArrayList<>();
    properties.add(count);
    properties.addAll(pages);
    properties.add(fontSize);
    return List.copyOf(properties);
  }

  /**
   * Returns the used pages of an instance with their font size, as the cutscene shows them.
   *
   * @param instance instance holding the pages
   * @return one message per used page
   */
  List<Tuple<String, Integer>> messages(PrefabInstance instance) {
    int size = instance.get(fontSize);
    return PrefabTextList.values(instance, count, pages).stream()
        .map(page -> message(page, size))
        .toList();
  }

  /**
   * Returns the text of a page and its font size: its own size, else the one of all pages.
   *
   * @param page authored page, e.g. {@code "48|The End"}
   * @param size font size of the pages without a size of their own
   * @return the text without its size, and the font size
   */
  static Tuple<String, Integer> message(String page, int size) {
    Matcher matcher = OWN_SIZE.matcher(page);
    if (!matcher.matches()) return Tuple.of(page, size);
    String digits = matcher.group(1);
    // Longer numbers are too large anyway, and might not fit into an int.
    int own = digits.length() > 3 ? MAX_FONT_SIZE : Integer.parseInt(digits);
    return Tuple.of(matcher.group(2).strip(), Math.clamp(own, MIN_FONT_SIZE, MAX_FONT_SIZE));
  }

  /**
   * Returns a short editor summary: the beginning of the first page.
   *
   * @param instance instance holding the pages
   * @return preview of the first page
   */
  String summary(PrefabInstance instance) {
    return PrefabDialogs.preview(message(pages.getFirst().get(instance), 0).a());
  }
}
