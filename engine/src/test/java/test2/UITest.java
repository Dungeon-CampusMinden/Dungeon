package test2;

import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector2i;

public class UITest extends GameWindow implements EventListener {

  public UITest() {
    super("UITest", new Vector2i(1280, 720), true, true);
  }

  public static void main(String[] args) {
    new UITest().start();
  }

  @Override
  public void init() {
    EventManager.getInstance().registerListener(this);
    this.fullscreen(true);
    this.setStateTransition(new UITestTransition(this));
    this.setState(new UITestState0(this));
  }

  @Override
  public void cleanup() {

  }

}
