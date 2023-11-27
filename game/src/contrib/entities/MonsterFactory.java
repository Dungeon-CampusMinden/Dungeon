package contrib.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import contrib.components.*;
import contrib.item.Item;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.interaction.DropItemsInteraction;
import contrib.utils.components.item.ItemGenerator;

import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;

import java.io.IOException;
import java.util.Random;
import java.util.function.BiConsumer;

/** A utility class for building monster entities in the game world. */
public final class MonsterFactory {

    private static final Random RANDOM = new Random();

    private static final String[] MONSTER_FILE_PATHS = {
        "character/monster/chort",
        "character/monster/imp",
        "character/monster/big_deamon",
        "character/monster/big_zombie",
        "character/monster/doc",
        "character/monster/goblin",
        "character/monster/ice_zombie",
        "character/monster/ogre",
        "character/monster/orc_shaman",
        "character/monster/orc_warrior",
        "character/monster/pumpkin_dude",
        "character/monster/zombie"
    };

    private static final int MIN_MONSTER_HEALTH = 10;

    // NOTE: +1 for health as nextInt() is exclusive
    private static final int MAX_MONSTER_HEALTH = 50 + 1;
    private static final float MIN_MONSTER_SPEED = 5.0f;
    private static final float MAX_MONSTER_SPEED = 8.5f;
    private static final DamageType MONSTER_COLLIDE_DAMAGE_TYPE = DamageType.PHYSICAL;
    private static final int MONSTER_COLLIDE_DAMAGE = 10;
    private static final int MONSTER_COLLIDE_COOL_DOWN = 2 * Game.frameRate();

    /**
     * Get an Entity that can be used as a monster.
     *
     * <p>The Entity is not added to the game yet.
     *
     * <p>It will have a {@link PositionComponent}, {@link HealthComponent}, {@link AIComponent}
     * with random AIs from the {@link AIFactory} class, {@link DrawComponent} with a randomly set
     * Animation, {@link VelocityComponent}, {@link CollideComponent}, {@link IdleSoundComponent}
     * and a 10% chance for an {@link InventoryComponent}. If it has an Inventory it will use the
     * {@link DropItemsInteraction} on death.
     *
     * @return A new Entity.
     * @throws IOException if the animation could not been loaded.
     */
    public static Entity randomMonster() throws IOException {
        return randomMonster(MONSTER_FILE_PATHS[RANDOM.nextInt(0, MONSTER_FILE_PATHS.length)]);
    }

    /**
     * Get an Entity that can be used as a monster.
     *
     * <p>The Entity is not added to the game yet. *
     *
     * <p>It will have a {@link PositionComponent}, {@link HealthComponent}, {@link AIComponent} *
     * with random AIs from the {@link AIFactory} class, {@link DrawComponent} with a randomly set *
     * Animation, {@link VelocityComponent}, {@link CollideComponent}, {@link IdleSoundComponent}
     * and a 10% chance for an {@link * InventoryComponent}. If it has an Inventory it will use the
     * {@link DropItemsInteraction} on * death.
     *
     * @param pathToTexture Textures to use for the monster.
     * @return A new Entity.
     * @throws IOException if the animation could not been loaded.
     */
    public static Entity randomMonster(String pathToTexture) throws IOException {
        int health = RANDOM.nextInt(MIN_MONSTER_HEALTH, MAX_MONSTER_HEALTH);
        float speed = RANDOM.nextFloat(MIN_MONSTER_SPEED, MAX_MONSTER_SPEED);

        Entity monster = new Entity("monster");
        int itemRoll = RANDOM.nextInt(0, 10);
        BiConsumer<Entity, Entity> onDeath;
        if (itemRoll == 0) {
            Item item = ItemGenerator.generateItemData();
            InventoryComponent ic = new InventoryComponent(1);
            monster.add(ic);
            ic.add(item);
            onDeath =
                    (e, who) -> {
                        playMonsterDieSound();
                        new DropItemsInteraction().accept(e, who);
                    };
        } else {
            onDeath = (e, who) -> playMonsterDieSound();
        }
        monster.add(new HealthComponent(health, (e) -> onDeath.accept(e, null)));
        monster.add(new PositionComponent());
        monster.add(AIFactory.randomAI(monster));
        monster.add(new DrawComponent(pathToTexture));
        monster.add(new VelocityComponent(speed, speed));
        monster.add(new CollideComponent());
        monster.add(
                new SpikyComponent(
                        MONSTER_COLLIDE_DAMAGE,
                        MONSTER_COLLIDE_DAMAGE_TYPE,
                        MONSTER_COLLIDE_COOL_DOWN));
        monster.add(new IdleSoundComponent(randomMonsterIdleSound()));
        return monster;
    }

    private static void playMonsterDieSound() {
        Sound dieSoundEffect;
        switch (RANDOM.nextInt(4)) {
            case 0 -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_01.wav"));
            case 1 -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_02.wav"));
            case 2 -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_03.wav"));
            default -> dieSoundEffect = Gdx.audio.newSound(Gdx.files.internal("sounds/die_04.wav"));
        }
        long soundID = dieSoundEffect.play();
        dieSoundEffect.setLooping(soundID, false);
        dieSoundEffect.setVolume(soundID, 0.35f);
    }

    private static String randomMonsterIdleSound() {
        switch (RANDOM.nextInt(4)) {
            case 0 -> {
                return "sounds/monster1.wav";
            }
            case 1 -> {
                return "sounds/monster2.wav";
            }
            case 2 -> {
                return "sounds/monster3.wav";
            }
            default -> {
                return "sounds/monster4.wav";
            }
        }
    }
}
