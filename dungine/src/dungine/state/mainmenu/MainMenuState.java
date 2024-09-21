package dungine.state.mainmenu;

import de.fwatermann.dungine.audio.AudioBuffer;
import de.fwatermann.dungine.audio.AudioSource;
import de.fwatermann.dungine.graphics.SkyBox;
import de.fwatermann.dungine.resource.Resource;
import de.fwatermann.dungine.state.GameState;
import de.fwatermann.dungine.state.LoadStepper;
import de.fwatermann.dungine.ui.elements.UIButton;
import de.fwatermann.dungine.ui.elements.UIColorPane;
import de.fwatermann.dungine.ui.elements.UIImage;
import de.fwatermann.dungine.ui.layout.AlignContent;
import de.fwatermann.dungine.ui.layout.FlexDirection;
import de.fwatermann.dungine.ui.layout.FlexWrap;
import de.fwatermann.dungine.ui.layout.JustifyContent;
import de.fwatermann.dungine.ui.layout.Unit;
import de.fwatermann.dungine.window.GameWindow;
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

    this.stepper.step("skybox", true, () -> {
      this.skyBox = new SkyBox(Resource.load("/images/skybox.png"));
    });

    this.stepper.step(
        "buttons",
        true,
        () -> {
          this.ui.layout()
            .flow(FlexDirection.COLUMN, FlexWrap.WRAP)
            .justifyContent(JustifyContent.SPACE_EVENLY)
            .alignContent(AlignContent.STRETCH);

          UIColorPane topContainer = new UIColorPane(0xFF000080);
          topContainer.layout()
            .flow(FlexDirection.ROW, FlexWrap.WRAP)
            .justifyContent(JustifyContent.CENTER)
            .alignContent(AlignContent.CENTER)
            .order(1)
            .height(Unit.percent(30));

          UIColorPane logoPlaceholder = new UIColorPane(0xFF800080);
          logoPlaceholder.layout().width(Unit.percent(50)).height(Unit.percent(50));

          this.logo = new UIImage(Resource.load("/images/logo.png"));
          this.logo.layout().width(Unit.percent(50)).height(Unit.percent(50));
          topContainer.add(this.logo);

          UIColorPane bottomContainer = new UIColorPane(0x00FF0080);
          bottomContainer.layout()
            .order(0)
            .flexGrow(1)
            .flow(FlexDirection.COLUMN, FlexWrap.WRAP)
            .justifyContent(JustifyContent.SPACE_EVENLY)
            .alignContent(AlignContent.CENTER)
            .rowGap(Unit.px(20));

          this.buttonStart = new UIButton();
          this.buttonStart.fillColor(0x0000FF80).borderRadius(10);
          this.buttonStart.layout().width(Unit.percent(50)).flexGrow(1).order(2);
          bottomContainer.add(this.buttonStart);

          this.buttonLoad = new UIButton();
          this.buttonLoad.fillColor(0x0000FF80).borderRadius(10);
          this.buttonLoad.layout().width(Unit.percent(50)).flexGrow(1).order(1);
          bottomContainer.add(this.buttonLoad);

          UIColorPane optExitContainer = new UIColorPane(0xFF800080);
          optExitContainer.layout()
            .order(0)
            .flexGrow(1)
            .flow(FlexDirection.ROW, FlexWrap.WRAP)
            .justifyContent(JustifyContent.SPACE_BETWEEN)
            .alignContent(AlignContent.STRETCH)
              .columnGap(Unit.px(20));
          bottomContainer.add(optExitContainer);

          this.buttonOptions = new UIButton();
          this.buttonOptions.fillColor(0x0000FF80).borderRadius(10);
          this.buttonOptions.layout().flexGrow(1);
          optExitContainer.add(this.buttonOptions);

          this.buttonExit = new UIButton();
          this.buttonExit.fillColor(0x0000FF80).borderRadius(10);
          this.buttonExit.layout().flexGrow(1);
          optExitContainer.add(this.buttonExit);

          this.ui.add(topContainer);
          this.ui.add(bottomContainer);


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
          //this.audioSourceBackgroundMusic.play();
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
