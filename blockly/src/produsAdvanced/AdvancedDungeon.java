package produsAdvanced;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import components.AmmunitionComponent;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.entities.HeroFactory;
import contrib.hud.DialogUtils;
import contrib.level.DevDungeonLoader;
import contrib.level.generator.GeneratorUtils;
import contrib.systems.*;
import contrib.utils.DynamicCompiler;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.game.ECSManagment;
import core.systems.LevelSystem;
import core.utils.IVoidFunction;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import level.produs.*;
import produsAdvanced.abstraction.Hero;
import produsAdvanced.abstraction.PlayerController;
import systems.BlockSystem;
import systems.TintTilesSystem;
import utils.CheckPatternPainter;

public class AdvancedDungeon {

  public static Hero hero;
  private static final boolean DRAW_CHECKER_PATTERN = true;

  private static final SimpleIPath CONTROLLER_PATH =
      new SimpleIPath("src/produsAdvanced/riddles/MyPlayerController.java");
  private static final String CONTROLLER_CLASSNAME = "produsAdvanced.riddles.MyPlayerController";
  private static final IVoidFunction onFrame =
      new IVoidFunction() {
        @Override
        public void execute() {
          if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            try {
              Object o =
                  DynamicCompiler.loadUserInstance(
                      CONTROLLER_PATH, CONTROLLER_CLASSNAME, new Tuple<>(Hero.class, hero));
              hero.setController((PlayerController) o);
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          }
        }
      };

  public static void main(String[] args) throws IOException {
    Game.initBaseLogger(Level.WARNING);
    configGame();
    onSetup();
    Game.userOnFrame(onFrame);
    onLevelLoad();
    Game.run();
  }

  private static void onSetup() {
    // TODO check file
    Game.userOnSetup(
        () -> {
          // chapter 1
          DevDungeonLoader.addLevel(Tuple.of("level1", Chapter11Level.class));
          DevDungeonLoader.addLevel(Tuple.of("level2", Chapter12Level.class));
          DevDungeonLoader.addLevel(Tuple.of("level3", Chapter13Level.class));
          DevDungeonLoader.addLevel(Tuple.of("level4", Chapter14Level.class));
          DevDungeonLoader.addLevel(Tuple.of("level5", Chapter15Level.class));
          createSystems();
          HeroFactory.heroDeath(entity -> restart());
          try {
            createHero();
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          Crafting.loadRecipes();
          Crafting.loadRecipes();
          LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
          levelSystem.onEndTile(DevDungeonLoader::loadNextLevel);
          DevDungeonLoader.afterAllLevels(AdvancedDungeon::startRoomBasedLevel);
          DevDungeonLoader.loadLevel(0);
        });
  }

  private static void onLevelLoad() {
    Game.userOnLevelLoad(
        (firstLoad) -> {
          if (DRAW_CHECKER_PATTERN)
            CheckPatternPainter.paintCheckerPattern(Game.currentLevel().layout());
          Game.hero()
              .flatMap(e -> e.fetch(AmmunitionComponent.class))
              .map(AmmunitionComponent::resetCurrentAmmunition);
        });
  }

  private static void startRoomBasedLevel() {
    GeneratorUtils.createRoomBasedLevel(10, 5, 1);
    DialogUtils.showTextPopup(
        "Du hast alle Level erfolgreich gelöst!\nDu bist jetzt im Sandbox Modus.", "Gewonnen");

    LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
    levelSystem.onEndTile(
        AdvancedDungeon::startRoomBasedLevel); // restart the level -> endless loop
  }

  private static void configGame() throws IOException {
    Game.loadConfig(
        new SimpleIPath("dungeon_config.json"),
        contrib.configuration.KeyboardConfig.class,
        core.configuration.KeyboardConfig.class);
    Game.frameRate(30);
    Game.disableAudio(true);
    Game.resizeable(true);
    Game.windowTitle("Advanced Dungeon");
  }

  private static void createSystems() {
    Game.add(new CollisionSystem());
    Game.add(new AISystem());
    Game.add(new HealthSystem());
    Game.add(new ProjectileSystem());
    Game.add(new HealthBarSystem());
    Game.add(new HudSystem());
    Game.add(new SpikeSystem());
    Game.add(new IdleSoundSystem());
    Game.add(new PathSystem());
    Game.add(new LevelTickSystem());
    Game.add(new LeverSystem());
    Game.add(new BlockSystem());
    Game.add(new FallingSystem());
    Game.add(new PitSystem());
    Game.add(new TintTilesSystem());
    Game.add(new EventScheduler());
    Game.add(new FogSystem());
  }

  private static void createHero() throws IOException {
    Game.entityStream(Set.of(PlayerComponent.class)).forEach(Game::remove);
    Entity heroEntity = EntityFactory.newHero();
    Game.add(heroEntity);
    hero = new Hero(heroEntity);
  }

  /**
   * Restarts the game by removing all entities, recreating the hero, and reloading the current
   * level.
   *
   * <p>This effectively resets the game state to its initial configuration.
   */
  public static void restart() {
    Game.removeAllEntities();
    try {
      createHero();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    DevDungeonLoader.reloadCurrentLevel();
  }
}
