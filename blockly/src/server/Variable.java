package server;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class is used to store all important values of a variable. This class stores the variable
 * type. Depending on the type the variable has either an int as value or an int array as a value.
 */
public class Variable {
  /** Type of the variable. Will either be set to "array" or "base". */
  public String type;

  /** Integer value of the variable. */
  public int intVal;

  /** Int array value of the variable. */
  public int[] arrayVal;

  /**
   * Create a new array variable. Set the type to "array".
   *
   * @param arrayVal Integer array that will be set as the array value.
   */
  public Variable(int[] arrayVal) {
    this.type = "array";
    this.arrayVal = arrayVal;
  }

  /**
   * Create a new int variable. Set the type to "base".
   *
   * @param intVal Integer value of the array.
   */
  public Variable(int intVal) {
    this.type = "base";
    this.intVal = intVal;
  }

  @Override
  public String toString() {
    if (Objects.equals(type, "base")) {
      return String.valueOf(intVal);
    } else if (Objects.equals(type, "array")) {
      return Arrays.toString(arrayVal);
    }
    return "Unkown type";
  }
}
