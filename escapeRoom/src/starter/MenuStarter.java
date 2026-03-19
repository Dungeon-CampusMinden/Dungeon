package starter;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import core.Game;
import core.game.PreRunConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public   class MenuStarter extends ApplicationAdapter {

  private Table mainMenuTable = null;
  private Skin skin;
  private Table settingsMenuTable;
  private Table levelTable;
  private TextureRegionDrawable menuBackground;
  private static Stage stage;

  public static void main(String args[]) {
    // 1. Die Konfiguration für das Fenster erstellen
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

    // 2. Fenstereigenschaften festlegen
    config.setTitle("Mein Rotes Fenster");
    config.setWindowedMode(800, 600); // Breite und Höhe
    config.setForegroundFPS(60);      // Framerate begrenzen
    config.useVsync(true);            // Ruckeln verhindern

    // 3. Die App starten: Verbindet die Konfiguration mit deiner Logik-Klasse
    new Lwjgl3Application(new MenuStarter(), config);


  }

  private static ArrayList<Starter> starters = new ArrayList<>();
  public static void register(Starter starter) {
    System.out.println("registered");
    starters.add(starter);
  }

  @Override
  public void create() {
    skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
    setupStage();
    createMainMenu();
  }

  @Override
  public void render() {
    ScreenUtils.clear(0, 0, 0, 1);

    // 2. Das Menü-Logik-Update (prüfen, ob mainMenuTable existiert)
    if (stage != null) {
      // Zeit vergehen lassen für Animationen/Input
      stage.act(Gdx.graphics.getDeltaTime());
      // DIE BÜHNE ZEICHNEN! (Das hat gefehlt)
      stage.draw();
    }
  }


  @Override
  public void dispose() {
  }


  private static void setupStage() {
    stage =
      new Stage(
        new ScalingViewport(
          Scaling.stretch,
          PreRunConfiguration.windowWidth(),
          PreRunConfiguration.windowHeight()),
        new SpriteBatch());
    Gdx.input.setInputProcessor(stage);
  }

  private void createMainMenu() {

    System.out.println("length of arry list"  + starters.size());

    mainMenuTable = new Table();
    mainMenuTable.setFillParent(true);

    Texture bgTexture = new Texture(Gdx.files.internal("blumenwiese.png"));
    menuBackground = new TextureRegionDrawable(new TextureRegion(bgTexture));
    mainMenuTable.setBackground(menuBackground);;

    mainMenuTable.center(); // Menü zentrieren

    TextButton startButton = new TextButton("Start", skin);
    TextButton exitButton  = new TextButton("Exit", skin);

    // Start Button startet das Spiel
    startButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Game.exit();

//        try {
//          new DemoRoom().run();
//        } catch (IOException e) {
//          throw new RuntimeException(e);
//        }

        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";

        // 2. Den aktuellen Classpath auslesen (damit DemoRoom gefunden wird)
        String classpath = System.getProperty("java.class.path");

        // 3. Den neuen Prozess konfigurieren
        // "starter.DemoRoom" muss der EXAKTE Pfad zu deiner Klasse sein
        ProcessBuilder builder = new ProcessBuilder(
          javaBin,
          "-cp", classpath,
          "starter.DemoRoom"
        );

        // 4. Arbeitsverzeichnis setzen (Wichtig für Assets wie Sounds/Bilder)
        builder.directory(new File(System.getProperty("user.dir")));

        // 5. Fehler/Ausgaben vom Spiel in deine jetzige Konsole leiten
        builder.inheritIO();

        // 6. Das neue Spiel starten
        try {
          builder.start();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }

        // 7. Das Menü-Fenster schließen
        Gdx.app.exit();

      }
    });

//    // Exit Button schließt das Spiel
    exitButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Game.exit("User Exit");
      }
    });

    mainMenuTable.add(startButton).pad(10).width(200);
    mainMenuTable.row();
    mainMenuTable.add(exitButton).pad(10).width(200);
    mainMenuTable.row();

    stage.addActor(mainMenuTable);




  }


}
