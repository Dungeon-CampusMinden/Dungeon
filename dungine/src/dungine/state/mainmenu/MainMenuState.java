package dungine.state.mainmenu;

import de.fwatermann.dungine.audio.AudioBuffer;
import de.fwatermann.dungine.audio.AudioSource;
import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.graphics.SkyBox;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.state.LoadStepper;
import de.fwatermann.dungine.ui.UIElement;
import de.fwatermann.dungine.ui.components.UIComponentClickable;
import de.fwatermann.dungine.ui.elements.UIButton;
import de.fwatermann.dungine.ui.elements.UIImage;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.utils.BoundingBox2D;
import de.fwatermann.dungine.window.GameWindow;
import dungine.state.ingame.InGameState;
import org.joml.Math;

public class MainMenuState extends GameState {

  public MainMenuState(GameWindow window) {
    super(window);
  }

  private LoadStepper stepper;
  private AudioSource audioSourceBackgroundMusic;

  private UIButton buttonStart;
  private UIText buttonStartText;

  private UIButton buttonEnd;
  private UIText buttonEndText;

  private UIImage logo;

  private boolean loaded = false;

  @Override
  public void init() {

    this.window.vsync(true);

    this.stepper = new LoadStepper(this.window);

    this.stepper.step(
        "backgroundMusic",
        true,
        () -> {
          this.audioSourceBackgroundMusic =
              this.audioContext.createSource("mainMenuBackgroundMusic", true, true);
          AudioBuffer buffer =
              this.audioContext.createBuffer(
                  Resource.load("/sounds/music/main_menu.ogg"),
                  AudioBuffer.AudioFileType.OGGVorbis);
          this.audioSourceBackgroundMusic.setBuffer(buffer);
          this.audioSourceBackgroundMusic.gain(0.25f);
        });

    this.stepper.step("skybox", true, () -> {
      this.skyBox = new SkyBox(Resource.load("/images/skybox.png"));
    });

    this.stepper.step(
        "buttons",
        true,
        () -> {
          this.buttonStart = new UIButton();
          this.buttonEnd = new UIButton();
          this.logo = new UIImage(Resource.load("/images/logo.png"));

          this.buttonStart.attachComponent(
              new UIComponentClickable(
                  (UIElement<?> clicked) -> {
                    this.window.setState(new InGameState(this.window));
                  }));

          this.buttonEnd.attachComponent(new UIComponentClickable((UIElement<?> clicked) -> {
            this.window.close();
          }));

          this.buttonStart.fillColor(0x37804aFF).borderRadius(10).borderWidth(5.0f).borderColor(0x1e4729FF);
          this.buttonEnd.fillColor(0x4a0404FF).borderRadius(10).borderWidth(5.0f).borderColor(0x290202FF);

          this.buttonStartText = new UIText(Font.defaultFont(), "Start", 32);
          this.buttonStart.add(this.buttonStartText);

          this.buttonEndText = new UIText(Font.defaultFont(), "Beenden", 32);
          this.buttonEnd.add(this.buttonEndText);

          this.ui.add(this.buttonStart);
          this.ui.add(this.buttonEnd);
          this.ui.add(this.logo);

          this.layout(this.window.size().x, this.window.size().y);
        });

    this.stepper.step(
        "sleep",
        false,
        () -> {
          try {
            Thread.sleep(200);
          } catch (InterruptedException ignored) {
          }
        });

    this.stepper.done(
        true,
        (results) -> {
          this.audioSourceBackgroundMusic.play();
          this.loaded = true;
        });

    this.stepper.start();
  }

  private void layout(int width, int height) {
    //TODO: Mach schön!

    this.buttonStart.size().set(width / 3.0f, 100, 0);
    this.buttonStart.position().set(width / 2.0f - this.buttonStart.size().x / 2.0f, height / 2.0f - 100, 0);

    this.buttonEnd.size().set(width / 3.0f, 100, 0);
    this.buttonEnd.position().set(width / 2.0f - this.buttonEnd.size().x / 2.0f, height / 2.0f - 250, 0);

    this.logo.size().set(width / 6.0f, width / 6.0f, 0);
    this.logo.position().set(width / 2.0f - this.logo.size().x / 2.0f, height / 2.0f, 0);

    BoundingBox2D startTextBB = this.buttonStartText.font().calculateBoundingBox(this.buttonStartText.text(), 32, Math.round(this.buttonStart.size().x));
    this.buttonStartText.size().set(startTextBB.width(), startTextBB.height(), 0);
    this.buttonStartText.position().set(this.buttonStart.size().x / 2 - startTextBB.width() / 2.0f, this.buttonStart.size().y / 2 - startTextBB.height() / 2.0f, 1);

    BoundingBox2D endTextBB = this.buttonEndText.font().calculateBoundingBox(this.buttonEndText.text(), 32, Math.round(this.buttonEnd.size().x));
    this.buttonEndText.size().set(endTextBB.width(), endTextBB.height(), 0);
    this.buttonEndText.position().set(this.buttonEnd.size().x / 2 - endTextBB.width() / 2.0f, this.buttonEnd.size().y / 2 - endTextBB.height() / 2.0f, 1);
  }

  @EventHandler
  public void onResize(WindowResizeEvent event) {
    if (!event.isCanceled()) {
      this.layout(event.to.x, event.to.y);
    }
  }

  @Override
  public void updateState(float deltaTime) {
    this.camera.yaw(Math.toRadians(5) * deltaTime);
  }

  @Override
  public boolean loaded() {
    return this.loaded;
  }

  @Override
  public float getProgress() {
    return this.stepper.currentStep() / (float) this.stepper.stepCount();
  }
}
