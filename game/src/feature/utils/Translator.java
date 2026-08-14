package feature.utils;

import java.util.HashSet;
import java.util.Set;

/** Base translator for replacing project-specific placeholder keys in text. */
public class Translator {

  protected static final Set<String> allKeys = new HashSet<>();

  /**
   * Checks whether the given text contains any registered translation key.
   *
   * @param text text to inspect.
   * @return true if a registered key is contained in the text.
   */
  public static boolean hasKey(String text) {
    return allKeys.stream().anyMatch(text::contains);
  }

  /**
   * Registers a translation key that can be detected by translators.
   *
   * @param key translation key to register.
   */
  public static void registerKey(String key) {
    allKeys.add(key);
  }

  /**
   * Translates the given text.
   *
   * @param text text to translate.
   * @return translated text.
   */
  public String translate(String text) {
    return text;
  }
}
