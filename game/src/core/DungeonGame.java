package core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Vector2i;

import de.fwatermann.dungine.window.GameWindow;

public class DungeonGame extends GameWindow {

  private static Logger LOGGER = LogManager.getLogger();

  public DungeonGame() {
    super("Dungeon Game", new Vector2i(1280, 720), true, true);
  }

  @Override
  public void init() {
    LOGGER.info("Game init...");
    LOGGER.info("Game init done!");
  }

  @Override
  public void render(float deltaTime) {
  }

  @Override
  public void update(float deltaTime) {
  }

  @Override
  public void cleanup() {
  }

}
