package server;

public class IfStats {
  public boolean if_flag;
  public boolean else_flag;
  public IfStats(boolean if_flag) {
    this.if_flag = if_flag;
    this.else_flag = false;
  }

  public boolean executeAction() {
    return if_flag || else_flag;
  }
}
