package dungine.state;

import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.state.GameStateTransition;
import de.fwatermann.dungine.ui.UIRoot;
import de.fwatermann.dungine.ui.elements.UISpinner;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.ui.layout.AlignContent;
import de.fwatermann.dungine.ui.layout.JustifyContent;
import de.fwatermann.dungine.ui.layout.Unit;
import de.fwatermann.dungine.window.GameWindow;

/**
 * The `StateTransition` class represents a state transition in the game. It is used to display a
 * loading screen while transitioning from one game state to another.
 */
public class StateTransition extends GameStateTransition {

  public StateTransition(GameWindow window) {
    super(window);
  }

  private UIRoot ui;
  private UIText loadingText;

  @Override
  public void init() {
    this.ui = new UIRoot(this.window, this.window.size().x, this.window.size().y);

    UISpinner spinner = new UISpinner();
    spinner.color(0x3071f2FF);
    spinner.layout().width(Unit.vH(10)).height(Unit.vH(10));

    this.loadingText = new UIText(Font.defaultMonoFont(), "Loading...", 24, TextAlignment.LEFT);
    this.loadingText.layout().width(Unit.vW(45));

    this.ui
        .layout()
        .justifyContent(JustifyContent.SPACE_BETWEEN)
        .alignContent(AlignContent.FLEX_START);

    this.ui.add(this.loadingText);
    this.ui.add(spinner);
  }

  @Override
  public void render(float deltaTime, GameState to) {
    this.loadingText.text(String.format("Loading... %.1f", to.getProgress() * 100.0f));
    this.ui.render();
  }

  @Override
  public void cleanup() {}
}
