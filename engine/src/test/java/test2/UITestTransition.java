package test2;

import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.state.GameStateTransition;
import de.fwatermann.dungine.ui.UIRoot;
import de.fwatermann.dungine.ui.elements.UIColorPane;
import de.fwatermann.dungine.ui.elements.UISpinner;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.window.GameWindow;

public class UITestTransition extends GameStateTransition implements EventListener {

  private UIRoot ui;

  private UIText textLoading;
  private UIColorPane progressBar;
  private UISpinner spinner;

  protected UITestTransition(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {
    this.ui = new UIRoot(this.window, this.window.size().x, this.window.size().y);

    this.textLoading = new UIText(Font.defaultMonoFont(), "Loading...", 60);
    this.progressBar = new UIColorPane(0xFF8000FF, 0xFFFFFFFF, 10.0f, 5.0f);
    this.spinner = new UISpinner().color(0xFF8000FF);

    this.ui.add(this.textLoading);
    this.ui.add(this.progressBar);
    this.ui.add(this.spinner);

    this.layout(this.window.size().x, this.window.size().y);

    EventManager.getInstance().registerListener(this);
  }

  private void layout(int width, int height) {
    this.textLoading.position().set(20, 20, 1);
    this.textLoading.size().set(1240, 50, 0);

    this.progressBar.position().set(0, height / 2.0f - 50, 0);
    this.progressBar.size().set(0, 100, 0);

    this.spinner.position().set(width - 120, 20, 0);
    this.spinner.size().set(100, 100, 0);
  }

  @Override
  public void render(float deltaTime, GameState to) {
    if(to != null) {
      this.textLoading.text("Loading ... " + (Math.round(to.getProgress() * 100)) + "%");
      this.progressBar.size().x = to.getProgress() * this.window.size().x;
    }
    this.ui.render();
  }

  @EventHandler
  public void onResize(WindowResizeEvent event) {
    if(!event.isCanceled()) {
      this.layout(event.to.x, event.to.y);
    }
  }


  @Override
  public void dispose() {}
}
