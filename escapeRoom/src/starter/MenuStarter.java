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
import java.util.HashMap;

public   class MenuStarter extends ApplicationAdapter {

  private Table mainMenuTable = null;
  private Skin skin;
  private Table settingsMenuTable;
  private Table levelTable;
  private TextureRegionDrawable menuBackground;
  private static Stage stage;
  private static HashMap<Starter, String> starters = new HashMap<>();
  private static Lwjgl3ApplicationConfiguration config;

  public static void main(String args[]) {
    starters.put(new MushRoom(), "MushRoom");
    starters.put(new DemoRoom(), "DemoRoom");
    starters.put(new SpriteTestRoom(), "SpriteTestRoom");
    // 1. Die Konfiguration für das Fenster erstellen
     config = new Lwjgl3ApplicationConfiguration();
    // 2. Fenstereigenschaften festlegen
    config.setTitle("Mein Rotes Fenster");
    config.setWindowedMode(800, 600); // Breite und Höhe
    config.setForegroundFPS(60);      // Framerate begrenzen
    config.useVsync(true);            // Ruckeln verhindern

    // 3. Die App starten: Verbindet die Konfiguration mit deiner Logik-Klasse
    new Lwjgl3Application(new MenuStarter(), config);
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
    System.out.println("Anzahl der Starter: " + starters.size());

    mainMenuTable = new Table();
    mainMenuTable.setFillParent(true);

    Texture bgTexture = new Texture(Gdx.files.internal("blumenwiese.png"));
    menuBackground = new TextureRegionDrawable(new TextureRegion(bgTexture));
    mainMenuTable.setBackground(menuBackground);
    mainMenuTable.center();

    // ITERATION ÜBER DIE HASHMAP
    // Wir gehen durch jedes Paar (Starter und sein Name)
    for (HashMap.Entry<Starter, String> entry : starters.entrySet()) {
      Starter starter = entry.getKey();
      String name = entry.getValue();

      // Button mit dem Namen aus der Map erstellen
      TextButton dynamicButton = new TextButton(name, skin);

      dynamicButton.addListener(new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
          // Das aktuelle Menü beenden
          Game.exit();

          // Den Starter in einem neuen Thread ausführen (dein "Warten"-Trick)
          new Thread(() -> {
            try {
              Thread.sleep(1000);
              // Führt die run() Methode des jeweiligen Starters aus
              starter.run();

              System.out.println("Spiel wurde beendet! Starte Menü neu...");

              // Menü wieder öffnen
              Gdx.app.postRunnable(() -> {
                // Hier rufst du die Main-Klasse deines Menüs wieder auf
                // oder startest eine neue Lwjgl3Application
                new Lwjgl3Application(new MenuStarter(), config);
              });


            } catch (InterruptedException | IOException e) {
              e.printStackTrace();
            }
          }).start();
        }
      });

      // Button zur Tabelle hinzufügen
      mainMenuTable.add(dynamicButton).pad(10).width(200);
      mainMenuTable.row(); // Nächste Zeile für den nächsten Button
    }

    // Am Ende noch den Exit-Button (optional)
    TextButton exitButton = new TextButton("Exit", skin);
    exitButton.addListener(new ClickListener() {
      @Override
      public void clicked(InputEvent event, float x, float y) {
        Game.exit("User Exit");
      }
    });

    mainMenuTable.add(exitButton).pad(30, 10, 10, 10).width(200); // Etwas mehr Abstand nach oben
    stage.addActor(mainMenuTable);
  }


}
