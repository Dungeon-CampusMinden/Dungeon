package ecs.components.quests;

import ecs.components.HealthComponent;
import ecs.entities.Entity;

import java.util.logging.Logger;

/**
 * Creates new instances of IReward
 */
public class RewardBuilder {
    private static transient final Logger rewardLogger = Logger.getLogger(RewardBuilder.class.getName());
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
                rewardLogger.info("You earned the MaxHealthReward");
                return buildIncreaseMaxHealthReward();
            case 2:
                rewardLogger.info("You earned the buildItemReward");
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
        return new IReward() {

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
    }

    /**
     * Builds a new instance of IReward that will increase the player's maximum
     * healthpoints by one percent
     *
     * @return new instance of IReward
     */
    public static IReward buildIncreaseMaxHealthReward() {
        return new IReward() {

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
    }

    // TODO: Implement items
    public static IReward buildItemReward() {
        throw new UnsupportedOperationException("The method buildItemReward is not yet implemented");
    }

    /**
     * Builds a new reward that will double the quest holders Healthpoints and fully
     * heal the quest holder
     *
     * @return new Instance of IReward
     */
    public static IReward buildBossReward() {
        return new IReward() {

            @Override
            public void reward(Entity entity) {
                if (entity.getComponent(HealthComponent.class).isPresent()) {
                    HealthComponent health = ((HealthComponent) entity.getComponent(HealthComponent.class).get());
                    health.setMaximalHealthpoints(health.getMaximalHealthpoints() * 2);
                }
                if (entity.getComponent(HealthComponent.class).isPresent()) {
                    HealthComponent health = ((HealthComponent) entity.getComponent(HealthComponent.class).get());
                    health.setCurrentHealthpoints(health.getMaximalHealthpoints());
                }
            }

            @Override
            public String toString() {
                return "get 100% more health points and get fully healed";
            }

        };
    }

}
