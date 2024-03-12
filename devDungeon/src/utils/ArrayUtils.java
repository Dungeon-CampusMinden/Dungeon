package utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
   */
  public static <T> List<T> getRandomElements(T[] array, int n) {
    List<T> list = Arrays.asList(array);
    Collections.shuffle(list);
    return list.subList(0, n);
  }
}
