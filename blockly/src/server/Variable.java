package server;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class is used to store all important values of a variable. This class stores the variable
 * type. Depending on the type the variable has either an int as value or an int array as a value.
 */
public class Variable {

  public String type;
  public int intVal;
  public int[] arrayVal;

  public Variable(int[] arrayVal) {
    this.type = "array";
    this.arrayVal = arrayVal;
  }

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
