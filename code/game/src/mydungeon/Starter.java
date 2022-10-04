package mydungeon;

import character.monster.Imp;
import character.monster.Monster;
import character.player.Hero;
import collision.CharacterDirection;
import controller.Game;
import controller.ScreenController;
import java.util.ArrayList;
import java.util.List;
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
    ScreenController sc;

    @Override
    protected void setup() {
        monster = new ArrayList<>();
        hero = new Hero();
        sc = new ScreenController(batch);
        controller.add(sc);
        levelAPI.loadLevel();
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
            CharacterDirection direction = hero.getHitbox().colide(m.getHitbox());
            if (direction != CharacterDirection.NONE) {
                hero.colide(m, direction);
                m.colide(hero, direction);
            }
        }
    }

    @Override
    public void onLevelLoad() {
        hero.setLevel(levelAPI.getCurrentLevel());
        spawnMonster();
    }

    void spawnMonster() {
        monster.forEach(m -> entityController.remove(m));
        monster.clear();
        for (int i = 0; i < 10; i++) {
            Monster m = new Imp();
            m.setLevel(levelAPI.getCurrentLevel());
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
