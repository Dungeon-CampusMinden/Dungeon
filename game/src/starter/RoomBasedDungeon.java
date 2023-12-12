package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;

import contrib.crafting.Crafting;
import contrib.entities.EntityFactory;
import contrib.level.generator.graphBased.RoomBasedLevelGenerator;
import contrib.systems.*;
import contrib.utils.components.Debugger;

import core.Entity;
import core.Game;
import core.gui.GUIButton;
import core.gui.GUIContainer;
import core.gui.GUIRoot;
import core.gui.GUIText;
import core.gui.layouts.BorderLayout;
import core.gui.layouts.hints.BorderLayoutHint;
import core.gui.layouts.hints.RelativeLayoutHint;
import core.gui.math.Vector4f;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.utils.components.path.SimpleIPath;

import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RoomBasedDungeon {
    private static final String BACKGROUND_MUSIC = "sounds/background.wav";

    public static void main(String[] args) throws IOException {
        Game.initBaseLogger();
        Debugger debugger = new Debugger();
        // start the game
        configGame();
        onSetup(10, 5, 1);
        onFrame(debugger);

        // build and start game
        Game.run();
    }

    private static void onFrame(Debugger debugger) {
        Game.userOnFrame(debugger::execute);
    }

    private static void onSetup(int roomcount, int monstercount, int chestcount) {
        Game.userOnSetup(
                () -> {
                    createSystems();
                    try {
                        createHero();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    setupMusic();
                    Crafting.loadRecipes();
                    createRoomBasedLevel(roomcount, monstercount, chestcount);

                    // Create GUI
                    GUIContainer container = GUIRoot.getInstance().rootContainer();

                    GUIButton button = new GUIButton();
                    button.layout(new BorderLayout(BorderLayout.BorderLayoutMode.HORIZONTAL));
                    button.onClick(
                            (btn, event) -> {
                                if (event.action == GLFW.GLFW_PRESS) {
                                    System.out.println("Button clicked: " + event.action);
                                    btn.backgroundColor(
                                            new Vector4f(
                                                    (float) Math.random(),
                                                    (float) Math.random(),
                                                    (float) Math.random(),
                                                    1.0f));
                                }
                            });

                    GUIText buttonText = new GUIText("Test");
                    button.add(buttonText, BorderLayoutHint.CENTER);

                    container.add(button, new RelativeLayoutHint(0.1f, 0.1f, 0.125f, 0.125f));
                });
    }

    private static void createRoomBasedLevel(int roomcount, int monstercount, int chestcount) {
        // create entity sets
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
                    throw new RuntimeException(e);
                }
            }
        }
        ILevel level = RoomBasedLevelGenerator.level(entities, DesignLabel.randomDesign());
        Game.currentLevel(level);
    }

    private static void createHero() throws IOException {
        Entity hero = EntityFactory.newHero();
        Game.add(hero);
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
    }

    private static void setupMusic() {
        Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();
        backgroundMusic.setVolume(.1f);
    }

    private static void configGame() throws IOException {
        Game.loadConfig(
                new SimpleIPath("dungeon_config.json"),
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class);
        Game.frameRate(30);
        Game.disableAudio(false);
        Game.windowTitle("My Dungeon");
    }
}
