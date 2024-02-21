package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import contrib.entities.EntityFactory;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.elements.ILevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.components.path.SimpleIPath;
import generator.RoomGenerator;
import server.WebsocketServer;
import systems.VisualProgrammingSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VisualProgrammingStarter {

    private static final String BACKGROUND_MUSIC = "sounds/background.wav";
    private static WebsocketServer websocketServer;

    public static void main(String[] args) throws IOException {
        startWebsocketServer();

        Game.initBaseLogger();
        Debugger debugger = new Debugger();

        // start the game
        configGame();
        onSetup();
        //onFrame(debugger);

        // build and start game
        Game.run();
    }

    private static void startWebsocketServer() {
        //Start websocket server
        websocketServer = new WebsocketServer();
        try {
            websocketServer.start();
        } catch (Exception e){

        }
    }

    private static void onSetup() {
        Game.userOnSetup(
            () -> {
                createSystems();
                createHero();
                setupMusic();
                createRoomBasedLevel();
        });
    }

    private static void createHero() {
        Entity hero;
        try {
            hero = EntityFactory.newHeroDummy();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Game.add(hero);
    }

    private static void createSystems() {
        Game.add(new VisualProgrammingSystem());
    }

    private static void createRoomBasedLevel() {
        RoomGenerator roomG = new RoomGenerator();
        ILevel level = roomG.level( DesignLabel.randomDesign());

        // Remove trap doors
        List<Tile> exits = new ArrayList<>(level.exitTiles());
        exits.forEach(exit -> level.changeTileElementType(exit, LevelElement.FLOOR));

        Game.currentLevel(level);
    }

    private static void setupMusic() {
        Music backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();
        backgroundMusic.setVolume(.01f);
    }

    private static void configGame() throws IOException {
        Game.loadConfig(
            new SimpleIPath("dungeon_config.json"),
            contrib.configuration.KeyboardConfig.class,
            core.configuration.KeyboardConfig.class);
        Game.frameRate(30);
        Game.disableAudio(false);
        Game.windowTitle("Visual Programming");
    }


}
