package ecs.components.quests;

import ecs.entities.Chort;
import ecs.entities.DarkKnight;
import ecs.entities.Entity;
import ecs.entities.Imp;
import ecs.entities.Monster;

import java.util.logging.Logger;

/**
 * Creates new Quests instead of the Quests
 */
public class QuestBuilder {
    private static transient final Logger questLogger = Logger.getLogger(QuestBuilder.class.getName());
    private static final Class[] KILLABLES = {
            Monster.class,
            Imp.class,
            Chort.class,
            DarkKnight.class
    };

    // TODO: implement this
    public static CollectQuest buildCollectQuest() {
        return CollectQuest.buildCollectQuest();
    }

    /**
     * Builds a new quest where monsters have to be killed
     *
     * @param entity the entity to have this quest
     * @return new instance of KillQuest
     */
    public static KillQuest buildKillQuest(Entity entity) {
        return KillQuest.buildKillQuest(KILLABLES[(int) (Math.random() * KILLABLES.length)], entity);
    }

    /**
     * Builds a new quest where the player has to move
     *
     * @param entity the entity to have this quest
     * @return new instance of MovementQuest
     */
    public static MovementQuest buildMovementQuest(Entity entity) {
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
        return LevelUpQuest.buildLevelUpQuest(entity);
    }

    /**
     * Builds a random Quest
     *
     * @param entity entity to have this quest
     * @return new instance of Quest
     */
    public static Quest buildRandomQuest(Entity entity) {
        final int METHOD_COUNT = 2;
        int key = (int) (Math.random() * METHOD_COUNT);
        switch (key) {

            case 1:
                questLogger.info("KillQuest was build");
                return buildKillQuest(entity);

            case 2:
                questLogger.info("LevelQuest was build");
                return buildLevelUpQuest(entity);

            case 3:
                questLogger.info("CollectQuest was build");
                return buildCollectQuest();

            default:
                return buildMovementQuest(entity);
        }
    }

}
