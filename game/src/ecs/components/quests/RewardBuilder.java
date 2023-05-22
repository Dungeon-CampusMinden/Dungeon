package ecs.components.quests;

import ecs.components.HealthComponent;
import ecs.entities.Entity;

/**
 * Creates new instances of IReward
 */
public class RewardBuilder {

    /**
     * Chooses a random reward and builds a new instance
     * 
     * @return new instance of IReward
     */
    public static IReward buildRandomReward() {
        final int METHOD_COUNT = 2;
        int key = (int) (Math.random() * METHOD_COUNT);
        switch (key) {

            case 1:
                return buildIncreaseMaxHealthReward();

            case 2:
                return buildItemReward();

            default:
                return buildHealReward();
        }
    }

    /**
     * Builds a new instance of IReward that will heal the player to maximum health
     * 
     * @return new instance of IReward
     */
    public static IReward buildHealReward() {
        IReward reward = new IReward() {

            @Override
            public void reward(Entity entity) {
                if (entity.getComponent(HealthComponent.class).isPresent()) {
                    HealthComponent health = ((HealthComponent) entity.getComponent(HealthComponent.class).get());
                    health.setCurrentHealthpoints(health.getMaximalHealthpoints());
                }
            }

            @Override
            public String toString() {
                return "get fully healed";
            }

        };
        return reward;
    }

    /**
     * Builds a new instance of IReward that will increase the player's maximum
     * healthpoints by one percent
     * 
     * @return new instance of IReward
     */
    public static IReward buildIncreaseMaxHealthReward() {
        IReward reward = new IReward() {

            @Override
            public void reward(Entity entity) {
                if (entity.getComponent(HealthComponent.class).isPresent()) {
                    HealthComponent health = ((HealthComponent) entity.getComponent(HealthComponent.class).get());
                    health.setMaximalHealthpoints((int) (health.getMaximalHealthpoints() * 1.01));
                }
            }

            @Override
            public String toString() {
                return "get 1% more health points";
            }

        };
        return reward;
    }

    // TODO: Implement items
    public static IReward buildItemReward() {
        throw new UnsupportedOperationException("The method buildItemReward is not yet implemented");
    }

}
