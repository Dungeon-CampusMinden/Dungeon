package server;

import java.util.ArrayList;

/**
 * This class is used to store the important values of a function. This class contains the function
 * name and the body of the function in an array list.
 */
public class FuncStats {
  /** Name of the function. */
  public String name;

  /**
   * Body of the function. Contains all actions that need to be performed when this function will be
   * called.
   */
  public ArrayList<String> funcBody;

  /**
   * Create a new function with the given name.
   *
   * @param name Name of the function.
   */
  public FuncStats(String name) {
    this.name = name;
    this.funcBody = new ArrayList<>();
  }

  @Override
  public String toString() {
    return name + "(" + funcBody.size() + " lines)";
  }
}
