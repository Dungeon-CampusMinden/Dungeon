package ecs.components.quests;

import java.io.Serializable;

import ecs.entities.Entity;

/**
 * Quests
 * <p>
 * Superclass for all kinds of quests.
 * <p>
 * Quests are created by using the QuestBuilder class
 * they could be created by using their own static methods but in QuestBuilder
 * they are all
 * at on place.
 */
public abstract class Quest implements Serializable {

    protected String name;
    protected String description;
    protected ITask task;
    protected IReward reward;
    protected Entity questHolder;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ITask getTask() {
        return task;
    }

    public IReward getReward() {
        return reward;
    }

    public Entity getQuestHolder() {
        return questHolder;
    }

    /**
     * Throws a NullPointerException if there is no task associated with this quest
     * or if there is no reward associated with this quest
     * or if there is no questHolder associated with this quest
     * 
     * <p>
     * Otherwise this will advance the quest and if the quest
     * is completed it will reward the questHolder
     */
    public void update() throws NullPointerException {
        if (task == null)
            throw new NullPointerException("task is null");
        if (reward == null)
            throw new NullPointerException("reward is null");
        if (questHolder == null)
            throw new NullPointerException("questHolder is null");
        task.advance();
        if (task.isCompleted()) {
            reward.reward(questHolder);
            System.out.println("Quest " + getName() + " completed :" + getDescription());
        }
    }

    @Override
    public String toString() {
        return getName() + ":\n" + getDescription() + "\n" + getTask().completion()
                + "\n==========================================================";
    }

}
