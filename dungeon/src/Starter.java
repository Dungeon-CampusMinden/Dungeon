import contrib.components.InteractionComponent;
import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.systems.*;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.hud.UITools;
import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.generator.graphBased.RoombasedLevelGenerator;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;

import dslToGame.DSLEntryPoint;
import dslToGame.loadFiles.DslFileLoader;

import interpreter.DSLEntryPointFinder;

import task.Task;
import task.TaskContent;
import task.quizquestion.Quiz;
import task.quizquestion.SingleChoice;
import task.quizquestion.UIAnswerCallback;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
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
    private static DSLEntryPoint selectedPoint = null;
    private static boolean realGameStarted = false;

    public static void main(String[] args) throws IOException {
        Set<DSLEntryPoint> entryPoints = processCLIArguments(args);
        configGame();
        Game.userOnSetup(
                () -> {
                    createHero();
                    createSystems();
                    Game.currentLevel(wizardLevel());
                });
        Game.userOnLevelLoad(
                (firstTime) -> {
                    // this will be at the start of the game
                    if (firstTime && selectedPoint == null) {
                        try {
                            Game.add(wizard(selectionQuestion(entryPoints)));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
        Game.userOnFrame(
                () -> {

                    // load the task and generate the level for it
                    if (!realGameStarted && selectedPoint != null) {
                        realGameStarted = true;
                        Set<Set<Entity>> entityCollection = todo(selectedPoint);

                        // todo find better solution
                        for (Set<Entity> collection : entityCollection)
                            for (Entity e : collection)
                                e.fetch(PositionComponent.class)
                                        .ifPresent(
                                                pc ->
                                                        pc.position(
                                                                PositionComponent
                                                                        .ILLEGAL_POSITION));

                        ILevel level =
                                RoombasedLevelGenerator.level(
                                        entityCollection, DesignLabel.randomDesign());
                        Game.currentLevel(level);
                        UITools.generateNewTextDialog(
                                "You selected " + selectedPoint.displayName(),
                                "Ok",
                                "Your selection");
                    }
                });

        Game.run();
    }

    // TODO connect with DSL
    private static Set<Set<Entity>> todo(DSLEntryPoint selectedPoint) {
        int roomcount = 10;
        int chestcount = 1;
        int monstercount = 5;

        Set<Set<Entity>> entities = new HashSet<>();
        for (int i = 0; i < roomcount; i++) {
            Set<Entity> set = new HashSet<>();
            entities.add(set);
            if (i == roomcount / 2) {
                try {
                    set.add(EntityFactory.newCraftingCauldron());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            for (int j = 0; j < monstercount; j++) {
                try {
                    set.add(EntityFactory.randomMonster());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            for (int k = 0; k < chestcount; k++) {
                try {
                    set.add(EntityFactory.newChest());
                } catch (IOException e) {

                }
            }
        }
        return entities;
    }

    private static void createHero() {
        Entity hero = null;
        try {
            hero = (EntityFactory.newHero());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Game.add(hero);
        Game.hero(hero);
    }

    private static void configGame() throws IOException {
        Game.initBaseLogger();
        Game.windowTitle("DSL Dungeon");
        Game.frameRate(30);
        Game.disableAudio(true);
        Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class);
        Crafting.loadRecipes();
    }

    private static void createSystems() {
        Game.add(new AISystem());
        Game.add(new CollisionSystem());
        Game.add(new HealthSystem());
        Game.add(new XPSystem());
        Game.add(new ProjectileSystem());
        Game.add(new HealthbarSystem());
        Game.add(new HeroUISystem());
    }

    private static Set<DSLEntryPoint> processCLIArguments(String[] args) throws IOException {
        Set<DSLEntryPoint> entryPoints = new HashSet<>();
        DSLEntryPointFinder finder = new DSLEntryPointFinder();
        DslFileLoader.processArguments(args)
                .forEach(path -> finder.getEntryPoints(path).ifPresent(entryPoints::addAll));
        return entryPoints;
    }

    private static ILevel wizardLevel() {
        // default layout is:
        //
        // W W W W W
        // W F F F W
        // W F F F W
        // W F F F W
        // W W W W W
        ILevel level =
                new TileLevel(
                        new LevelElement[][] {
                            new LevelElement[] {
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                            },
                            new LevelElement[] {
                                LevelElement.WALL,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.WALL,
                            },
                            new LevelElement[] {
                                LevelElement.WALL,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.WALL,
                            },
                            new LevelElement[] {
                                LevelElement.WALL,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.FLOOR,
                                LevelElement.WALL,
                            },
                            new LevelElement[] {
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                                LevelElement.WALL,
                            }
                        },
                        DesignLabel.randomDesign());
        level.changeTileElementType(level.endTile(), LevelElement.FLOOR);
        return level;
    }

    private static SingleChoice selectionQuestion(Set<DSLEntryPoint> entryPoints) {
        SingleChoice question = new SingleChoice("Wähle deine Mission:");
        entryPoints.forEach(ep -> question.addAnswer(new PayloadTaskContent(ep)));
        return question;
    }

    private static Entity wizard(SingleChoice selectionQuestion) throws IOException {
        Entity wizard = new Entity("Selection Wizard");
        wizard.addComponent(new DrawComponent("character/wizard"));
        wizard.addComponent(new PositionComponent());
        wizard.addComponent(
                new InteractionComponent(
                        1,
                        true,
                        UIAnswerCallback.askOnInteraction(
                                selectionQuestion, setSelectedEntryPoint())));

        return wizard;
    }

    private static BiConsumer<Task, Set<TaskContent>> setSelectedEntryPoint() {
        return (task, taskContents) -> {
            selectedPoint =
                    ((PayloadTaskContent) taskContents.stream().findFirst().get()).payload();
        };
    }

    private static class PayloadTaskContent extends Quiz.Content {
        private final DSLEntryPoint payload;

        public PayloadTaskContent(DSLEntryPoint payload) {
            super(payload.displayName());
            this.payload = payload;
        }

        public DSLEntryPoint payload() {
            return payload;
        }
    }
}
