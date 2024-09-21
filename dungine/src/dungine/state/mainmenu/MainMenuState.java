package dungine.state.mainmenu;

import de.fwatermann.dungine.audio.AudioBuffer;
import de.fwatermann.dungine.audio.AudioSource;
import de.fwatermann.dungine.event.input.MouseButtonEvent;
import de.fwatermann.dungine.graphics.SkyBox;
import de.fwatermann.dungine.graphics.text.Font;
import de.fwatermann.dungine.graphics.text.TextAlignment;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.state.LoadStepper;
import de.fwatermann.dungine.ui.UIContainer;
import de.fwatermann.dungine.ui.components.UIComponentClickable;
import de.fwatermann.dungine.ui.elements.UIButton;
import de.fwatermann.dungine.ui.elements.UIImage;
import de.fwatermann.dungine.ui.elements.UIText;
import de.fwatermann.dungine.ui.layout.AlignContent;
import de.fwatermann.dungine.ui.layout.FlexDirection;
import de.fwatermann.dungine.ui.layout.FlexWrap;
import de.fwatermann.dungine.ui.layout.JustifyContent;
import de.fwatermann.dungine.ui.layout.Unit;
import de.fwatermann.dungine.window.GameWindow;
import dungine.state.ingame.InGameState;
import org.joml.Math;

public class MainMenuState extends GameState {

  public MainMenuState(GameWindow window) {
    super(window);
  }

  private LoadStepper stepper;
  private AudioSource audioSourceBackgroundMusic;

  private UIButton buttonStart, buttonLoad, buttonOptions, buttonExit;
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

    this.stepper.step(
        "skybox",
        true,
        () -> {
          this.skyBox = new SkyBox(Resource.load("/images/skybox.png"));
        });

    this.stepper.step(
        "buttons",
        true,
        () -> {
          this.ui
              .layout()
              .flow(FlexDirection.COLUMN, FlexWrap.WRAP)
              .justifyContent(JustifyContent.SPACE_EVENLY)
              .alignContent(AlignContent.STRETCH);

          UIContainer<?> topContainer = new UIContainer<>();
          topContainer
              .layout()
              .flow(FlexDirection.ROW, FlexWrap.WRAP)
              .justifyContent(JustifyContent.CENTER)
              .alignContent(AlignContent.CENTER)
              .order(1)
              .height(Unit.percent(30));

          UIContainer<?> bottomContainer = new UIContainer<>();
          bottomContainer
              .layout()
              .order(0)
              .flexGrow(1)
              .flow(FlexDirection.COLUMN, FlexWrap.WRAP)
              .justifyContent(JustifyContent.SPACE_EVENLY)
              .alignContent(AlignContent.CENTER)
              .rowGap(Unit.px(20));

          this.logo = new UIImage(Resource.load("/images/logo.png"));
          this.logo.layout().height(Unit.percent(100)).aspectRatio(Unit.px(1.0f));
          topContainer.add(this.logo);

          this.buttonStart = new UIButton();
          this.buttonStart.fillColor(0x0000FF80).borderRadius(10);
          this.buttonStart
              .layout()
              .width(Unit.percent(50))
              .flexGrow(1)
              .order(2)
              .flow(FlexDirection.ROW, FlexWrap.WRAP)
              .alignContent(AlignContent.CENTER);
          bottomContainer.add(this.buttonStart);

          UIText buttonStartText =
              new UIText(Font.defaultFont(), "Neues Spiel", 32, TextAlignment.CENTER);
          buttonStartText.layout().flexGrow(1);
          this.buttonStart.add(buttonStartText);

          this.buttonLoad = new UIButton();
          this.buttonLoad.fillColor(0x0000FF80).borderRadius(10);
          this.buttonLoad
              .layout()
              .width(Unit.percent(50))
              .flexGrow(1)
              .order(1)
              .flow(FlexDirection.ROW, FlexWrap.WRAP)
              .alignContent(AlignContent.CENTER);
          bottomContainer.add(this.buttonLoad);

          UIText buttonLoadText =
              new UIText(Font.defaultFont(), "Spiel laden", 32, TextAlignment.CENTER);
          buttonLoadText.layout().flexGrow(1);
          this.buttonLoad.add(buttonLoadText);

          UIContainer<?> optExitContainer = new UIContainer<>();
          optExitContainer
              .layout()
              .order(0)
              .flexGrow(1)
              .flow(FlexDirection.ROW, FlexWrap.WRAP)
              .justifyContent(JustifyContent.SPACE_BETWEEN)
              .alignContent(AlignContent.STRETCH)
              .columnGap(Unit.px(20));
          bottomContainer.add(optExitContainer);

          this.buttonOptions = new UIButton();
          this.buttonOptions.fillColor(0x0000FF80).borderRadius(10);
          this.buttonOptions
              .layout()
              .flexGrow(1)
              .flow(FlexDirection.ROW, FlexWrap.WRAP)
              .alignContent(AlignContent.CENTER);
          optExitContainer.add(this.buttonOptions);

          UIText buttonOptionsText =
              new UIText(Font.defaultFont(), "Optionen", 32, TextAlignment.CENTER);
          buttonOptionsText.layout().flexGrow(1);
          this.buttonOptions.add(buttonOptionsText);

          this.buttonExit = new UIButton();
          this.buttonExit.fillColor(0x0000FF80).borderRadius(10);
          this.buttonExit
              .layout()
              .flexGrow(1)
              .flow(FlexDirection.ROW, FlexWrap.WRAP)
              .alignContent(AlignContent.CENTER);
          optExitContainer.add(this.buttonExit);

          UIText buttonExitText =
              new UIText(Font.defaultFont(), "Beenden", 32, TextAlignment.CENTER);
          buttonExitText.layout().flexGrow(1);
          this.buttonExit.add(buttonExitText);

          this.ui.add(topContainer);
          this.ui.add(bottomContainer);

          this.buttonStart.attachComponent(
              new UIComponentClickable(
                  (element, button, action) -> {
                    if (button == 0 && action == MouseButtonEvent.MouseButtonAction.PRESS) {
                      this.window.setState(new InGameState(this.window));
                    }
                  }));
          this.buttonExit.attachComponent(
              new UIComponentClickable(
                  (element, button, action) -> {
                    if (button == 0 && action == MouseButtonEvent.MouseButtonAction.PRESS) {
                      this.window.close();
                    }
                  }));
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
          // this.audioSourceBackgroundMusic.play();
          this.loaded = true;
        });

    this.stepper.start();
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
