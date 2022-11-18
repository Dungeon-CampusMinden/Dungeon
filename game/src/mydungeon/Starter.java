package mydungeon;

import basiselements.DungeonElement;
import basiselements.hud.ScreenText;
import character.monster.Imp;
import character.monster.Monster;
import character.objects.*;
import character.player.Hero;
import collision.CharacterDirection;
import collision.CollisionMap;
import controller.Game;
import controller.ScreenController;
import dslToGame.QuestConfig;
import interpreter.DSLInterpreter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import level.elements.ILevel;
import level.elements.tile.DoorTile;
import level.elements.tile.Tile;
import level.tools.LevelElement;
import quest.Quest;
import quest.QuestFactory;
import room.Room;
import starter.DesktopLauncher;

/**
 * The entry class to create your own implementation.
 *
 * <p>This class is directly derived form {@link Game} and acts as the {@link
 * com.badlogic.gdx.Game}.
 */
public class Starter extends Game {
    private Hero hero;
    private List<Monster> monster;
    private List<TreasureChest> chests;
    private ScreenController sc;
    private CollisionMap clevel;
    private List<PasswordChest> pwChest;

    private Letter letter;
    private DSLInterpreter dslInterpreter;
    private Quest quest;
    private ScreenText questInfo;

    @Override
    protected void setup() {
        dslInterpreter = new DSLInterpreter();
        QuestConfig config = loadConfig();

        clevel = new CollisionMap();
        monster = new ArrayList<>();
        pwChest = new ArrayList<>();
        chests = new ArrayList<>();
        hero = new Hero();
        sc = new ScreenController(batch);
        controller.add(sc);

        quest = QuestFactory.generateQuestFromConfig(config, sc);
        generator = quest.getGenerator();

        levelAPI.setGenerator(generator);
        levelAPI.loadLevel();

        quest.setRootLevel(levelAPI.getCurrentLevel());
        quest.addQuestObjectsToLevels();
        onLevelLoad();

        hero.getHitbox().setCollidable(hero);
        camera.follow(hero);
        entityController.add(hero);
        quest.addQuestUIElements();
    }

    @Override
    protected void frame() {
        Tile currentTile = levelAPI.getCurrentLevel().getTileAtEntity(hero);
        if (currentTile.getLevelElement() == LevelElement.EXIT) levelAPI.loadLevel();
        else if (currentTile.getLevelElement() == LevelElement.DOOR) {
            DoorTile otherDoor = ((DoorTile) currentTile).getOtherDoor();
            currentTile.onEntering(hero);
            levelAPI.setLevel(otherDoor.getLevel());
        } else checkForCollision();
    }

    private void checkForCollision() {
        for (Monster m : monster) {
            CharacterDirection direction = hero.getHitbox().collide(m.getHitbox());
            if (direction != CharacterDirection.NONE) {
                hero.colide(m, direction);
                m.colide(hero, direction);
            }
        }
        CharacterDirection direction;
        for (PasswordChest p : pwChest) {
            direction = hero.getHitbox().collide(p.getHitbox());
            hero.colide(p, direction);
            p.colide(hero, direction);
        }
        for (TreasureChest t : chests) {
            direction = hero.getHitbox().collide(t.getHitbox());
            if (direction != CharacterDirection.NONE) {
                hero.colide(t, direction);
                t.colide(hero, direction);
            }
        }
    }

    @Override
    public void onLevelLoad() {
        ILevel level = levelAPI.getCurrentLevel();
        hero.setLevel(level);
        quest.onLevelLoad(level, entityController);
        chests.forEach(t -> entityController.remove(t));
        chests.clear();
        for (DungeonElement element : ((Room) level).getElements()) {
            if (element instanceof TreasureChest) {
                chests.add((TreasureChest) element);
            }
            entityController.add(element);
        }
        clevel.regenHitboxen(level);
    }

    void spawnMonster() {
        monster.forEach(m -> entityController.remove(m));
        monster.clear();
        for (int i = 0; i < 10; i++) {
            Monster m = new Imp();
            m.setLevel(levelAPI.getCurrentLevel());
            m.setCLevel(clevel);
            m.getHitbox().setCollidable(m);
            monster.add(m);
            entityController.add(m);
        }
    }

    private QuestConfig loadConfig() {
        return dslInterpreter.getQuestConfig(readInFile());
    }

    private String readInFile() {
        String ret = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader("game/assets/scripts/input1.ds"));

            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) ret += sCurrentLine + "\n";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
    /**
     * The program entry point to start the dungeon.
     *
     * @param args command line arguments, but not needed.
     */
    public static void main(String[] args) {
        // start the game
        DesktopLauncher.run(new Starter());
    }
}
