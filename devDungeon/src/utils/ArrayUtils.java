package utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** The ArrayUtils class provides utility methods for working with arrays. */
public class ArrayUtils {
  /**
   * This method is used to get a specified number of random elements from an array. It first
   * converts the array to a list, then shuffles the list to randomize the order of elements.
   * Finally, it returns a sublist containing the first 'n' elements from the shuffled list.
   *
   * @param <T> the type of elements in the array
   * @param array the array from which to get random elements
   * @param n the number of random elements to get
   * @return a list containing 'n' random elements from the array
   * @throws IllegalArgumentException if 'n' is greater than the length of the array or less than 0
   */
  public static <T> List<T> getRandomElements(T[] array, int n) {
    if (n > array.length || n < 0) {
      throw new IllegalArgumentException(
          "Invalid number of elements to get. It must be between 0 and the length of the array.");
    }
    List<T> list = Arrays.asList(array);
    Collections.shuffle(list);
    return list.subList(0, n);
  }
}
