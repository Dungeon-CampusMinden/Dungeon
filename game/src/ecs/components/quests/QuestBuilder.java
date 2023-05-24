package ecs.components.quests;

import java.util.logging.Logger;

import ecs.entities.Chort;
import ecs.entities.DarkKnight;
import ecs.entities.Entity;
import ecs.entities.Imp;
import ecs.entities.Monster;

/**
 * Creates new Quests instead of the Quests
 */
public class QuestBuilder {
    private static final Logger QB_LOGGER = Logger.getLogger(QuestBuilder.class.getSimpleName());

    private static final Class[] KILLABLES = {
            Monster.class,
            Imp.class,
            Chort.class,
            DarkKnight.class
    };

    // TODO: implement this
    public static CollectQuest buildCollectQuest() {
        QB_LOGGER.info("Building new CollectQuest");
        return CollectQuest.buildCollectQuest();
    }

    /**
     * Builds a new quest where monsters have to be killed
     *
     * @param entity the entity to have this quest
     * @return new instance of KillQuest
     */
    public static KillQuest buildKillQuest(Entity entity) {
        QB_LOGGER.info("Building new KillQuest");
        return KillQuest.buildKillQuest(KILLABLES[(int) (Math.random() * KILLABLES.length)], entity);
    }

    /**
     * Builds a new quest where the Boss has to be killed
     *
     * @param entity the entity to have this quest
     * @return new instance of KillQuest
     */
    public static KillQuest buildBossKillQuest(Entity entity) {
        QB_LOGGER.info("Building new KillQuest for Bosses");
        return KillQuest.buildBossKillQuest(entity);
    }

    /**
     * Builds a new quest where the player has to move
     *
     * @param entity the entity to have this quest
     * @return new instance of MovementQuest
     */
    public static MovementQuest buildMovementQuest(Entity entity) {
        QB_LOGGER.info("Building new MovementQuest");
        return MovementQuest.buildMovementQuest(entity);
    }

    /**
     * Builds a new quest where the player has to LevelUp
     * for now only the dungeon depth later maybe the actual level of the player
     *
     * @param entity the entity to have this quest
     * @return new instance of LevelUpQuest
     */
    public static LevelUpQuest buildLevelUpQuest(Entity entity) {
        QB_LOGGER.info("Building new LevelUpQuest");
        return LevelUpQuest.buildLevelUpQuest(entity);
    }

    /**
     * Builds a random Quest
     *
     * @param entity entity to have this quest
     * @return new instance of Quest
     */
    public static Quest buildRandomQuest(Entity entity) {
        final int METHOD_COUNT = 4;
        int key = (int) (Math.random() * METHOD_COUNT);
        switch (key) {

            case 1:
                return buildKillQuest(entity);

            case 2:
                return buildLevelUpQuest(entity);

            case 3:
                return buildBossKillQuest(entity);

            case 4:
                return buildCollectQuest();

            default:
                return buildMovementQuest(entity);
        }
    }

}
