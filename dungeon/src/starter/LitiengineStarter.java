package starter;

import core.game.GameLoopCore;
import core.game.litiengine.LitiengineGameLoopHost;

public final class LitiengineStarter {
  private LitiengineStarter() {}

  static void main(String[] args) {
    // Initialize Dungeon runtime (logger/network) without starting libGDX.
    core.Game.initialize();

    // Start LITIENGINE host which drives the Dungeon core loop.
    LitiengineGameLoopHost.run(args, new GameLoopCore());
  }
}
