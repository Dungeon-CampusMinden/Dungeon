package core.utils.components.draw;

public enum DepthLayer {
  Background(-9999),
  Level(-1000),
  BackgroundDeco(-100),
  Normal(0),
  ForegroundDeco(50),
  Player(100),
  AbovePlayer(1000),
  UI(9999),
  ;

  private int depth;

  DepthLayer(int depth) {
    this.depth = depth;
  }

  public int depth() {
    return depth;
  }
}
