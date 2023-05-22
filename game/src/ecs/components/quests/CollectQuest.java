package ecs.components.quests;

import ecs.entities.Entity;

public class CollectQuest extends Quest {

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
    private CollectQuest(String name, String description, ITask task, IReward reward, Entity questHolder) {
        this.name = name;
        this.description = description;
        this.task = task;
        this.reward = reward;
        this.questHolder = questHolder;
    }

    public static CollectQuest buildCollectQuest() {
        // TODO: implement items or collectables
        throw new UnsupportedOperationException("The class CollectQuest is not yet implemented");
    }

}
