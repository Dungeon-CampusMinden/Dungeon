package dungine.state;

import de.fwatermann.dungine.ecs.systems.FreeCamSystem;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.ui.layout.Position;
import de.fwatermann.dungine.ui.layout.Unit;
import de.fwatermann.dungine.window.GameWindow;
import dungine.level.level3d.Level3D;
import dungine.level.level3d.generator.rooms.RoomsGenerator;
import dungine.util.DemoUI;

public class State3dLevel extends GameState {

  private UIText fpsText;
  private UIText chunksText;
  private Level3D level;

  public State3dLevel(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {
    this.fpsText = new UIText(Font.defaultMonoFont(), "FPS: 0", 12, TextAlignment.LEFT);
    this.chunksText = new UIText(Font.defaultMonoFont(), "Chunks: 0", 12, TextAlignment.LEFT);
    this.chunksText.layout().position(Position.FIXED).top(Unit.px(32)).left(Unit.px(10)).width(Unit.vW(45));
    this.ui.add(this.chunksText);
    DemoUI.init(this.window, this.ui, this.fpsText, "Ein 3D-Level im Dungeon-Stil. Mehr muss ich dazu glaube ich nicht sagen ;)");

    this.addSystem(new FreeCamSystem(this.camera, false, this));

    this.level = new Level3D();
    RoomsGenerator generator = new RoomsGenerator(this.level);
    this.level.generator(generator);
    generator.generate(20, 3467589736L);

    this.grid(true);
  }

  @Override
  public void renderState(float deltaTime) {
    this.fpsText.text("FPS: " + this.window.frameCounter().currentFPS());
    this.chunksText.text("Chunks: " + this.level.chunkCount());
    this.level.render(this.camera);
  }

  @Override
  public boolean loaded() {
    return true;
  }


}
