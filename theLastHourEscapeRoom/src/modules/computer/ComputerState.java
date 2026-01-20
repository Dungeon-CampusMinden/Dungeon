package modules.computer;

public enum ComputerState {
  PRE_LOGIN(0),
  LOGGED_IN(1),
  ;

  private final int progress;
  ComputerState(int progress){
    this.progress = progress;
  }

  public int progress(){
    return progress;
  }

  public boolean hasReached(ComputerState other){
    return this.progress >= other.progress;
  }

}
