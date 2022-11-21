package mydungeon;

import basiselements.DungeonElement;
import basiselements.hud.ScreenText;
import character.monster.Imp;
import character.monster.Monster;
import character.objects.*;
import character.player.Hero;
import character.skills.BaseSkillEffect;
import collision.CharacterDirection;
import collision.Collidable;
import collision.CollisionMap;
import collision.Hitbox;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
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
    private List<BaseSkillEffect> skillEffects;
    private List<TreasureChest> chests;
    private ScreenController sc;
    private CollisionMap clevel;
    private List<PasswordChest> pwChest;
    private ShapeRenderer shape;
    public static boolean renderHitboxen = true;
    public static Starter Game;

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
        skillEffects = new ArrayList<>();
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
        shape = new ShapeRenderer();
        Game = this;
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
                hero.colide(m, direction.inverse());
                m.colide(hero, direction);
            }
        }
        for (BaseSkillEffect skillEffect : skillEffects) {
            if (!skillEffect.removable()) {
                for (Monster m : monster) {
                    CharacterDirection direction = skillEffect.getHitbox().collide(m.getHitbox());
                    if (direction != CharacterDirection.NONE) {
                        skillEffect.colide(m, direction);
                        m.colide(skillEffect, direction);
                    }
                }
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

    public void spawnEffect(BaseSkillEffect effect) {
        skillEffects.add(effect);
        entityController.add(effect);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        if (renderHitboxen) {
            ILevel level = levelAPI.getCurrentLevel();
            shape.begin(ShapeRenderer.ShapeType.Line);
            shape.setProjectionMatrix(camera.combined);
            shape.setColor(Color.RED);
            for (Collidable box : clevel.getCollidables()) {
                Point bottomLeft = box.getHitbox().getCorners()[Hitbox.CORNER_BOTTOM_LEFT];
                Point topRight = box.getHitbox().getCorners()[Hitbox.CORNER_TOP_RIGHT];

                shape.rect(
                        -0.85f + box.getPosition().x + bottomLeft.x,
                        -0.5f + box.getPosition().y + bottomLeft.y,
                        topRight.x - bottomLeft.x,
                        topRight.y - bottomLeft.y);
            }
            for (Collidable box : monster) {
                Point bottomLeft = box.getHitbox().getCorners()[Hitbox.CORNER_BOTTOM_LEFT];
                Point topRight = box.getHitbox().getCorners()[Hitbox.CORNER_TOP_RIGHT];

                shape.rect(
                        -0.85f + box.getPosition().x + bottomLeft.x,
                        -0.5f + box.getPosition().y + bottomLeft.y,
                        topRight.x - bottomLeft.x,
                        topRight.y - bottomLeft.y);
            }
            skillEffects.removeIf(BaseSkillEffect::removable);
            for (Collidable box : skillEffects) {
                Point bottomLeft = box.getHitbox().getCorners()[Hitbox.CORNER_BOTTOM_LEFT];
                Point topRight = box.getHitbox().getCorners()[Hitbox.CORNER_TOP_RIGHT];

                shape.rect(
                        -0.85f + box.getPosition().x + bottomLeft.x,
                        -0.5f + box.getPosition().y + bottomLeft.y,
                        topRight.x - bottomLeft.x,
                        topRight.y - bottomLeft.y);
            }
            Hitbox box = hero.getHitbox();
            Point bottomLeft = box.getCorners()[Hitbox.CORNER_BOTTOM_LEFT];
            Point topRight = box.getCorners()[Hitbox.CORNER_TOP_RIGHT];

            shape.rect(
                    -0.85f + hero.getPosition().x + bottomLeft.x,
                    -0.5f + hero.getPosition().y + bottomLeft.y,
                    topRight.x - bottomLeft.x,
                    topRight.y - bottomLeft.y);
            shape.end();
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
