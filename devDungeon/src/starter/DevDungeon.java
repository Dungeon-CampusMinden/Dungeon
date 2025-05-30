package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.crafting.Crafting;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.game.ECSManagment;
import core.systems.LevelSystem;
import core.utils.components.path.SimpleIPath;
import entities.DevHeroFactory;
import java.io.IOException;
import java.util.logging.Level;
import level.utils.DungeonLoader;
import systems.*;
import systems.DevHealthSystem;
import systems.EventScheduler;

public class DevDungeon {

    public static final DungeonLoader DUNGEON_LOADER =
        new DungeonLoader(
            new String[] {
                "tutorial", "damagedBridge", "torchRiddle", "illusionRiddle", "bridgeGuard", "finalBoss"
            });

    private static final String BACKGROUND_MUSIC = "sounds/background.wav";

    // Direkt mit TorchRiddle-Level starten
    private static final boolean SKIP_TUTORIAL = true;

    public static void main(String[] args) throws IOException {
        Game.initBaseLogger(Level.WARNING);
        configGame();
        onSetup();

        Game.userOnLevelLoad(
            (firstTime) -> {
                FogOfWarSystem fogOfWarSystem = (FogOfWarSystem) Game.systems().get(FogOfWarSystem.class);
                fogOfWarSystem.reset();
                EventScheduler.getInstance().clear();
                LeverSystem leverSystem = (LeverSystem) Game.systems().get(LeverSystem.class);
                leverSystem.clear();
                TeleporterSystem.getInstance().clearTeleporters();
            });

        Game.run();
        Game.windowTitle("Dev Dungeon");
    }

    private static void onSetup() {
        Game.userOnSetup(
            () -> {
                LevelSystem levelSystem = (LevelSystem) ECSManagment.systems().get(LevelSystem.class);
                levelSystem.onEndTile(DUNGEON_LOADER::loadNextLevel);

                createSystems();

                FogOfWarSystem fogOfWarSystem = (FogOfWarSystem) Game.systems().get(FogOfWarSystem.class);
                fogOfWarSystem.active(false);

                try {
                    createHero();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                setupMusic();
                Crafting.loadRecipes();

                if (SKIP_TUTORIAL) {
                    DUNGEON_LOADER.loadLevel(DUNGEON_LOADER.levelOrder()[2]);
                } else {
                    DUNGEON_LOADER.loadLevel(DUNGEON_LOADER.levelOrder()[0]);
                }
            });
    }

    private static void createHero() throws IOException {
        Entity hero = DevHeroFactory.newHero();
        Game.add(hero);
    }

    private static void setupMusic() {
        Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();
        backgroundMusic.setVolume(.05f);
    }

    private static void configGame() throws IOException {
        Game.loadConfig(
            new SimpleIPath("dungeon_config.json"),
            contrib.configuration.KeyboardConfig.class,
            core.configuration.KeyboardConfig.class);
        Game.frameRate(30);
        Game.disableAudio(false);
        Game.windowTitle("DevDungeon");
    }

    private static void createSystems() {
        Game.add(new CollisionSystem());
        Game.add(new AISystem());
        Game.add(new ReviveSystem());
        Game.add(new DevHealthSystem());
        Game.add(new ProjectileSystem());
        Game.add(new HealthBarSystem());
        Game.add(new HudSystem());
        Game.add(new SpikeSystem());
        Game.add(new IdleSoundSystem());
        Game.add(new FallingSystem());
        Game.add(new PathSystem());
        Game.add(new LevelTickSystem());
        Game.add(new PitSystem());
        Game.add(TeleporterSystem.getInstance());
        Game.add(EventScheduler.getInstance());
        Game.add(new FogOfWarSystem());
        Game.add(new LeverSystem());
        Game.add(new MobSpawnerSystem());
        Game.add(new MagicShieldSystem());
    }
}
