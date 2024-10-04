package server;

import java.util.ArrayList;

/**
 * This class is used to store the important values of a function. This class contains the function name and the body
 * of the function in an array list.
 */
public class FuncStats {
  public String name;
  public ArrayList<String> funcBody;

  public FuncStats(String name) {
    this.name = name;
    this.funcBody = new ArrayList<>();
  }

  @Override
  public String toString(){
    return name + "(" + funcBody.size() + " lines)";
  }
}
