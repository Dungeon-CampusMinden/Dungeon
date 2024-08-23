package test2;

import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.state.GameStateTransition;
import de.fwatermann.dungine.ui.UIRoot;
import de.fwatermann.dungine.ui.elements.UIColorPane;
import de.fwatermann.dungine.ui.elements.UISpinner;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.window.GameWindow;
import org.joml.Vector3f;

public class UITestTransition extends GameStateTransition {

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
    this.textLoading.size(new Vector3f(1240, 50, 0));
    this.textLoading.position(new Vector3f(20, 20, 1));

    this.progressBar = new UIColorPane(0xFF8000FF, 0xFFFFFFFF, 10.0f, 5.0f);
    this.progressBar.size().set(0, 100, 0);
    this.progressBar.position().set(0, this.window.size().y / 2.0f - 50, 0);

    this.spinner = new UISpinner();
    this.spinner.size().set(100, 100, 0);
    this.spinner.position().set(this.window.size().x - 120, 20, 0);
    this.spinner.color(0xFF8000FF);

    this.ui.add(this.textLoading);
    this.ui.add(this.progressBar);
    this.ui.add(this.spinner);
  }

  @Override
  public void render(float deltaTime, GameState to) {
    if(to != null) {
      this.textLoading.text("Loading ... " + (Math.round(to.getProgress() * 100)) + "%");
      this.progressBar.size().x = to.getProgress() * this.window.size().x;
    }
    this.ui.render();
  }

  @Override
  public void dispose() {}
}
