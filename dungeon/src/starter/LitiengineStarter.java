package starter;

import de.gurkenlabs.litiengine.Game;

public final class LitiengineStarter {
  private LitiengineStarter() {}

  static void main(String[] args) {
    Game.info().setName("Dungeon");
    Game.info().setSubTitle("LITIENGINE bootstrap");
    Game.info().setVersion("0.0.0-dev");

    Game.init(args);
    Game.start();
  }
}
