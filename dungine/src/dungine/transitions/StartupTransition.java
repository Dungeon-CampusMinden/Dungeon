package dungine.transitions;

import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.state.GameStateTransition;
import de.fwatermann.dungine.ui.UIRoot;
import de.fwatermann.dungine.ui.elements.UIImage;
import de.fwatermann.dungine.ui.elements.UISpinner;
import de.fwatermann.dungine.window.GameWindow;

public class StartupTransition extends GameStateTransition implements EventListener {

  public StartupTransition(GameWindow window) {
    super(window);
  }

  private UIRoot ui;
  private UISpinner spinner;
  private UIImage logo;

  @Override
  public void init() {
    this.window.clearColor(0x000000FF);

    this.ui = new UIRoot(this.window, this.window.size().x, this.window.size().y);
    this.logo = new UIImage(Resource.load("/images/logo.png"));
    this.spinner = new UISpinner().color(0xFFFFFFFF);

    this.ui.add(this.logo);
    this.ui.add(this.spinner);

    this.layout(this.window.size().x, this.window.size().y);

    EventManager.getInstance().registerListener(this);
  }

  private void layout(int width, int height) {
    this.ui.size().set(width, height, 0);

    this.logo.size().set(width / 6.0f, width / 6.0f, 0);
    this.logo
        .position()
        .set(
            width / 2.0f - this.logo.size().x / 2.0f, height / 2.0f - this.logo.size().y / 2.0f, 0);

    this.spinner.size().set(100, 100, 0);
    this.spinner.position().set(width - 120, 20, 0);
  }

  @Override
  public void render(float deltaTime, GameState to) {
    this.ui.render();
  }

  @EventHandler
  public void onWindowResize(WindowResizeEvent event) {
    this.layout(event.to.x, event.to.y);
  }

  @Override
  public void cleanup() {
    EventManager.getInstance().unregisterListener(this);
    this.ui.dispose();
  }
}
