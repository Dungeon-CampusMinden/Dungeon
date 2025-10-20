package produsAdvanced;

import contrib.entities.EntityFactory;
import contrib.entities.HeroFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.VelocityComponent;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import java.io.IOException;
import java.util.logging.Level;

import core.utils.Vector2;
import produsAdvanced.abstraction.Hero;
import produsAdvanced.abstraction.portals.systems.PortalExtendSystem;
import produsAdvanced.level.LightBridgeTestLevel;

/**
 * Ein Starter zum Testen eines Levels mit einem spielbaren Charakter. Lädt alle notwendigen
 * Systeme, um das Level spielbar zu machen.
 */
public class BridgeTestStarter {

  public static void main(String[] args) {
    // Initialisiert das Spiel mit minimalem Logging
    Game.initBaseLogger(Level.WARNING);

    // Konfiguriert die grundlegenden Spieleinstellungen
    Game.frameRate(30);
    Game.windowTitle("Level Test");
    Game.disableAudio(true);

    // Definiert, was beim Start des Spiels passieren soll
    Game.userOnSetup(
      () -> {
        createSystems();
        try {
          createHero();
        } catch (IOException e) {
          throw new RuntimeException("Held konnte nicht erstellt werden: ", e);
        }
        // Fügt das Testlevel zur Level-Registry hinzu
        DungeonLoader.addLevel(Tuple.of("lightbridge", LightBridgeTestLevel.class));
        // Lädt das hinzugefügte Level
        DungeonLoader.loadLevel("lightbridge");
      });

    // Startet die Spiel-Schleife
    Game.run();
  }

  /** Fügt alle für das Spiel relevanten Systeme zum ECS hinzu. */
  private static void createSystems() {
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new LeverSystem());
    Game.add(new BlockSystem());
    Game.add(new FallingSystem());
    Game.add(new PitSystem());
    Game.add(new EventScheduler());
    Game.add(new ManaRestoreSystem());
    Game.add(new StaminaRestoreSystem());
    Game.add(new ManaBarSystem());
    Game.add(new StaminaBarSystem());
    Game.add(new Debugger());
    Game.add(new LevelEditorSystem());
    DebugDrawSystem debugDrawSystem = new DebugDrawSystem();
    Game.add(debugDrawSystem);
    debugDrawSystem.toggleHUD();
    Game.add(new PortalExtendSystem());
  }

  /** Erstellt die Spieler-Held-Entität und fügt sie dem Spiel hinzu. */
  private static void createHero() throws IOException {
    Entity hero = HeroFactory.newHero();


    // Basislaufgeschwindigkeit des Helden anpassen
    hero.fetch(VelocityComponent.class)
      .ifPresent(
        vc -> {
          vc.maxSpeed(10f);
        });


    Game.add(hero);
  }
}
