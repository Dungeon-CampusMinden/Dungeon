package server;

import java.util.ArrayList;

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
