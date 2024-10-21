package dungine.state.hero.level;


public class Tile {

  private int x;
  private int y;
  private int entry;

  public Tile(int x, int y, int entry) {
    this.x = x;
    this.y = y;
    this.entry = entry;
  }

  public int x() {
    return this.x;
  }

  public int y() {
    return this.y;
  }

  public int entry() {
    return this.entry;
  }
}
