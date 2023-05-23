package ecs.components.quests;

import ecs.components.AnimationComponent;
import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.entities.Entity;
import starter.Game;

/**
 * Class for all Quests that involve killing monsters
 */
public class KillQuest extends Quest {

    /**
     * This constructor is private so that we can only access it by using the
     * buildKillQuest method
     * 
     * @param name        name of the Quest
     * @param description short description of the task and the reward
     * @param task        task to be completed
     * @param reward      IReward that will reward the player
     * @param questHolder player that takes the quest
     */
    private KillQuest(String name, String description, ITask task, IReward reward, Entity questHolder) {
        this.name = name;
        this.description = description;
        this.task = task;
        this.reward = reward;
        this.questHolder = questHolder;
    }

    /**
     * Builds a KillQuest from scratch
     * 
     * @param klass       Class of the entity that shall be killed
     * @param questHolder entity that owns the quest
     * @return new Instance of KillQuest
     */
    public static KillQuest buildKillQuest(Class klass, Entity questHolder) {
        String name = "Slaughterer of " + klass.getSimpleName() + "s";
        int total = 1 + (Game.getLevel() >>> 1)
                + (int) (Math.random() * ((Game.getLevel() << 1) - (Game.getLevel() >>> 1)));
        ITask task = new ITask() {

            // Total number of entities to be killed 1/2 times the current floor level to 2
            // times the current floor level
            private final int TOTAL = total;
            private int count = 0;

            /**
             * Adds to the counter for the amount of entities killed by the questHolder
             */
            @Override
            public void advance() {
                count += Game.getEntities().stream()
                        // Consider only entities that extend the given class
                        .filter(e -> klass.isAssignableFrom(e.getClass()))
                        // Consider only entities that have a HealthComponent
                        .flatMap(e -> e.getComponent(HealthComponent.class).stream())
                        // Form triples (e, hc, ac)
                        .map(hc -> buildDataObject((HealthComponent) hc))
                        // Filter all dead entities
                        .filter(hsd -> hsd.hc.getCurrentHealthpoints() <= 0)
                        // Filter entities that were killed by something
                        .filter(hsd -> hsd.hc.getLastDamageCause().isPresent())
                        // Filter entities that were killed by the questHolder
                        .filter(hsd -> hsd.hc.getLastDamageCause().get().equals(questHolder))
                        // increase count
                        .count();
            }

            /**
             * @return true if the Quest is completed
             *         <p>
             *         otherwise returns false
             */
            @Override
            public boolean isCompleted() {
                return count >= TOTAL;
            }

            /**
             * @return the current progress
             */
            @Override
            public String completion() {
                return count + " / " + TOTAL;
            }

            /**
             * Called when the game loads
             * 
             * @param entity The questHolder entity
             */
            @Override
            public void load(Entity entity) {
                // Shouldn't be necessary
            }

        };
        IReward reward = RewardBuilder.buildRandomReward();
        String description = "Kill " + total + " " + klass.getSimpleName() + "s to " + reward.toString() + ".";
        return new KillQuest(name, description, task, reward, questHolder);
    }

    /*
     * ----------------------------------------------------------------
     * Mostly things from the health System
     */

    // private record to hold all data during streaming
    private record HSData(Entity e, HealthComponent hc, AnimationComponent ac) {
    }

    private static HSData buildDataObject(HealthComponent hc) {
        Entity e = hc.getEntity();

        AnimationComponent ac = (AnimationComponent) e.getComponent(AnimationComponent.class)
                .orElseThrow(KillQuest::missingAC);

        return new HSData(e, hc, ac);
    }

    private static MissingComponentException missingAC() {
        return new MissingComponentException("AnimationComponent");
    }

}
