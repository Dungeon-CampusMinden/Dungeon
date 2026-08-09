package contrib.utils;

import java.util.HashSet;
import java.util.Set;

public class Translator {

  protected static final Set<String> allKeys = new HashSet<>();

  public static boolean hasKey(String text) {
    return allKeys.stream().anyMatch(text::contains);
  }

  public static void registerKey(String key) {
    allKeys.add(key);
  }

  public String translate(String text){
    return text;
  }
}
