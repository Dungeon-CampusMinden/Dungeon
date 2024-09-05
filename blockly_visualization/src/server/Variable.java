package server;

import java.util.Arrays;
import java.util.Objects;

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
