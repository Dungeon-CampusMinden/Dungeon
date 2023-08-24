import contrib.entities.EntityFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;

import core.Game;
import core.level.utils.LevelSize;

import dslToGame.DSLEntryPoint;
import dslToGame.loadFiles.DslFileLoader;

import interpreter.DSLEntryPointFinder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Generic Game Starter for a game that uses DSL inputs.
 *
 * <p>This will set up a basic game with all systems and a hero.
 *
 * <p>It reads command line arguments that are paths to DSL files or jars.
 *
 * <p>Not yet implemented: Letting the player select a starting point (essentially a level) from the
 * input DSL files and loading the game.
 *
 * <p>Start with "./gradlew start".
 */
public class Starter {

    private static final Logger LOGGER = Logger.getLogger(Starter.class.getName());

    public static void main(String[] args) throws IOException {
        setupBasicGame();
        processCLIArguments(args);
        Game.run();
    }

    private static void processCLIArguments(String[] args) throws IOException {
        Set<DSLEntryPoint> entryPoints = new HashSet<>();
        DSLEntryPointFinder finder = new DSLEntryPointFinder();
        DslFileLoader.processArguments(args)
                .forEach(path -> finder.getEntryPoints(path).ifPresent(entryPoints::addAll));
        // Todo Game.menu.show(entryPoints)
    }

    private static void setupBasicGame() throws IOException {
        Game.initBaseLogger();
        Debugger debugger = new Debugger();
        Game.hero(EntityFactory.newHero());
        Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class);
        Game.frameRate(30);
        Game.disableAudio(true);
        Game.userOnFrame(debugger::execute);
        Game.userOnLevelLoad(
                (firstload) -> {
                    if (firstload) {
                        try {
                            Game.add(EntityFactory.newChest());
                        } catch (IOException e) {
                            LOGGER.warning("Could not create new Chest: " + e.getMessage());
                            throw new RuntimeException();
                        }
                        for (int i = 0; i < 5; i++) {
                            try {
                                Game.add(EntityFactory.randomMonster());
                            } catch (IOException e) {
                                LOGGER.warning("Could not create new Monster: " + e.getMessage());
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    Game.levelSize(LevelSize.randomSize());
                });
        Game.windowTitle("DSL Dungeon");
        Game.add(new AISystem());
        Game.add(new CollisionSystem());
        Game.add(new HealthSystem());
        Game.add(new XPSystem());
        Game.add(new ProjectileSystem());
        Game.add(new HealthbarSystem());
        Game.add(new HeroUISystem());
    }
}
