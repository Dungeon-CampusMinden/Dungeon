package mydungeon;

import character.monster.Imp;
import character.monster.Monster;
import character.player.Hero;
import collision.CharacterDirection;
import collision.CollisionMap;
import controller.Game;
import controller.ScreenController;
import java.util.ArrayList;
import java.util.List;
import level.elements.ILevel;
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
    private ScreenController sc;
    private CollisionMap clevel;

    @Override
    protected void setup() {
        clevel = new CollisionMap();
        monster = new ArrayList<>();
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
        if (levelAPI.getCurrentLevel().isOnEndTile(hero)) levelAPI.loadLevel();
        checkForCollision();
    }

    private void checkForCollision() {
        for (Monster m : monster) {
            CharacterDirection direction = hero.getHitbox().collide(m.getHitbox());
            if (direction != CharacterDirection.NONE) {
                hero.colide(m, direction);
                m.colide(hero, direction);
            }
        }
    }

    @Override
    public void onLevelLoad() {
        ILevel level = levelAPI.getCurrentLevel();
        hero.setLevel(level);
        spawnMonster();
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
