package syntaxHighlighting;

import java.util.Arrays;
import java.util.List;

/** Utility class to provide a list of the names of the entries of an enum. */
public class EnumNamesUtil {
  /**
   * Gets the names of enum entries in the order the entries where defined.
   *
   * @param enumeration the enum class to get the names of.
   * @param <E> Type parameter to assure that the class is an enum class.
   * @return the names of enum entries in the order the entries where defined.
   */
  public static <E extends Enum<E>> List<String> getNamesInDeclarationOrder(Class<E> enumeration) {
    return Arrays.stream(enumeration.getEnumConstants()).map(Enum::name).toList();
  }
}
