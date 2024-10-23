package dungine.state;

import de.fwatermann.dungine.ecs.Entity;
import de.fwatermann.dungine.ecs.components.RenderableComponent;
import de.fwatermann.dungine.ecs.systems.RenderableSystem;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.input.KeyboardEvent;
import de.fwatermann.dungine.graphics.SkyBox;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.ui.layout.Position;
import de.fwatermann.dungine.ui.layout.Unit;
import de.fwatermann.dungine.window.GameWindow;
import dungine.level.level3d.Level3D;
import dungine.level.level3d.generator.rooms.RoomsGenerator;
import dungine.systems.CameraSystem;
import dungine.systems.PlayerSystem;
import dungine.systems.VelocitySystem;
import dungine.util.DemoUI;
import dungine.util.HeroFactory;
import org.lwjgl.glfw.GLFW;

public class State3dLevel extends GameState {

  private UIText fpsText;
  private UIText chunksText;
  private Level3D level;

  private Entity hero;
  private Entity levelEntity;

  public State3dLevel(GameWindow window) {
    super(window);
  }

  @Override
  public void init() {
    this.fpsText = new UIText(Font.defaultMonoFont(), "FPS: 0", 12, TextAlignment.LEFT);
    this.chunksText = new UIText(Font.defaultMonoFont(), "Chunks: 0", 12, TextAlignment.LEFT);
    this.chunksText.layout().position(Position.FIXED).top(Unit.px(32)).left(Unit.px(10)).width(Unit.vW(45));
    this.ui.add(this.chunksText);
    DemoUI.init(this.window, this.ui, this.fpsText, "Ein 3D-Level im Dungeon-Stil. Mehr muss ich dazu glaube ich nicht sagen ;)\nEs existieren nur noch keine Gegner die einen hier erwarten könnten... Die Fortbewegung funktioniert hier auch wieder mit 'W','A','S','D'.");

    VelocitySystem velocitySystem = new VelocitySystem();

    this.addSystem(new CameraSystem(this.camera));
    this.addSystem(new RenderableSystem(this.camera));
    this.addSystem(new PlayerSystem());
    this.addSystem(velocitySystem);

    this.level = new Level3D();
    RoomsGenerator generator = new RoomsGenerator(this.level, System.currentTimeMillis());
    this.level.generator(generator);
    generator.generate();

    velocitySystem.level = this.level;

    this.skyBox = new SkyBox(Resource.load("/textures/skybox.png"));

    this.hero = HeroFactory.create();
    HeroFactory.makeControlled(this.window, this.hero);
    this.hero.position(generator.getStartPosition());

    this.levelEntity = new Entity();
    this.levelEntity.addComponent(new RenderableComponent(this.level));

    this.addEntity(this.hero);
    this.addEntity(this.levelEntity);
  }

  @Override
  public void renderState(float deltaTime) {
    this.fpsText.text("FPS: " + this.window.frameCounter().currentFPS());
    this.chunksText.text("Chunks: " + this.level.chunkCount());
  }

  @EventHandler
  private void onKeyboard(KeyboardEvent event) {
    if(event.action == KeyboardEvent.KeyAction.PRESS ){
      if(event.key == GLFW.GLFW_KEY_R) {
        this.level.chunkByWorldCoordinates(
                (int)Math.floor(this.camera.position().x),
                (int)Math.floor(this.camera.position().y),
                (int)Math.floor(this.camera.position().z),
                false).rebuild();
      }
    }
  }

  @Override
  public boolean loaded() {
    return true;
  }


}
