package feature.prefabs.types;

import engine.utils.Tuple;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Authored pages of a text cutscene, e.g. an intro: the number of pages, one text per page and the
 * font size.
 *
 * @param count number of used pages
 * @param pages text of every possible page, only shown up to {@link #count}
 * @param fontSize font size of all pages
 */
record PrefabPages(
    PrefabProperty<Integer> count,
    List<PrefabProperty<String>> pages,
    PrefabProperty<Integer> fontSize) {

  /** Maximum number of pages. */
  static final int MAX_PAGES = 12;

  /**
   * Creates the properties of a page list.
   *
   * @return the page properties
   */
  static PrefabPages create() {
    PrefabProperty<Integer> count =
        PrefabProperty.integer("page-count", "Page Count", 2, 1, MAX_PAGES);
    return new PrefabPages(
        count,
        PrefabTextList.entries(
            count, MAX_PAGES, "page", "Page", index -> "Page " + (index + 1), values -> true),
        PrefabProperty.integer("font-size", "Font Size", 32, 12, 96));
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
        .map(text -> Tuple.of(text, size))
        .toList();
  }

  /**
   * Returns a short editor summary: the beginning of the first page.
   *
   * @param instance instance holding the pages
   * @return preview of the first page
   */
  String summary(PrefabInstance instance) {
    return PrefabDialogs.preview(pages.getFirst().get(instance));
  }
}
