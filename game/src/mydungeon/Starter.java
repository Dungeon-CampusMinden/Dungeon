package mydungeon;

import character.monster.Imp;
import character.monster.Monster;
import character.objects.*;
import character.player.Hero;
import collision.CharacterDirection;
import collision.CollisionMap;
import controller.Game;
import controller.ScreenController;
import dslToGame.QuestConfig;
import java.util.ArrayList;
import java.util.List;
import level.elements.ILevel;
import level.elements.tile.DoorTile;
import level.elements.tile.Tile;
import level.tools.LevelElement;
import levelgraph.GraphLevelGenerator;
import minimap.IMinimap;
import starter.DesktopLauncher;
import tools.Point;

/**
 * The entry class to create your own implementation.
 *
 * <p>This class is directly derived form {@link Game} and acts as the {@link
 * com.badlogic.gdx.Game}.
 */
public class Starter extends Game {
    private Hero hero;
    private List<Monster> monster;
    private ScreenController sc;
    private CollisionMap clevel;
    // private List<TreasureChest> chest;
    private List<PasswordChest> pwChest;
    private Letter letter;

    @Override
    protected void setup() {
        QuestConfig config = loadConfig();
        generator = new GraphLevelGenerator(config.levelGenGraph());
        levelAPI.setGenerator(generator);
        clevel = new CollisionMap();
        monster = new ArrayList<>();
        // chest = new ArrayList<>();
        pwChest = new ArrayList<>();
        letter =
                new Letter(
                        'A',
                        new IMinimap() {
                            @Override
                            public void drawOnMap(char c) {
                                System.out.println("Draw on map " + c);
                            }

                            @Override
                            public void drawMap() {}
                        });
        hero = new Hero();
        sc = new ScreenController(batch);
        controller.add(sc);
        levelAPI.loadLevel();
        hero.getHitbox().setCollidable(hero);
        camera.follow(hero);
        entityController.add(hero);
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
        /*
         * for (TreasureChest t : chest) { CharacterDirection direction =
         * hero.getHitbox().collide(t.getHitbox()); if (direction != CharacterDirection.NONE) {
         * hero.colide(t, direction); t.colide(hero, direction); } }
         */
        for (PasswordChest p : pwChest) {
            CharacterDirection direction = hero.getHitbox().collide(p.getHitbox());
            if (direction != CharacterDirection.NONE) {
                hero.colide(p, direction);
                p.colide(hero, direction);
            }
        }
    }

    @Override
    public void onLevelLoad() {
        ILevel level = levelAPI.getCurrentLevel();
        hero.setLevel(level);
        spawnMonster();
        // spawnTreasureChest();
        spawnPasswordChest();
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

    /*
     * void spawnTreasureChest() { chest.forEach(t -> entityController.remove(t)); chest.clear();
     * Point p = levelAPI.getCurrentLevel().getStartTile().getCoordinate().toPoint(); p.x += 1;
     * TreasureChest t = new TreasureChest(p); chest.add(t); entityController.add(t); }
     */
    void spawnPasswordChest() {
        pwChest.forEach(pC -> entityController.remove(pC));
        pwChest.clear();
        Point p = levelAPI.getCurrentLevel().getStartTile().getCoordinate().toPoint();
        p.x += 1;
        PasswordChest pWChest = new PasswordChest(p, "Test", sc);
        pwChest.add(pWChest);
        entityController.add(pWChest);
    }

    private QuestConfig loadConfig() {
        // todo
        // return new QuestConfig();
        return null;
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
