package feature.prefabs.types;

import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabPort;
import feature.prefabs.PrefabProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import tools.jackson.databind.JsonNode;

/**
 * Authored list of texts on a prefab: an integer property with the used length and one text
 * property per possible entry, e.g. the answers of a multiple-choice dialog.
 */
final class PrefabTextList {

  private PrefabTextList() {}

  /**
   * Creates the entry properties of a list. Entries beyond the used length are hidden and keep
   * their values.
   *
   * @param length property storing the used length of the list
   * @param maximum maximum length of the list
   * @param key serialized key of an entry without its number, e.g. {@code "option"}
   * @param label editor label of an entry without its number, e.g. {@code "Answer"}
   * @param defaultValue default text by zero-based entry index
   * @param visible condition for showing any of the entries
   * @return one non-blank text property per possible entry
   */
  static List<PrefabProperty<String>> entries(
      PrefabProperty<Integer> length,
      int maximum,
      String key,
      String label,
      IntFunction<String> defaultValue,
      Predicate<Map<String, JsonNode>> visible) {
    List<PrefabProperty<String>> properties = new ArrayList<>(maximum);
    for (int index = 0; index < maximum; index++) {
      int number = index + 1;
      properties.add(
          PrefabProperty.string(
                  key + "-" + number,
                  label + " " + number,
                  defaultValue.apply(index),
                  value -> !value.isBlank())
              .visibleWhen(values -> visible.test(values) && usedLength(length, values) >= number));
    }
    return List.copyOf(properties);
  }

  /**
   * Returns the texts of the used entries of a list.
   *
   * @param instance instance holding the list
   * @param length property storing the used length of the list
   * @param entries entry properties of the list
   * @return texts of the entries up to the used length
   */
  static List<String> values(
      PrefabInstance instance,
      PrefabProperty<Integer> length,
      List<PrefabProperty<String>> entries) {
    return entries.subList(0, instance.get(length)).stream().map(instance::get).toList();
  }

  /**
   * Returns the editor-facing name of a port belonging to a list entry, including its text.
   *
   * @param port port of the entry
   * @param text text of the entry
   * @return name of the port followed by a preview of the text
   */
  static String portName(PrefabPort port, String text) {
    return port.displayName() + " \"" + PrefabDialogs.preview(text) + "\"";
  }

  private static int usedLength(PrefabProperty<Integer> length, Map<String, JsonNode> values) {
    JsonNode used = values.get(length.key());
    return used == null || !used.isInt() ? length.defaultValue() : used.asInt();
  }
}
