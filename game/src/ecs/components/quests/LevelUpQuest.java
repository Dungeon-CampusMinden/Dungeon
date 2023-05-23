package ecs.components.quests;

import ecs.components.MissingComponentException;
import ecs.components.xp.XPComponent;
import ecs.entities.Entity;
import starter.Game;

public class LevelUpQuest extends Quest {

    /**
     * This constructor is private so that we can only access it by using the
     * buildLevelUpQuest method
     * 
     * @param name        name of the Quest
     * @param description short description of the task and the reward
     * @param task        task to be completed
     * @param reward      IReward that will reward the player
     * @param questHolder player that takes the quest
     */
    private LevelUpQuest(String name, String description, ITask task, IReward reward, Entity questHolder) {
        this.name = name;
        this.description = description;
        this.task = task;
        this.reward = reward;
        this.questHolder = questHolder;
    }

    public static LevelUpQuest buildLevelUpQuest(Entity questHolder) {
        return LevelUpQuest.dungeonLevelQuest(questHolder);
        // !We just don't have xp yet
        // int rnd = (int) (2 * Math.random());
        // if (rnd == 0)
        // return LevelUpQuest.dungeonLevelQuest(questHolder);
        // else
        // return LevelUpQuest.heroLevelQuest(questHolder);
    }

    private static LevelUpQuest dungeonLevelQuest(Entity questHolder) {
        String name = "To Hell and Back";
        int total = Game.getLevel() + 5 + (int) (Math.random() * 15);
        ITask task = new ITask() {

            private final int TOTAL = total;

            @Override
            public void advance() {
                // Doesn't need to do anything
            }

            @Override
            public boolean isCompleted() {
                return TOTAL < Game.getLevel();
            }

            @Override
            public String completion() {
                return Game.getLevel() + " / " + TOTAL;
            }

            /**
             * Called when the game loads
             * 
             * @param entity The questHolder entity
             */
            @Override
            public void load(Entity entity) {
                // Don't need to do anything
            }

        };
        IReward reward = RewardBuilder.buildRandomReward();
        String description = "Reach Dungeonlevel " + total + " to " + reward.toString() + ".";
        return new LevelUpQuest(name, description, task, reward, questHolder);
    }

    // ! No real issue but there is no Experience
    private static LevelUpQuest heroLevelQuest(Entity questHolder) {
        String name = "Allknowing";
        long rnd = 5 + (long) (Math.random() * 5);
        long total;
        if (questHolder.getComponent(XPComponent.class).isPresent())
            total = (((XPComponent) questHolder.getComponent(XPComponent.class).get())).getCurrentLevel() + rnd;
        else
            total = rnd;
        ITask task = new ITask() {

            private final long TOTAL = total;
            private XPComponent xpc = (XPComponent) questHolder.getComponent(XPComponent.class).get();

            @Override
            public void advance() {
                // Nothing todo here it should level up automatically
            }

            /**
             * @return true if the Quest is completed
             *         <p>
             *         otherwise returns false
             */
            @Override
            public boolean isCompleted() {
                if (xpc == null)
                    return false;
                return xpc.getCurrentLevel() >= TOTAL;
            }

            /**
             * @return the current progress
             */
            @Override
            public String completion() {
                if (xpc == null)
                    return "0 / " + TOTAL;
                return xpc.getCurrentLevel() + " / " + TOTAL;
            }

            /**
             * Called when the game loads
             * 
             * @param entity The questHolder entity
             */
            @Override
            public void load(Entity entity) {
                if (entity == null)
                    throw new NullPointerException("Quest holding entity must not be null");
                if (!entity.getComponent(XPComponent.class).isPresent())
                    throw new MissingComponentException("XPComponent");
                xpc = (XPComponent) entity.getComponent(XPComponent.class).get();
            }

        };
        IReward reward = RewardBuilder.buildRandomReward();
        String description = "Reach Playerlevel " + total + " to " + reward.toString() + ".";
        return new LevelUpQuest(name, description, task, reward, questHolder);
    }

}