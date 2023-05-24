package ecs.components.quests;

import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.entities.Entity;
import starter.Game;

import java.util.logging.Logger;

public class MovementQuest extends Quest {
    private transient final Logger movementLogger = Logger.getLogger(this.getClass().getName());
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
    private MovementQuest(String name, String description, ITask task, IReward reward, Entity questHolder) {
        this.name = name;
        this.description = description;
        this.task = task;
        this.reward = reward;
        this.questHolder = questHolder;
        movementLogger.info(this.name + "was created");
    }

    /**
     * Builds a new MovementQuest from scratch
     *
     * @param questHolder Entity that owns the quest
     * @return new Instance of MovementQuest
     */
    public static MovementQuest buildMovementQuest(Entity questHolder) {
        String name = "Wanderer";
        int total = (int) (Math.pow(10, Math.floor(Math.log(1 + (Game.getLevel() >> 3)) / Math.log(2) + 1)));
        ITask task = new ITask() {

            private final int TOTAL = total;
            private PositionComponent pc = (PositionComponent) questHolder.getComponent(PositionComponent.class).get();
            private float distance = 0;
            private float lastX = pc.getPosition().x, lastY = pc.getPosition().y;

            /**
             * Adds the distance to the last position to the total distance counter
             */
            @Override
            public void advance() {
                if (pc == null)
                    throw new MissingComponentException("PositionComponent");
                distance += Math.abs(lastX - pc.getPosition().x);
                lastX = pc.getPosition().x;
                distance += Math.abs(lastY - pc.getPosition().y);
                lastY = pc.getPosition().y;
            }

            /**
             * @return true if the Quest is completed
             *         <p>
             *         otherwise returns false
             */
            @Override
            public boolean isCompleted() {
                return distance >= TOTAL;
            }

            /**
             * @return the current progress
             */
            @Override
            public String completion() {
                return distance + " / " + TOTAL;
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
                if (entity.getComponent(PositionComponent.class) == null)
                    throw new MissingComponentException("PositionComponent");
                pc = (PositionComponent) entity.getComponent(PositionComponent.class).get();
            }

        };
        IReward reward = RewardBuilder.buildRandomReward();
        String description = "Walk " + total + " tiles to " + reward.toString() + ".";
        return new MovementQuest(name, description, task, reward, questHolder);
    }

}
